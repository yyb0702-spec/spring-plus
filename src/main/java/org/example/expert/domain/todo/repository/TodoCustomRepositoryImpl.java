package org.example.expert.domain.todo.repository;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.Todo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustromRepository {

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
}
