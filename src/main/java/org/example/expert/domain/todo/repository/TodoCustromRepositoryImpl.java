package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class TodoCustromRepositoryImpl implements TodoCustromRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(
                queryFactory
                        .select(todo)
                        .from(todo)
                        .leftJoin(todo.user, user)
                        .fetchJoin()
                        .where(todo.id.eq(todoId))
                        .fetchOne()
        );
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(TodoSearchRequest request, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            builder.and(todo.title.contains(request.getTitle()));
        }

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            builder.and(user.nickname.contains(request.getNickname()));
        }

        if (request.getStartDate() != null && request.getEndDate() != null) {
            builder.and(todo.createdAt.between(request.getStartDate(), request.getEndDate()));
        } else if (request.getStartDate() != null) {
            builder.and(todo.createdAt.goe(request.getStartDate()));
        } else if (request.getEndDate() != null) {
            builder.and(todo.createdAt.loe(request.getEndDate()));
        }

        //실제 데이터 조회
        List<TodoSearchResponse> content = queryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        manager.count(),
                        comment.count()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(builder)
                .groupBy(todo.id, todo.title)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //전체 카운트 조회
        Long total = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(builder)
                .fetchOne();

        //total이 null일 경우 NPE 발생 방지
        if (total == null) {
            total = 0L;
        }

        //페이지객체로 변환
        return new PageImpl<>(content, pageable, total);
    }
}
