package ek.dk.yogaFestival.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ActivityRegistrationRequest(
        @NotNull(message = "Festival registration ID is required")
        Long festivalRegistrationId,

        @NotEmpty(message = "At least one activity must be selected")
        @Size(max = 10, message = "Maximum 10 activities can be selected")
        List<@NotNull(message = "Activity ID is required") Long> activityIds
) {
}
