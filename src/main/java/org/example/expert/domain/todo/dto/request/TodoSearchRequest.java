package org.example.expert.domain.todo.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TodoSearchRequest {
    //날씨 검색용 (GET /todos)
    private String weather;

    //공통
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    //검색용
    private String title;
    private String nickname;
}
