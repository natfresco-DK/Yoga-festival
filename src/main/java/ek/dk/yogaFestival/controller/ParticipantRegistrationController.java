package ek.dk.yogaFestival.controller;

import ek.dk.yogaFestival.dto.ParticipantRegistrationRequest;
import ek.dk.yogaFestival.model.FestivalRegistration;
import ek.dk.yogaFestival.service.ParticipantRegistrationService;
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
    public FestivalRegistration register(@RequestBody ParticipantRegistrationRequest request) {
        return registrationService.register(request);
    }
}
