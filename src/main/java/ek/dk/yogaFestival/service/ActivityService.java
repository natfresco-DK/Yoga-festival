package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.dto.ActivityRequest;
import ek.dk.yogaFestival.model.Activity;
import ek.dk.yogaFestival.model.Festival;
import ek.dk.yogaFestival.model.RegistrationStatus;
import ek.dk.yogaFestival.repository.ActivityRegistrationRepo;
import ek.dk.yogaFestival.repository.ActivityRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityService {
    private final ActivityRepo activityRepo;
    private final FestivalService festivalService;
    private final ActivityRegistrationRepo activityRegistrationRepo;

    public ActivityService(ActivityRepo activityRepo, FestivalService festivalService, ActivityRegistrationRepo activityRegistrationRepo) {
        this.activityRepo = activityRepo;
        this.festivalService = festivalService;
        this.activityRegistrationRepo = activityRegistrationRepo;
    }

    public List<Activity> getAllFestivals() {
        return activityRepo.findAll();
    }

    public long getConfirmedRegistrationCount(Long activityId) {
        return activityRegistrationRepo.countByActivityIdAndStatus(
                activityId,
                RegistrationStatus.CONFIRMED
        );
    }

    public Activity saveActivity(ActivityRequest request) {
        validateActivityRequest(request);

        Festival festival = festivalService.findById(request.festivalId());
        if (festival == null) {
            throw new IllegalArgumentException("Festival with id " + request.festivalId() + " does not exist");
        }

        Activity activity = new Activity();
        activity.setName(request.name());
        activity.setDescription(request.description());
        activity.setInstructor(request.instructor());
        activity.setRoom(request.room());
        activity.setItemsToBring(request.itemsToBring());
        activity.setStartTime(request.startTime());
        activity.setEndTime(request.endTime());
        activity.setCapacity(request.capacity());
        activity.setFestival(festival);

        return activityRepo.save(activity);
    }

    @Transactional
    public Activity updateActivity(Long id, ActivityRequest request) {
        validateActivityRequest(request);

        Activity existingActivity = activityRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity with id " + id + " does not exist"));

        long confirmedRegistrations =
                activityRegistrationRepo.countByActivityIdAndStatus(
                        id,
                        RegistrationStatus.CONFIRMED
                );

        if (request.capacity() < confirmedRegistrations) {
            throw new IllegalArgumentException(
                    "Capacity cannot be lower than the number of confirmed registrations"
            );
        }

        existingActivity.setName(request.name());
        existingActivity.setDescription(request.description());
        existingActivity.setInstructor(request.instructor());
        existingActivity.setRoom(request.room());
        existingActivity.setItemsToBring(request.itemsToBring());
        existingActivity.setStartTime(request.startTime());
        existingActivity.setEndTime(request.endTime());
        existingActivity.setCapacity(request.capacity());

        return activityRepo.save(existingActivity);
    }

    public int getAvailablePlaces(Activity activity) {
        long confirmedRegistrations = activityRegistrationRepo.countByActivityIdAndStatus(
                activity.getId(),
                RegistrationStatus.CONFIRMED
        );
        return (int) (activity.getCapacity() - confirmedRegistrations);
    }

    private void validateActivityRequest(ActivityRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Activity request is required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.instructor() == null || request.instructor().isBlank()) {
            throw new IllegalArgumentException("Instructor is required");
        }
        if (request.startTime() == null) {
            throw new IllegalArgumentException("Start time is required");
        }
        if (request.endTime() == null) {
            throw new IllegalArgumentException("End time is required");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (request.capacity() == null || request.capacity() < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1");
        }
        if (request.festivalId() == null) {
            throw new IllegalArgumentException("Festival ID is required");
        }
        if (request.room() == null || request.room().isBlank()) {
            throw new IllegalArgumentException("Room is required");
        }
    }
}
