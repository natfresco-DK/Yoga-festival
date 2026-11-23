package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.dto.ActivityRequest;
import ek.dk.yogaFestival.model.Activity;
import ek.dk.yogaFestival.model.Festival;
import ek.dk.yogaFestival.repository.ActivityRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepo activityRepo;

    @Mock
    private FestivalService festivalService;

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