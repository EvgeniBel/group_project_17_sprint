package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>,
        QuerydslPredicateExecutor<Event>, CustomEventRepository {

    List<Event> findAllByInitiator_IdOrderByEventDateAsc(Long initiatorId, Pageable pageable);

    List<Event> findAllByIdInOrderByIdAsc(List<Long> eventsIds);

    /**
     * ИСПРАВЛЕНО: упрощённый запрос для публичных событий
     */
    @Query("SELECT e FROM Event e " +
            "WHERE e.state = :state " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:text IS NULL OR " +
            "     LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "     LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "ORDER BY e.eventDate ASC")
    List<Event> findPublicEvents(@Param("state") EventState state,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("text") String text,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 Pageable pageable);

    /**
     * ИСПРАВЛЕНО: упрощённый запрос с проверкой available
     */
    @Query("SELECT e FROM Event e " +
            "WHERE e.state = :state " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:text IS NULL OR " +
            "     LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "     LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND (:onlyAvailable = false OR " +
            "     e.participantLimit = 0 OR " +
            "     e.participantLimit > (SELECT COUNT(r) FROM ParticipationRequest r " +
            "                           WHERE r.event.id = e.id AND r.status = 'CONFIRMED')) " +
            "ORDER BY e.eventDate ASC")
    List<Event> findPublicEventsWithAvailableCheck(@Param("state") EventState state,
                                                   @Param("categories") List<Long> categories,
                                                   @Param("paid") Boolean paid,
                                                   @Param("text") String text,
                                                   @Param("rangeStart") LocalDateTime rangeStart,
                                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                                   @Param("onlyAvailable") Boolean onlyAvailable,
                                                   Pageable pageable);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequests(@Param("eventId") Long eventId);

    boolean existsByIdAndState(Long id, EventState state);

    List<Event> findAllByState(EventState state, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "     LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Event> searchByText(@Param("text") String text, Pageable pageable);

    List<Event> findAllByCategory_IdInAndState(List<Long> categories, EventState state, Pageable pageable);

    List<Event> findAllByEventDateBetweenAndState(LocalDateTime start, LocalDateTime end, EventState state, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (e.participantLimit = 0 OR " +
            "     e.participantLimit > (SELECT COUNT(r) FROM ParticipationRequest r " +
            "                           WHERE r.event.id = e.id AND r.status = 'CONFIRMED'))")
    List<Event> findAllAvailable(Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e WHERE e.category.id = :categoryId")
    boolean existsByCategoryId(@Param("categoryId") Long categoryId);
}