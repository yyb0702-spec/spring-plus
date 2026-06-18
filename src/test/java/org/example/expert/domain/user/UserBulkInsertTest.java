package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class UserBulkInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int TOTAL = 1_000_000;
    private static final int BATCH_SIZE = 1_000;

    @Test
    void 유저_100만건_bulk_insert() {
        String sql = "INSERT INTO users (email, password, nickname, user_role, created_at, modified_at) VALUES (?, ?, ?, ?, NOW(), NOW())";

        long start = System.currentTimeMillis();
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= TOTAL; i++) {
            String uid = UUID.randomUUID().toString().replace("-", "");
            String email = "user_" + uid + "@test.com";
            String nickname = generateNickname();
            String password = "password123";
            String role = "USER";

            batch.add(new Object[]{email, password, nickname, role});

            if (batch.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();

                if (i % 100_000 == 0) {
                    System.out.println(i + "건 삽입 완료");
                }
            }
        }

        // 남은 데이터 처리
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
        }

        long end = System.currentTimeMillis();
        System.out.println("총 소요 시간: " + (end - start) / 1000 + "초");
    }

    // 중복 최소화: 형용사 + 명사 + 고유번호 조합
    private String generateNickname() {
        String[] adjectives = {"따듯한", "추운", "배고픈", "배부른", "재미있는", "재미없는", "검정", "빨강", "파랑", "노랑",
                "주황", "친절한", "시끄러운", "화난", "약한", "강한", "보라", "듬직한", "연약한", "아픈"};
        String[] nouns = {"개", "고양이", "개구리", "기러기", "황조롱이", "쥐", "소", "호랑이", "토끼", "용",
                "돼지", "말", "양", "원숭이", "닭", "개", "돼지", "지렁이", "팬더", "햄스터"};

        String adj = adjectives[(int)(Math.random() * adjectives.length)];
        String noun = nouns[(int)(Math.random() * nouns.length)];
        String uid = UUID.randomUUID().toString().substring(0, 6);
        return adj + noun + "_" + uid;
    }
}
