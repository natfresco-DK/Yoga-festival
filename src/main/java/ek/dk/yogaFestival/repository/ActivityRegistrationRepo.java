package ek.dk.yogaFestival.repository;

import ek.dk.yogaFestival.model.ActivityRegistration;
import ek.dk.yogaFestival.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRegistrationRepo extends JpaRepository<ActivityRegistration, Long> {
    boolean existsByFestivalRegistration_Participant_IdAndActivity_IdAndStatus(
            Long participantId,
            Long activityId,
            RegistrationStatus status
    );

    long countByActivityIdAndStatus(
            Long activityId,
            RegistrationStatus status
    );
}
