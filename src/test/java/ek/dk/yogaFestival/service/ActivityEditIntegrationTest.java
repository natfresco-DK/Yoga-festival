package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.dto.ActivityRequest;
import ek.dk.yogaFestival.model.*;
import ek.dk.yogaFestival.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:activity-edit-it;DB_CLOSE_DELAY=-1;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@Transactional
class ActivityEditIntegrationTest {

    @Autowired private ActivityService activityService;
    @Autowired private ActivityRepo activityRepo;
    @Autowired private ActivityRegistrationRepo activityRegistrationRepo;
    @Autowired private FestivalRegistrationRepo festivalRegistrationRepo;
    @Autowired private FestivalRepo festivalRepo;
    @Autowired private ParticipantRepo participantRepo;
    @Autowired private EntityManager entityManager;

    @Test
    void editingActivityPreservesItsIdFestivalAndRegistration() {
        Festival festival = festivalRepo.save(new Festival("Yoga Festival", 2026));
        Participant participant = participantRepo.save(
                new Participant("Tina Tester", "tina@example.com", null)
        );
        FestivalRegistration festivalRegistration = festivalRegistrationRepo.save(
                new FestivalRegistration(participant, festival)
        );

        Activity activity = new Activity(
                null, "Old name", "Old description", "Old instructor", "Old room",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                2, "Yoga mat"
        );
        activity.setFestival(festival);
        activity = activityRepo.save(activity);

        ActivityRegistration registration = activityRegistrationRepo.save(
                new ActivityRegistration(festivalRegistration, activity)
        );

        Long activityId = activity.getId();
        Long registrationId = registration.getId();

        entityManager.flush();
        entityManager.clear();

        ActivityRequest update = new ActivityRequest(
                "Updated name", "Updated description", "New instructor", "New room",
                "Yoga mat", LocalDateTime.of(2026, 6, 1, 11, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0), 3, festival.getId()
        );

        activityService.updateActivity(activityId, update);

        entityManager.flush();
        entityManager.clear();

        Activity reloadedActivity = activityRepo.findById(activityId).orElseThrow();
        ActivityRegistration reloadedRegistration =
                activityRegistrationRepo.findById(registrationId).orElseThrow();

        assertEquals(activityId, reloadedActivity.getId());
        assertEquals(festival.getId(), reloadedActivity.getFestival().getId());
        assertEquals("Updated name", reloadedActivity.getName());
        assertEquals(registrationId, reloadedRegistration.getId());
        assertEquals(activityId, reloadedRegistration.getActivity().getId());
        assertEquals(1L, activityRegistrationRepo.countByActivityIdAndStatus(
                activityId, RegistrationStatus.CONFIRMED
        ));
        assertNotNull(reloadedRegistration.getFestivalRegistration());
    }
}