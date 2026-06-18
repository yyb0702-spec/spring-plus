package org.example.expert.domain.todo.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TodoWeatherRequest {
    private String weather;
    private LocalDateTime startDate; // modifiedAt 기준
    private LocalDateTime endDate;
}
