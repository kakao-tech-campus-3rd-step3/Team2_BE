-- 이 스크립트는 컨테이너가 처음 생성될 때 실행됩니다.
-- 로컬 개발에 필요한 초기 데이터를 여기에 추가할 수 있습니다.

-- 참고: 테이블 스키마는 Flyway가 관리하므로, 여기서는 데이터 삽입(INSERT)만 수행합니다.
INSERT INTO member (id, kakao_id, email, name, status, created_at, updated_at)
VALUES (1, 123456789, 'test@example.com', '테스트유저', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE kakao_id=VALUES(kakao_id), email=VALUES(email), name=VALUES(name), status=VALUES(status), updated_at=NOW();

INSERT INTO member (id, kakao_id, email, name, status, created_at, updated_at)
VALUES (2, 987654321, 'another@example.com', '어나더유저', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE kakao_id=VALUES(kakao_id), email=VALUES(email), name=VALUES(name), status=VALUES(status), updated_at=NOW();
