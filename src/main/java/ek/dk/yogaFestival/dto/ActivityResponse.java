package ek.dk.yogaFestival.dto;

import java.time.LocalDateTime;

public record ActivityResponse(
        Long id,
        String name,
        String description,
        String instructor,
        String room,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer capacity,
        String itemsToBring,
        Long festivalId,
        Integer availablePlaces
) {
}