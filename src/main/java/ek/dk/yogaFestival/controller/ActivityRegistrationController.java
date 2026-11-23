package ek.dk.yogaFestival.controller;

import ek.dk.yogaFestival.dto.ActivityRegistrationRequest;
import ek.dk.yogaFestival.model.ActivityRegistration;
import ek.dk.yogaFestival.service.ActivityRegistrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ActivityRegistrationController {

    private final ActivityRegistrationService registrationService;

    public ActivityRegistrationController(ActivityRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/api/activity-registrations")
    public List<ActivityRegistrationDto> register(@Valid @RequestBody ActivityRegistrationRequest request) {
        return registrationService.register(request).stream()
                .map(registration -> new ActivityRegistrationDto(
                        registration.getId(),
                        registration.getActivity().getId(),
                        registration.getStatus().name()))
                .toList();
    }

    public record ActivityRegistrationDto(
            Long id,
            Long activityId,
            String status
    ) {
    }
}
