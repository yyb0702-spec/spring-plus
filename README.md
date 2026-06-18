# SPRING PLUS

## Challenge 13. 대용량 데이터 처리

---

### 1. User Bulk Insert

대용량 데이터 처리 실습을 위해 `JdbcTemplate.batchUpdate()`를 활용하여 `users` 테이블에 유저 1,000,000건을 삽입했다.

| 항목 | 결과                                     |
|------|----------------------------------------|
| 생성 데이터 수 | 1,000,000건                             |
| 생성 방식 | JDBC Bulk Insert                       |
| Batch Size | 1,000                                  |
| 닉네임 생성 방식 | 형용사 + 명사 + UUID 6자리 (예: `검정토끼_a3f9c2`) |

**실행 결과**
```
100000건 삽입 완료
200000건 삽입 완료
...
1000000건 삽입 완료
총 소요 시간: 70초
BUILD SUCCESSFUL
```

---

### 2. 닉네임 검색 성능 개선

생성된 데이터를 대상으로 닉네임 정확 일치 조회 속도를 단계별로 측정했다.

**검색 API**
```
GET /users?nickname={nickname}
```

---

### 3. 단계별 성능 비교

| 단계 | 방법 | 조회 시간 | EXPLAIN type | key | rows |
|------|------|-----------|--------------|-----|------|
| 1 | 인덱스 없음 | 1,670 ms | ALL | NULL | 1,014,254 |
| 2 | 단순 인덱스 | 427 ms | ref | idx_users_nickname | 1 |
| 3 | 커버링 인덱스 | 427 ms | ref | idx_users_nickname_cover | 1 |
| 4 | 캐시 미스 (첫 요청) | 1,177 ms | ref | idx_users_nickname | 1 |
| 5 | 캐시 히트 (재요청) | 72 ms | - | - | - |

---

### 4. 단계별 개선 내용

#### 1단계 — 인덱스 없음 (baseline)

인덱스가 없는 상태에서 닉네임으로 조회하면 MySQL이 테이블 전체를 스캔한다.

```
EXPLAIN type = ALL, key = NULL, rows = 1,014,254
조회 시간: 1,670 ms
```

100만 건을 전부 확인하기 때문에 가장 느리다.

---

#### 2단계 — 단순 인덱스 추가

`nickname` 컬럼에 인덱스를 추가했다.

```java
@Table(name = "users", indexes = {
    @Index(name = "idx_users_nickname", columnList = "nickname")
})
```

```
EXPLAIN type = ref, key = idx_users_nickname, rows = 1
조회 시간: 427 ms
```

풀스캔에서 인덱스 스캔으로 변경되어 **약 3.9배 빠르다.**
다만 인덱스로 행을 찾은 후 실제 데이터를 가져오기 위해 테이블에 한 번 더 접근한다.

---

#### 3단계 — 커버링 인덱스

API 응답에 필요한 `id`, `email` 컬럼을 인덱스에 포함시켜 테이블 본체 접근을 제거했다.

```java
@Table(name = "users", indexes = {
    @Index(name = "idx_users_nickname_cover", columnList = "nickname, id, email")
})
```

아래 쿼리로 커버링 인덱스 적용 여부를 확인할 수 있다.

```sql
EXPLAIN SELECT id, email, nickname FROM users WHERE nickname = '검정토끼_a3f9c2';
-- Extra = Using index 이면 커버링 인덱스 적용된 것
```

```
EXPLAIN type = ref, key = idx_users_nickname_cover, rows = 1, Extra = Using index
조회 시간: 427 ms
```

단순 인덱스와 속도 차이가 거의 없었던 이유는 `rows = 1` — 닉네임 exact match 조회라 결과가 1건뿐이기 때문이다.
단순 인덱스도 테이블 본체에 **딱 1번만** 접근하면 끝나므로 커버링 인덱스의 이점이 거의 나타나지 않는다.
커버링 인덱스는 **결과 rows가 많을수록** (예: LIKE '%검정%' 범위 조회) 효과가 극대화된다.

---

#### 4단계 — Caffeine 캐시 적용

동일한 닉네임으로 반복 조회 시 DB를 거치지 않고 로컬 캐시에서 바로 반환하도록 했다.

```java
@Cacheable(value = "users", key = "#nickname")
public UserResponse getUserByNickname(String nickname) { ... }
```

```
1회차 (캐시 미스): 1,177 ms  → DB 조회 발생
2회차 (캐시 히트):    72 ms  → DB 우회, 캐시에서 반환
```

캐시 히트 시 캐시 미스 대비 **약 16배 빠르다.**

---

### 5. 성능 개선 요약

```
인덱스 없음:    1,670 ms  (baseline)
단순 인덱스:      427 ms  (3.9배 개선)
캐시 히트:         72 ms  (23.2배 개선)
```

인덱스 추가만으로도 큰 효과가 있었으며, 반복 조회가 많은 환경에서는 캐시가 가장 큰 성능 향상을 가져온다.
