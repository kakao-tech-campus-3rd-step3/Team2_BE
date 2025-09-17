-- =========================================================================================
-- Pullit Local Development Seed Data
-- =========================================================================================
-- This script is executed when the Docker container is first created.
-- It populates the database with initial data for local development and testing.
-- Note: The table schema is managed by Flyway. This script should only contain INSERT statements.
-- Using `ON DUPLICATE KEY UPDATE` to prevent errors if the script is run multiple times.
-- =========================================================================================

-- -----------------------------------------------------
-- Table `member` (5 members)
-- -----------------------------------------------------
INSERT INTO members (id, kakao_id, email, name, status, created_at, updated_at)
VALUES (1, 1000000001, 'test.user@example.com', '테스트유저', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), updated_at=NOW();

INSERT INTO members (id, kakao_id, email, name, status, created_at, updated_at)
VALUES (2, 1000000002, 'alice@example.com', '앨리스', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), updated_at=NOW();

-- -----------------------------------------------------
-- Table `source_folder`
-- -----------------------------------------------------
INSERT INTO source_folder (id, member_id, name, description, color, created_at, updated_at)
VALUES (1, 1, '전체 폴더', '전체폴더이다.', '#4287f5', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), updated_at=NOW();

-- -----------------------------------------------------
-- Table `source`
-- -----------------------------------------------------
INSERT INTO source (id, member_id, source_folder_id, original_name, content_type, file_path, file_size_bytes, status, created_at, updated_at)
VALUES (1, 1, 1, 'PS-2. 배열과 리스트 그리고 벡터, 구간 합.pdf', 'application/pdf', 'learning-sources/2025/09/16/member-1/09756256-564f-4faa-9314-d915a436b2db.pdf', 795608, 'UPLOADED', NOW(), NOW())
ON DUPLICATE KEY UPDATE original_name=VALUES(original_name), updated_at=NOW();