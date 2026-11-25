package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.dto.ParticipantRegistrationRequest;
import ek.dk.yogaFestival.model.Festival;
import ek.dk.yogaFestival.model.FestivalRegistration;
import ek.dk.yogaFestival.model.Participant;
import ek.dk.yogaFestival.repository.FestivalRegistrationRepo;
import ek.dk.yogaFestival.repository.ParticipantRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantRegistrationServiceTest {

    @Mock
    private ParticipantRepo participantRepo;

    @Mock
    private FestivalRegistrationRepo festivalRegistrationRepo;

    @Mock
    private FestivalService festivalService;

    @InjectMocks
    private ParticipantRegistrationService registrationService;

    @Test
    void shouldRegisterParticipantWithoutPhoneNumber() {
        Festival festival = new Festival("Yoga Festival", 2026);
        ParticipantRegistrationRequest request = new ParticipantRegistrationRequest(
                "Anna Hansen",
                "anna@example.com",
                null,
                1L
        );

        when(festivalService.findById(1L)).thenReturn(festival);
        when(participantRepo.findByEmail("anna@example.com"))
                .thenReturn(Optional.empty());
        when(participantRepo.save(any(Participant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(festivalRegistrationRepo.save(any(FestivalRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FestivalRegistration registration = registrationService.register(request);

        assertEquals("Anna Hansen", registration.getParticipant().getName());
        assertEquals("anna@example.com", registration.getParticipant().getEmail());
        assertNull(registration.getParticipant().getPhoneNumber());
        assertEquals(festival, registration.getFestival());
        verify(participantRepo).save(any(Participant.class));
        verify(festivalRegistrationRepo).save(any(FestivalRegistration.class));
    }

    @Test
    void shouldRegisterParticipantWithPhoneNumber() {
        Festival festival = new Festival("Yoga Festival", 2026);
        ParticipantRegistrationRequest request = new ParticipantRegistrationRequest(
                "Peter Larsen",
                "peter@example.com",
                " 12345678 ",
                1L
        );

        when(festivalService.findById(1L)).thenReturn(festival);
        when(participantRepo.findByEmail("peter@example.com"))
                .thenReturn(Optional.empty());
        when(participantRepo.save(any(Participant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(festivalRegistrationRepo.save(any(FestivalRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FestivalRegistration registration = registrationService.register(request);

        assertEquals("Peter Larsen", registration.getParticipant().getName());
        assertEquals("12345678", registration.getParticipant().getPhoneNumber());
        assertEquals(festival, registration.getFestival());
        verify(participantRepo).save(any(Participant.class));
        verify(festivalRegistrationRepo).save(any(FestivalRegistration.class));
    }

    @Test
    void shouldReuseExistingFestivalRegistrationForParticipant() {
        Participant participant = mock(Participant.class);
        Festival festival = mock(Festival.class);
        FestivalRegistration existingRegistration = mock(FestivalRegistration.class);

        when(participant.getId()).thenReturn(12L);
        when(festival.getId()).thenReturn(3L);
        when(participantRepo.findByEmail("anna@example.com"))
                .thenReturn(Optional.of(participant));
        when(festivalService.findById(3L)).thenReturn(festival);
        when(festivalRegistrationRepo.findByParticipant_IdAndFestival_Id(12L, 3L))
                .thenReturn(Optional.of(existingRegistration));

        FestivalRegistration registration = registrationService.register(
                new ParticipantRegistrationRequest(
                        "Anna Hansen",
                        "anna@example.com",
                        null,
                        3L
                )
        );

        assertSame(existingRegistration, registration);
        verify(participantRepo, never()).save(any(Participant.class));
        verify(festivalRegistrationRepo, never()).save(any(FestivalRegistration.class));
    }
}
