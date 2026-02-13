package com.erp.domain.calendar.repository;

import com.erp.domain.calendar.entity.CalendarEvent;
import com.erp.domain.calendar.entity.CalendarScopeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    @Query("SELECT e FROM CalendarEvent e " +
           "LEFT JOIN FETCH e.kindergarten k " +
           "LEFT JOIN FETCH e.classroom c " +
           "LEFT JOIN FETCH e.creator m " +
           "WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<CalendarEvent> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    @Query("SELECT e FROM CalendarEvent e " +
           "LEFT JOIN FETCH e.kindergarten k " +
           "LEFT JOIN FETCH e.classroom c " +
           "LEFT JOIN FETCH e.creator m " +
           "WHERE e.scopeType = :scopeType AND k.id = :kindergartenId " +
           "AND e.deletedAt IS NULL " +
           "AND e.startDateTime <= :endDateTime AND e.endDateTime >= :startDateTime " +
           "ORDER BY e.startDateTime ASC")
    List<CalendarEvent> findKindergartenEvents(
            @Param("kindergartenId") Long kindergartenId,
            @Param("scopeType") CalendarScopeType scopeType,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT e FROM CalendarEvent e " +
           "LEFT JOIN FETCH e.kindergarten k " +
           "LEFT JOIN FETCH e.classroom c " +
           "LEFT JOIN FETCH e.creator m " +
           "WHERE e.scopeType = :scopeType AND c.id IN :classroomIds " +
           "AND e.deletedAt IS NULL " +
           "AND e.startDateTime <= :endDateTime AND e.endDateTime >= :startDateTime " +
           "ORDER BY e.startDateTime ASC")
    List<CalendarEvent> findClassroomEvents(
            @Param("classroomIds") List<Long> classroomIds,
            @Param("scopeType") CalendarScopeType scopeType,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT e FROM CalendarEvent e " +
           "LEFT JOIN FETCH e.kindergarten k " +
           "LEFT JOIN FETCH e.classroom c " +
           "LEFT JOIN FETCH e.creator m " +
           "WHERE e.scopeType = :scopeType AND m.id = :memberId " +
           "AND e.deletedAt IS NULL " +
           "AND e.startDateTime <= :endDateTime AND e.endDateTime >= :startDateTime " +
           "ORDER BY e.startDateTime ASC")
    List<CalendarEvent> findPersonalEvents(
            @Param("memberId") Long memberId,
            @Param("scopeType") CalendarScopeType scopeType,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
