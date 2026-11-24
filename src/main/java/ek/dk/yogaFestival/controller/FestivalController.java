package ek.dk.yogaFestival.controller;

import ek.dk.yogaFestival.model.Festival;
import ek.dk.yogaFestival.service.FestivalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FestivalController {

    private final FestivalService festivalService;

    public FestivalController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @GetMapping("/api/festivals")
    public List<FestivalDto> getFestivals() {
        return festivalService.findAll().stream()
                .map(festival -> new FestivalDto(
                        festival.getId(),
                        festival.getName(),
                        festival.getYear()
                ))
                .toList();
    }

    public record FestivalDto(Long id, String name, int year) {
    }
}
