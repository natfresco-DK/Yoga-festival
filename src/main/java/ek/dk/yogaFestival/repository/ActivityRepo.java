package ek.dk.yogaFestival.repository;

import ek.dk.yogaFestival.model.Activity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityRepo extends JpaRepository<Activity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select activity from Activity activity where activity.id = :id")
    Optional<Activity> findByIdForUpdate(@Param("id") Long id);
}
