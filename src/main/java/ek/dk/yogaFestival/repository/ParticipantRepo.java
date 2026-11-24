package ek.dk.yogaFestival.repository;

import ek.dk.yogaFestival.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepo extends JpaRepository<Participant, Long> {
    Optional<Participant> findByEmail(String email);
}
