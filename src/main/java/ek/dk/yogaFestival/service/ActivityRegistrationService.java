package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.dto.ActivityRegistrationRequest;
import ek.dk.yogaFestival.model.Activity;
import ek.dk.yogaFestival.model.ActivityRegistration;
import ek.dk.yogaFestival.model.FestivalRegistration;
import ek.dk.yogaFestival.model.RegistrationStatus;
import ek.dk.yogaFestival.repository.ActivityRegistrationRepo;
import ek.dk.yogaFestival.repository.ActivityRepo;
import ek.dk.yogaFestival.repository.FestivalRegistrationRepo;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ActivityRegistrationService {

    private final FestivalRegistrationRepo festivalRegistrationRepo;
    private final ActivityRepo activityRepo;
    private final ActivityRegistrationRepo activityRegistrationRepo;

    public ActivityRegistrationService(FestivalRegistrationRepo festivalRegistrationRepo, ActivityRepo activityRepo, ActivityRegistrationRepo activityRegistrationRepo) {
        this.festivalRegistrationRepo = festivalRegistrationRepo;
        this.activityRepo = activityRepo;
        this.activityRegistrationRepo = activityRegistrationRepo;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<ActivityRegistration> register(ActivityRegistrationRequest request) {
        validate(request);

        FestivalRegistration festivalRegistration = festivalRegistrationRepo
                .findById(request.festivalRegistrationId())
                .orElseThrow(() -> new IllegalArgumentException("Festival registration with id "
                        + request.festivalRegistrationId() + " does not exist"));
        List<ActivityRegistration> registrations = new ArrayList<>();

        for (Long activityId : request.activityIds().stream().sorted().toList()) {
            Activity activity = activityRepo.findByIdForUpdate(activityId)
                    .orElseThrow(() -> new IllegalArgumentException("Activity with id " + activityId + " does not exist"));

            if (!activity.getFestival().getId().equals(festivalRegistration.getFestival().getId())) {
                throw new IllegalArgumentException("Activity " + activityId + " does not belong to the registered festival");
            }

            if (activityRegistrationRepo.existsByFestivalRegistration_Participant_IdAndActivity_IdAndStatus(
                    festivalRegistration.getParticipant().getId(),
                    activityId,
                    RegistrationStatus.CONFIRMED
            )) {
                throw new IllegalArgumentException(
                        "Participant is already registered for activity " + activityId
                );
            }

            long confirmedRegistrations = activityRegistrationRepo.countByActivityIdAndStatus(
                    activityId,
                    RegistrationStatus.CONFIRMED
            );
            if (confirmedRegistrations >= activity.getCapacity()) {
                throw new IllegalArgumentException(
                        "Activity " + activityId + " is full"
                );
            }

            registrations.add(activityRegistrationRepo.save(new ActivityRegistration(festivalRegistration, activity)));
        }
        return registrations;
    }



    private void validate(ActivityRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Activity registration request is required");
        }
        if (request.festivalRegistrationId() == null) {
            throw new IllegalArgumentException("Festival registration ID is required");
        }
        if (request.activityIds() == null || request.activityIds().isEmpty()) {
            throw new IllegalArgumentException("At least one activity must be selected");
        }
        Set<Long> uniqueActivityIds = new HashSet<>(request.activityIds());
        if (uniqueActivityIds.size() != request.activityIds().size()) {
            throw new IllegalArgumentException("Duplicate activity IDs are not allowed");
        }
    }
}
