package ek.dk.yogaFestival.repository;

import ek.dk.yogaFestival.model.FestivalRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalRegistrationRepo extends JpaRepository<FestivalRegistration, Long> {
}
