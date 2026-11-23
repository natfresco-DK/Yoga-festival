package ek.dk.yogaFestival.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_registrations")
public class ActivityRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_registration_id", nullable = false)
    private FestivalRegistration festivalRegistration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    protected ActivityRegistration() {
    }

    public ActivityRegistration(FestivalRegistration festivalRegistration, Activity activity) {
        this.festivalRegistration = festivalRegistration;
        this.activity = activity;
        this.registrationDate = LocalDateTime.now();
        this.status = RegistrationStatus.CONFIRMED;
    }

    public Long getId() {
        return id;
    }

    public FestivalRegistration getFestivalRegistration() {
        return festivalRegistration;
    }

    public Activity getActivity() {
        return activity;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
}
