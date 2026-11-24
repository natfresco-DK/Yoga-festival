package ek.dk.yogaFestival.model;

import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.NotNull;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activities")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String instructor;
    private String room;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer capacity;

    private String itemsToBring;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @OneToMany(mappedBy = "activity")
    private List<ActivityRegistration> registrations = new ArrayList<>();

    public Activity() {}

    public Activity(Long id, String name, String description, String instructor, String room, LocalDateTime startTime, LocalDateTime endTime, Integer capacity, String itemsToBring) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.instructor = instructor;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.itemsToBring = itemsToBring;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getItemsToBring() {
        return itemsToBring;
    }

    public void setItemsToBring(String itemsToBring) {
        this.itemsToBring = itemsToBring;
    }

    public Festival getFestival() {
    return festival;
}

public void setFestival(Festival festival) {
    this.festival = festival;
}

    public List<ActivityRegistration> getRegistrations() {
        return registrations;
    }
}
