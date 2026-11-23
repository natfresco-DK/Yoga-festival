package ek.dk.yogaFestival.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActivityPageController {

    @GetMapping("/activities")
    public String activitiesPage() {
        return "activities";
    }
}
