package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.model.Festival;
import ek.dk.yogaFestival.repository.FestivalRepo;
import org.springframework.stereotype.Service;

@Service
public class FestivalService {
    private final FestivalRepo festivalRepo;

    public FestivalService(FestivalRepo festivalRepo) {
        this.festivalRepo = festivalRepo;
    }

    public Festival findById(Long id) {
    return festivalRepo.findById(id).orElse(null);
    }
}