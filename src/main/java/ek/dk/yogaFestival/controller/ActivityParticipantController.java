package ek.dk.yogaFestival.controller;

import ek.dk.yogaFestival.dto.ActivityParticipantResponse;
import ek.dk.yogaFestival.service.ActivityRegistrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ActivityParticipantController {

    private final ActivityRegistrationService registrationService;

    public ActivityParticipantController(ActivityRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/api/activities/{activityId}/participants")
    public List<ActivityParticipantResponse> getConfirmedParticipants(@PathVariable Long activityId) {
        return registrationService.getConfirmedParticipants(activityId);
    }
}
