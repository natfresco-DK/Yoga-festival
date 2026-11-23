package ek.dk.yogaFestival.dto;

import java.time.LocalDateTime;

public record ActivityRequest(
        String name,
        String description,
        String instructor,
        String room,
        String itemsToBring,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer capacity,
        Long festivalId
) {}