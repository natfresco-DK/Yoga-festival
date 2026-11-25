package ek.dk.yogaFestival.controller;

import ek.dk.yogaFestival.dto.ActivityRequest;
import ek.dk.yogaFestival.dto.ActivityResponse;
import ek.dk.yogaFestival.model.Activity;
import ek.dk.yogaFestival.service.ActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /*
    @GetMapping("/api/activities")
    public List<Activity> getActivities() {
        return activityService.getAllFestivals();
    }

     */

    @GetMapping("/api/activities")
    public List<ActivityResponse> getActivities() {
        return activityService.getAllFestivals().stream()
                .map(activity -> new ActivityResponse(
                        activity.getId(),
                        activity.getName(),
                        activity.getDescription(),
                        activity.getInstructor(),
                        activity.getRoom(),
                        activity.getStartTime(),
                        activity.getEndTime(),
                        activity.getCapacity(),
                        activity.getItemsToBring(),
                        activity.getFestival().getId()))
                .toList();
    }

    @PostMapping("/api/activities")
    public Activity saveActivity(@RequestBody ActivityRequest request) {
        return activityService.saveActivity(request);
    }

    @PutMapping("/api/activities/{id}")
    public Activity updateActivity(@PathVariable Long id, @RequestBody ActivityRequest request) {
        return activityService.updateActivity(id, request);
    }
}