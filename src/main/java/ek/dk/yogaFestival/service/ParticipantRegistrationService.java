package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.dto.ParticipantRegistrationRequest;
import ek.dk.yogaFestival.model.Festival;
import ek.dk.yogaFestival.model.FestivalRegistration;
import ek.dk.yogaFestival.model.Participant;
import ek.dk.yogaFestival.repository.FestivalRegistrationRepo;
import ek.dk.yogaFestival.repository.ParticipantRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ParticipantRegistrationService {

    private final ParticipantRepo participantRepo;
    private final FestivalRegistrationRepo festivalRegistrationRepo;
    private final FestivalService festivalService;

    public ParticipantRegistrationService(ParticipantRepo participantRepo, FestivalRegistrationRepo festivalRegistrationRepo, FestivalService festivalService) {
        this.participantRepo = participantRepo;
        this.festivalRegistrationRepo = festivalRegistrationRepo;
        this.festivalService = festivalService;
    }

    @Transactional
    public FestivalRegistration register(ParticipantRegistrationRequest request) {
        validate(request);

        Festival festival = festivalService.findById(request.festivalId());
        if (festival == null) {
            throw new IllegalArgumentException("Festival with id " + request.festivalId() + " does not exist");
        }

        String email = request.email().trim();
        Participant participant = participantRepo.findByEmail(email)
                .orElseGet(() -> participantRepo.save(new Participant(
                                request.name().trim(),
                                email,
                                request.phoneNumber() == null || request.phoneNumber().isBlank() ? null : request.phoneNumber().trim())
                ));
        return festivalRegistrationRepo
                .findByParticipant_IdAndFestival_Id(participant.getId(), festival.getId())
                .orElseGet(() -> festivalRegistrationRepo.save(
                        new FestivalRegistration(participant, festival)
                ));
    }

    private void validate(ParticipantRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request is required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.festivalId() == null) {
            throw new IllegalArgumentException("Festival ID is required");
        }
    }
}
