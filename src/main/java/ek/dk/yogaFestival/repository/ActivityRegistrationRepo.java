package ek.dk.yogaFestival.repository;

import ek.dk.yogaFestival.model.ActivityRegistration;
import ek.dk.yogaFestival.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRegistrationRepo extends JpaRepository<ActivityRegistration, Long> {
    boolean existsByFestivalRegistrationIdAndActivityIdAndStatus(
            Long festivalRegistrationId,
            Long activityId,
            RegistrationStatus status
    );
}
