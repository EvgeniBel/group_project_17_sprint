package ru.practicum.ewm.model.request;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@Table(name = "request")
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
