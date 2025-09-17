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
INSERT INTO member (id, kakao_id, email, name, status, created_at, updated_at)
VALUES (1, 1000000001, 'test.user@example.com', '테스트유저', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), updated_at=NOW();

INSERT INTO member (id, kakao_id, email, name, status, created_at, updated_at)
VALUES (2, 1000000002, 'alice@example.com', '앨리스', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), updated_at=NOW();

INSERT INTO member (id, kakao_id, email, name, status, created_at, updated_at)
VALUES (3, 1000000003, 'bob@example.com', '밥', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), updated_at=NOW();

INSERT INTO member (id, kakao_id, email, name, status, created_at, updated_at)
VALUES (4, 1000000004, 'charlie@example.com', '찰리', 'INACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), updated_at=NOW();

INSERT INTO member (id, kakao_id, email, name, status, created_at, updated_at)
VALUES (5, 1000000005, 'david@example.com', '데이빗', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), updated_at=NOW();


-- -----------------------------------------------------
-- Table `source_folder`
-- -----------------------------------------------------
INSERT INTO source_folder (id, member_id, name, description, color, created_at, updated_at)
VALUES (1, 1, '자바 스터디', '자바 기초부터 심화까지', '#4287f5', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), updated_at=NOW();

INSERT INTO source_folder (id, member_id, name, description, color, created_at, updated_at)
VALUES (2, 2, '알고리즘 문제풀이', '코딩 테스트 대비', '#f5a442', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), updated_at=NOW();

INSERT INTO source_folder (id, member_id, name, description, color, created_at, updated_at)
VALUES (3, 1, 'Spring Framework', 'Spring 심화 학습 자료', '#42f560', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), updated_at=NOW();


-- -----------------------------------------------------
-- Table `source`
-- -----------------------------------------------------
INSERT INTO source (id, member_id, source_folder_id, original_name, content_type, file_path, file_size_bytes, status, created_at, updated_at)
VALUES (1, 1, 1, 'chapter1_variables.pdf', 'application/pdf', 's3://learning-sources/2025/09/12/member-1/5b4af2d1-c803-4e82-bbe6-bcbeff2c21c1.pdf', 1048576, 'READY', NOW(), NOW())
ON DUPLICATE KEY UPDATE original_name=VALUES(original_name), updated_at=NOW();

INSERT INTO source (id, member_id, source_folder_id, original_name, content_type, file_path, file_size_bytes, status, created_at, updated_at)
VALUES (2, 1, 1, 'chapter2_operators.pdf', 'application/pdf', 's3://learning-sources/2025/09/12/member-1/5b4af2d1-c803-4e82-bbe6-bcbeff2c21c1.pdf', 2097152, 'READY', NOW(), NOW())
ON DUPLICATE KEY UPDATE original_name=VALUES(original_name), updated_at=NOW();

INSERT INTO source (id, member_id, source_folder_id, original_name, content_type, file_path, file_size_bytes, status, created_at, updated_at)
VALUES (3, 2, 2, 'sorting_algorithms.pdf', 'application/pdf', 's3://learning-sources/2025/09/12/member-1/5b4af2d1-c803-4e82-bbe6-bcbeff2c21c1.pdf', 3145728, 'READY', NOW(), NOW())
ON DUPLICATE KEY UPDATE original_name=VALUES(original_name), updated_at=NOW();

INSERT INTO source (id, member_id, source_folder_id, original_name, content_type, file_path, file_size_bytes, status, created_at, updated_at)
VALUES (4, 1, 3, 'spring_aop_deep_dive.txt', 'text/plain', 's3://learning-sources/2025/09/12/member-1/5b4af2d1-c803-4e82-bbe6-bcbeff2c21c1.pdf', 512000, 'READY', NOW(), NOW())
ON DUPLICATE KEY UPDATE original_name=VALUES(original_name), updated_at=NOW();


-- -----------------------------------------------------
-- Table `question_set`
-- -----------------------------------------------------
INSERT INTO question_set (id, owner_id, title, difficulty, type, question_length, created_at, updated_at)
VALUES (1, 1, '자바 변수와 타입', 'EASY', 'MULTIPLE_CHOICE', 5, NOW(), NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title), question_length=VALUES(question_length), updated_at=NOW();

INSERT INTO question_set (id, owner_id, title, difficulty, type, question_length, created_at, updated_at)
VALUES (2, 2, '정렬 알고리즘 기초', 'HARD', 'SHORT_ANSWER', 5, NOW(), NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title), question_length=VALUES(question_length), updated_at=NOW();

INSERT INTO question_set (id, owner_id, title, difficulty, type, question_length, created_at, updated_at)
VALUES (3, 1, 'Spring AOP 핵심 개념', 'HARD', 'SUBJECTIVE', 5, NOW(), NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title), question_length=VALUES(question_length), updated_at=NOW();


-- -----------------------------------------------------
-- Table `question` (15 questions)
-- -----------------------------------------------------
-- Question Set 1 (Java Basics)
INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (1, 1, 1, '다음 중 자바의 기본 타입(Primitive Type)이 아닌 것은?', 'String', 'String은 참조 타입(Reference Type)입니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (2, 1, 1, '변수를 선언할 때 사용하는 키워드는 무엇인가요?', 'int, long, double 등', '변수의 타입에 맞는 키워드를 사용해야 합니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (3, 1, 1, '자동 형변환(Promotion)에 대한 설명으로 옳은 것은?', '표현 범위가 좁은 타입에서 넓은 타입으로 변환될 때 발생한다.', '예: int -> long, float -> double', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (4, 1, 1, '`final` 키워드가 변수에 사용될 때의 의미는 무엇인가요?', '한 번만 값을 할당할 수 있는 상수가 된다.', '초기화 이후에는 값을 변경할 수 없습니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (5, 1, 2, '산술 연산자 `%`의 역할은 무엇인가요?', '나머지 연산', '두 수를 나눈 나머지를 구합니다. 예: 10 % 3 = 1', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

-- Question Set 2 (Algorithms)
INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (6, 2, 3, '버블 정렬(Bubble Sort)의 평균 시간 복잡도는 무엇인가요?', 'O(n^2)', '최선, 평균, 최악 모두 O(n^2)입니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (7, 2, 3, '선택 정렬(Selection Sort)의 핵심 아이디어는 무엇인가요?', '배열에서 최소값(또는 최대값)을 찾아 정렬되지 않은 부분의 첫 요소와 교환한다.', '이 과정을 배열의 크기만큼 반복합니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (8, 2, 3, '퀵 정렬(Quick Sort)에서 `피봇(pivot)`의 역할은 무엇인가요?', '분할의 기준이 되는 요소', '피봇보다 작은 요소는 왼쪽, 큰 요소는 오른쪽으로 이동시킵니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (9, 2, 3, '병합 정렬(Merge Sort)이 다른 O(n log n) 정렬에 비해 가지는 주요 장점은 무엇인가요?', '안정 정렬(Stable Sort)이다.', '같은 값의 요소들의 상대적인 순서가 정렬 후에도 유지됩니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (10, 2, 3, '삽입 정렬(Insertion Sort)이 가장 효율적인 경우는 어떤 데이터 상태일 때인가요?', '거의 정렬되어 있는 상태', '이 경우 O(n)에 가까운 시간 복잡도를 가집니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

-- Question Set 3 (Spring AOP)
INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (11, 3, 4, 'AOP(Aspect-Oriented Programming)의 주된 목적은 무엇인가요?', '관심사의 분리 (Separation of Concerns)', '로깅, 트랜잭션, 보안 등 여러 모듈에 흩어져 있는 공통 기능(횡단 관심사)을 분리하여 관리합니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (12, 3, 4, 'AOP 용어 중 `조인 포인트(Join Point)`란 무엇을 의미하나요?', '어드바이스(Advice)가 적용될 수 있는 모든 위치', '메서드 실행, 예외 발생 등 애플리케이션 실행 흐름의 특정 지점을 의미합니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (13, 3, 4, '다음 중 Spring AOP의 `어드바이스(Advice)` 종류가 아닌 것은?', '`@Around`, `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing` 외의 것', '이 5가지가 Spring에서 지원하는 주요 어드바이스 타입입니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (14, 3, 4, '`포인트컷(Pointcut)`의 역할은 무엇인가요?', '조인 포인트 중에서 어드바이스를 적용할 대상을 선별하는 표현식', '어디에 공통 기능을 적용할지 정밀하게 지정하는 역할을 합니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();

INSERT INTO question (id, question_set_id, source_id, question_text, answer, explanation, created_at, updated_at)
VALUES (15, 3, 4, 'Spring AOP가 기본적으로 프록시(Proxy)를 생성하는 방식은 무엇인가요?', 'JDK Dynamic Proxy 또는 CGLIB', '인터페이스가 있으면 JDK Dynamic Proxy, 없으면 CGLIB를 사용하여 프록시 객체를 생성합니다.', NOW(), NOW())
ON DUPLICATE KEY UPDATE question_text=VALUES(question_text), updated_at=NOW();


-- -----------------------------------------------------
-- Table `question_set_source` (Many-to-Many relationship)
-- -----------------------------------------------------
INSERT INTO question_set_source (question_set_id, source_id) VALUES (1, 1) ON DUPLICATE KEY UPDATE question_set_id=VALUES(question_set_id);
INSERT INTO question_set_source (question_set_id, source_id) VALUES (1, 2) ON DUPLICATE KEY UPDATE question_set_id=VALUES(question_set_id);
INSERT INTO question_set_source (question_set_id, source_id) VALUES (2, 3) ON DUPLICATE KEY UPDATE question_set_id=VALUES(question_set_id);
INSERT INTO question_set_source (question_set_id, source_id) VALUES (3, 4) ON DUPLICATE KEY UPDATE question_set_id=VALUES(question_set_id);
