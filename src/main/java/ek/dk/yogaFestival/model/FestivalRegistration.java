package ek.dk.yogaFestival.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "festival_registrations")
public class FestivalRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    @OneToMany(mappedBy = "festivalRegistration")
    private List<ActivityRegistration> activityRegistrations = new ArrayList<>();

    protected FestivalRegistration() {
    }

    public FestivalRegistration(Participant participant, Festival festival) {
        this.participant = participant;
        this.festival = festival;
        this.registrationDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Participant getParticipant() {
        return participant;
    }

    public Festival getFestival() {
        return festival;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public List<ActivityRegistration> getActivityRegistrations() {
        return activityRegistrations;
    }
}
