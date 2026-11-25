package ek.dk.yogaFestival.service;

import ek.dk.yogaFestival.dto.ActivityRegistrationRequest;
import ek.dk.yogaFestival.model.Activity;
import ek.dk.yogaFestival.model.Festival;
import ek.dk.yogaFestival.model.FestivalRegistration;
import ek.dk.yogaFestival.model.Participant;
import ek.dk.yogaFestival.model.RegistrationStatus;
import ek.dk.yogaFestival.repository.ActivityRegistrationRepo;
import ek.dk.yogaFestival.repository.ActivityRepo;
import ek.dk.yogaFestival.repository.FestivalRegistrationRepo;
import ek.dk.yogaFestival.repository.FestivalRepo;
import ek.dk.yogaFestival.repository.ParticipantRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:activity-registration-concurrency;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class ConcurrentActivityRegistrationIntegrationTest {

    @Autowired
    private ActivityRegistrationService activityRegistrationService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityRegistrationRepo activityRegistrationRepo;

    @Autowired
    private ActivityRepo activityRepo;

    @Autowired
    private FestivalRegistrationRepo festivalRegistrationRepo;

    @Autowired
    private FestivalRepo festivalRepo;

    @Autowired
    private ParticipantRepo participantRepo;

    @Test
    void confirmedRegistrationReducesAvailablePlacesByOne() {
        Festival festival = festivalRepo.save(new Festival("Availability Festival", 2026));
        FestivalRegistration festivalRegistration = createFestivalRegistration(festival, "availability");

        Activity activity = new Activity(
                null,
                "Available Yoga",
                "Availability check",
                "Instructor",
                "Room 1",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                2,
                null
        );
        activity.setFestival(festival);
        activity = activityRepo.save(activity);

        assertEquals(2, activityService.getAvailablePlaces(activity));

        activityRegistrationService.register(
                new ActivityRegistrationRequest(festivalRegistration.getId(), List.of(activity.getId()))
        );

        assertEquals(1, activityService.getAvailablePlaces(activity));
    }

    @Test
    void simultaneousRegistrationsCannotOverbookLastPlace() throws Exception {
        Festival festival = festivalRepo.save(new Festival("Concurrency Festival", 2026));
        FestivalRegistration firstRegistration = createFestivalRegistration(festival, "first");
        FestivalRegistration secondRegistration = createFestivalRegistration(festival, "second");

        Activity activity = new Activity(
                null,
                "Limited Yoga",
                "One place only",
                "Instructor",
                "Room 1",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                1,
                null
        );
        activity.setFestival(festival);
        activity = activityRepo.save(activity);

        CyclicBarrier startTogether = new CyclicBarrier(2);
        Long activityId = activity.getId();
        Callable<Boolean> firstAttempt = attemptRegistration(startTogether, firstRegistration.getId(), activityId);
        Callable<Boolean> secondAttempt = attemptRegistration(startTogether, secondRegistration.getId(), activityId);

        var executor = Executors.newFixedThreadPool(2);
        try {
            var firstResult = executor.submit(firstAttempt);
            var secondResult = executor.submit(secondAttempt);

            int successfulRegistrations =
                    (firstResult.get(15, TimeUnit.SECONDS) ? 1 : 0)
                            + (secondResult.get(15, TimeUnit.SECONDS) ? 1 : 0);

            assertEquals(1, successfulRegistrations);
            assertEquals(1L, activityRegistrationRepo.countByActivityIdAndStatus(
                    activityId,
                    RegistrationStatus.CONFIRMED
            ));
        } finally {
            executor.shutdownNow();
        }
    }

    private FestivalRegistration createFestivalRegistration(Festival festival, String suffix) {
        Participant participant = participantRepo.save(new Participant(
                "Participant " + suffix,
                suffix + "@example.com",
                null
        ));
        return festivalRegistrationRepo.save(new FestivalRegistration(participant, festival));
    }

    private Callable<Boolean> attemptRegistration(
            CyclicBarrier startTogether,
            Long festivalRegistrationId,
            Long activityId
    ) {
        return () -> {
            startTogether.await(10, TimeUnit.SECONDS);
            try {
                activityRegistrationService.register(
                        new ActivityRegistrationRequest(festivalRegistrationId, List.of(activityId))
                );
                return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        };
    }
}
