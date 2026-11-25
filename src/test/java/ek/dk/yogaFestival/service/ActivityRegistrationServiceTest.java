package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.dto.ActivityRegistrationRequest;
import ek.dk.yogaFestival.model.Activity;
import ek.dk.yogaFestival.model.ActivityRegistration;
import ek.dk.yogaFestival.model.Festival;
import ek.dk.yogaFestival.model.FestivalRegistration;
import ek.dk.yogaFestival.model.RegistrationStatus;
import ek.dk.yogaFestival.repository.ActivityRegistrationRepo;
import ek.dk.yogaFestival.repository.ActivityRepo;
import ek.dk.yogaFestival.repository.FestivalRegistrationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityRegistrationServiceTest {

    @Mock
    private FestivalRegistrationRepo festivalRegistrationRepo;

    @Mock
    private ActivityRepo activityRepo;

    @Mock
    private ActivityRegistrationRepo activityRegistrationRepo;

    @InjectMocks
    private ActivityRegistrationService registrationService;

    @Test
    void shouldAllowRegistrationForLastAvailablePlace() {
        Festival festival = mock(Festival.class);
        FestivalRegistration festivalRegistration = mock(FestivalRegistration.class);
        Activity activity = mock(Activity.class);
        prepareRegistration(festival, festivalRegistration, activity, 5L, 10L);

        when(activityRegistrationRepo.countByActivityIdAndStatus(
                10L, RegistrationStatus.CONFIRMED
        )).thenReturn(1L);
        when(activityRegistrationRepo.existsByFestivalRegistrationIdAndActivityIdAndStatus(
                5L, 10L, RegistrationStatus.CONFIRMED
        )).thenReturn(false);
        when(activity.getCapacity()).thenReturn(2);
        when(activityRegistrationRepo.save(any(ActivityRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var saved = registrationService.register(
                new ActivityRegistrationRequest(5L, java.util.List.of(10L))
        );

        assertEquals(1, saved.size());
        verify(activityRegistrationRepo).save(any(ActivityRegistration.class));
    }

    @Test
    void shouldRejectRegistrationWhenActivityIsFull() {
        Festival festival = mock(Festival.class);
        FestivalRegistration festivalRegistration = mock(FestivalRegistration.class);
        Activity activity = mock(Activity.class);
        prepareRegistration(festival, festivalRegistration, activity, 5L, 10L);

        when(activityRegistrationRepo.countByActivityIdAndStatus(
                10L, RegistrationStatus.CONFIRMED
        )).thenReturn(2L);
        when(activityRegistrationRepo.existsByFestivalRegistrationIdAndActivityIdAndStatus(
                5L, 10L, RegistrationStatus.CONFIRMED
        )).thenReturn(false);
        when(activity.getCapacity()).thenReturn(2);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.register(
                        new ActivityRegistrationRequest(5L, java.util.List.of(10L))
                )
        );

        assertEquals("Activity 10 is full", exception.getMessage());
        verify(activityRegistrationRepo, never()).save(any(ActivityRegistration.class));
    }

    @Test
    void shouldRejectParticipantAlreadyRegisteredForActivity() {
        Festival festival = mock(Festival.class);
        FestivalRegistration festivalRegistration = mock(FestivalRegistration.class);
        Activity activity = mock(Activity.class);
        prepareRegistration(festival, festivalRegistration, activity, 5L, 10L);
        when(activityRegistrationRepo.existsByFestivalRegistrationIdAndActivityIdAndStatus(
                5L, 10L, RegistrationStatus.CONFIRMED
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.register(
                        new ActivityRegistrationRequest(5L, java.util.List.of(10L))
                )
        );

        assertEquals(
                "Participant is already registered for activity 10",
                exception.getMessage()
        );
        verify(activityRegistrationRepo, never()).save(any(ActivityRegistration.class));
    }

    @Test
    void shouldRejectDuplicateActivityIdsInSameRequest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.register(
                        new ActivityRegistrationRequest(5L, java.util.List.of(10L, 10L))
                )
        );

        assertEquals("Duplicate activity IDs are not allowed", exception.getMessage());
        verifyNoInteractions(
                festivalRegistrationRepo,
                activityRepo,
                activityRegistrationRepo
        );
    }

    private void prepareRegistration(
            Festival festival,
            FestivalRegistration festivalRegistration,
            Activity activity,
            Long festivalRegistrationId,
            Long activityId
    ) {
        when(festival.getId()).thenReturn(1L);
        when(festivalRegistration.getId()).thenReturn(festivalRegistrationId);
        when(festivalRegistration.getFestival()).thenReturn(festival);
        when(activity.getFestival()).thenReturn(festival);
        when(festivalRegistrationRepo.findById(festivalRegistrationId))
                .thenReturn(Optional.of(festivalRegistration));
        when(activityRepo.findByIdForUpdate(activityId)).thenReturn(Optional.of(activity));
    }
}
