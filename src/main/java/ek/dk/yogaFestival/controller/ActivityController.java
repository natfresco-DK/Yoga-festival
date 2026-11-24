package ek.dk.yogaFestival.controller;

import ek.dk.yogaFestival.dto.ActivityRequest;
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

    @GetMapping("/api/activities")
    public List<Activity> getActivities() {
        return activityService.getAllFestivals();
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