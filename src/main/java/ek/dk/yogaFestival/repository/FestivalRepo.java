package ek.dk.yogaFestival.repository;

import ek.dk.yogaFestival.model.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FestivalRepo extends JpaRepository<Festival, Long> {
}
