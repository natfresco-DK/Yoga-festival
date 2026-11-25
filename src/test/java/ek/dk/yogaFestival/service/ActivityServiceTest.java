package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.dto.ActivityRequest;
import ek.dk.yogaFestival.model.Activity;
import ek.dk.yogaFestival.model.Festival;
import ek.dk.yogaFestival.model.RegistrationStatus;
import ek.dk.yogaFestival.repository.ActivityRegistrationRepo;
import ek.dk.yogaFestival.repository.ActivityRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepo activityRepo;

    @Mock
    private FestivalService festivalService;

    @Mock
    private ActivityRegistrationRepo activityRegistrationRepo;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void shouldSaveValidActivity() {
        Festival festival = new Festival("Yoga Festival", 2026);
        ActivityRequest request = validRequest();

        when(festivalService.findById(1L)).thenReturn(festival);
        when(activityRepo.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Activity saved = activityService.saveActivity(request);

        assertEquals("Morning Yoga", saved.getName());
        assertEquals("Main Hall", saved.getRoom());
        assertEquals(festival, saved.getFestival());
        verify(activityRepo).save(saved);
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        ActivityRequest request = new ActivityRequest(
                "Morning Yoga",
                "A description",
                "Tina",
                "Main Hall",
                "Yoga mat",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 9, 0),
                20,
                1L
        );

        assertThrows(IllegalArgumentException.class,
                () -> activityService.saveActivity(request));
        verifyNoInteractions(activityRepo);
    }

    @Test
    void shouldRejectMissingFestival() {
        ActivityRequest request = validRequest();

        when(festivalService.findById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> activityService.saveActivity(request));
        verifyNoInteractions(activityRepo);
    }

    @Test
    void shouldCalculateAvailablePlacesFromConfirmedRegistrations() {
        Activity activity = new Activity();
        activity.setId(10L);
        activity.setCapacity(3);
        when(activityRegistrationRepo.countByActivityIdAndStatus(
                10L, RegistrationStatus.CONFIRMED
        )).thenReturn(0L, 1L, 3L);

        assertEquals(3, activityService.getAvailablePlaces(activity));
        assertEquals(2, activityService.getAvailablePlaces(activity));
        assertEquals(0, activityService.getAvailablePlaces(activity));
        verify(activityRegistrationRepo, times(3)).countByActivityIdAndStatus(
                10L, RegistrationStatus.CONFIRMED
        );
    }

    @Test
    void shouldUpdateExistingActivityAndPreserveRelations() {
        Festival festival = new Festival("Yoga Festival", 2026);
        Activity existing = new Activity(
                10L,
                "Old name",
                "Old description",
                "Old instructor",
                "Old room",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                20,
                "Yoga mat"
        );
        existing.setFestival(festival);

        List<?> registrationsBefore = existing.getRegistrations();

        when(activityRepo.findById(10L)).thenReturn(Optional.of(existing));
        when(activityRegistrationRepo.countByActivityIdAndStatus(
                10L, RegistrationStatus.CONFIRMED)).thenReturn(2L);
        when(activityRepo.save(existing)).thenReturn(existing);

        Activity updated = activityService.updateActivity(10L, validRequest());

        assertEquals(10L, updated.getId());
        assertSame(festival, updated.getFestival());
        assertEquals("Morning Yoga", updated.getName());
        assertEquals("Main Hall", updated.getRoom());
        assertSame(registrationsBefore, updated.getRegistrations());
        verify(activityRepo).save(existing);
    }

    @Test
    void shouldRejectCapacityBelowConfirmedRegistrations() {
        Festival festival = new Festival("Yoga Festival", 2026);
        Activity existing = new Activity();
        existing.setId(10L);
        existing.setFestival(festival);

        ActivityRequest request = new ActivityRequest(
                "Morning Yoga",
                "A description",
                "Tina",
                "Main Hall",
                "Yoga mat",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                2,
                1L
        );

        when(activityRepo.findById(10L)).thenReturn(Optional.of(existing));
        when(activityRegistrationRepo.countByActivityIdAndStatus(
                10L, RegistrationStatus.CONFIRMED)).thenReturn(3L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.updateActivity(10L, request)
        );

        assertEquals(
                "Capacity cannot be lower than the number of confirmed registrations",
                exception.getMessage()
        );
        verify(activityRepo, never()).save(any(Activity.class));
    }

    private ActivityRequest validRequest() {
        return new ActivityRequest(
                "Morning Yoga",
                "A description",
                "Tina",
                "Main Hall",
                "Yoga mat",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                20,
                1L
        );
    }
}