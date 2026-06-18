package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.request.TodoWeatherRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    @Transactional(readOnly = true)
    public Page<TodoResponse> getTodos(int page, int size, TodoWeatherRequest request) {
        validatePage(page, size);
        validateWeatherRequest(request);

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Todo> todos = todoRepository.searchTodoWithWeather(
                request.getWeather(),
                request.getStartDate(),
                request.getEndDate(),
                pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    @Transactional(readOnly = true)
    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<TodoSearchResponse> searchTodos(int page, int size, TodoSearchRequest request) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        return todoRepository.searchTodos(request, pageable);
    }

    private void validatePage(int page, int size) {
        if (page < 1) {
            throw new InvalidRequestException("페이지 번호는 1 이상이어야 합니다.");
        }
        if (size < 1 || size > 100) {
            throw new InvalidRequestException("페이지 사이즈는 1 이상 100 이하여야 합니다.");
        }
    }

    private void validateWeatherRequest(TodoWeatherRequest req) {
        if (req.getStartDate() != null && req.getEndDate() != null) {
            if (req.getStartDate().isAfter(req.getEndDate())) {
                throw new InvalidRequestException("시작일이 종료일보다 늦을 수 없습니다.");
            }
        }
        if (req.getWeather() != null && req.getWeather().isBlank()) {
            throw new InvalidRequestException("weather 값이 올바르지 않습니다.");
        }
    }
}
