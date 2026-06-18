package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long todoId;

    private Long requestUserId;

    private Long managerUserId;

    private String status;

    private String message;
    //로그생성시간은 저장
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Log(Long todoId, Long requestUserId, Long managerUserId, String status, String message) {
        this.todoId = todoId;
        this.requestUserId = requestUserId;
        this.managerUserId = managerUserId;
        this.status = status;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
