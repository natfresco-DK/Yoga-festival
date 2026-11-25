package ek.dk.yogaFestival.repository;

import ek.dk.yogaFestival.model.FestivalRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FestivalRegistrationRepo extends JpaRepository<FestivalRegistration, Long> {
    Optional<FestivalRegistration> findByParticipant_IdAndFestival_Id(
            Long participantId,
            Long festivalId
    );
}
