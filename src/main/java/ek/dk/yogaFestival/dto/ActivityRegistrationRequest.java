package ek.dk.yogaFestival.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ActivityRegistrationRequest(
        @NotNull(message = "Festival registration ID is required")
        Long festivalRegistrationId,

        @NotEmpty(message = "At least one activity must be selected")
        List<@NotNull(message = "Activity ID is required") Long> activityIds
) {
}
