package ek.dk.yogaFestival.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ParticipantRegistrationRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must contain at most 100 characters")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must contain at most 255 characters")
        String email,
        @Size(max = 8, message = "Phone number must contain at most 8 characters")
        String phoneNumber,
        @NotNull(message = "Festival ID is required")
        Long festivalId
) {
}
