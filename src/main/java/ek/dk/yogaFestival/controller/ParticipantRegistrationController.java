package ek.dk.yogaFestival.controller;

import ek.dk.yogaFestival.dto.ParticipantRegistrationRequest;
import ek.dk.yogaFestival.dto.ParticipantRegistrationResponse;
import ek.dk.yogaFestival.model.FestivalRegistration;
import ek.dk.yogaFestival.service.ParticipantRegistrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParticipantRegistrationController {

    private final ParticipantRegistrationService registrationService;

    public ParticipantRegistrationController(ParticipantRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/api/participants/registrations")
    public ParticipantRegistrationResponse register(@Valid @RequestBody ParticipantRegistrationRequest request) {
        FestivalRegistration registration = registrationService.register(request);
        return new ParticipantRegistrationResponse(registration.getId());
    }
}
