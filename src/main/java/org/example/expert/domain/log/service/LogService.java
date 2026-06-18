package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    //트랜잭션이 롤백되지않기위해 독립적으로 로그저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(Long todoId, Long requestUserId, Long managerUserId, String status, String message) {
        logRepository.save(new Log(todoId, requestUserId, managerUserId, status, message));
    }
}
