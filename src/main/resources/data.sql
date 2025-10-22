SET FOREIGN_KEY_CHECKS=0;

INSERT INTO member
(id, kakao_id, email, name, refresh_token, status, created_at, updated_at)
VALUES
    (1, 123456789, 'test@example.com', '홍길동', 'sample_refresh_token', 'ACTIVE', NOW(), NOW());
