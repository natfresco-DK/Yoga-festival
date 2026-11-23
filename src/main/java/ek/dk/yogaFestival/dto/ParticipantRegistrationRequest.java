package ek.dk.yogaFestival.dto;

public record ParticipantRegistrationRequest(
        String name,
        String email,
        String phoneNumber,
        Long festivalId
) {
}
