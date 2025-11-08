package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.QuestionSetStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionSetJpaRepository extends JpaRepository<QuestionSet, Long> {

  @Query(
      """
       SELECT qs
       FROM QuestionSet qs
       LEFT JOIN FETCH qs.questions
       WHERE qs.id = :id
       AND qs.deletedAt IS NULL
      """)
  Optional<QuestionSet> findByIdWithQuestions(@Param("id") Long id);

  List<QuestionSet> findByStatusAndCreatedAtBefore(
      QuestionSetStatus status, LocalDateTime threshold);

  @Query(
      value =
          """
        SELECT * FROM question_set
        WHERE status = 'FAILED'
          AND retry_count < :maxRetry
        ORDER BY id
        LIMIT 1
        FOR UPDATE SKIP LOCKED
      """,
      nativeQuery = true)
  Optional<QuestionSet> findFirstFailedSetForRetryForUpdate(@Param("maxRetry") int maxRetry);

  @Query(
      """
       SELECT qs
       FROM QuestionSet qs
       LEFT JOIN FETCH qs.questions
       WHERE qs.id = :id
       AND qs.ownerId = :memberId
       AND qs.deletedAt IS NULL
      """)
  Optional<QuestionSet> findByIdAndMemberId(@Param("id") Long id, @Param("memberId") Long memberId);

  @Query(
      """
       SELECT qs
       FROM QuestionSet qs
       LEFT JOIN FETCH qs.questions
       WHERE qs.id = :id
       AND qs.ownerId = :memberId
       AND qs.status = 'COMPLETE'
       AND qs.deletedAt IS NULL
      """)
  Optional<QuestionSet> findByIdWithQuestionsForSolve(
      @Param("id") Long id, @Param("memberId") Long memberId);

  @Query(
      """
      SELECT qs
      FROM QuestionSet qs
      WHERE qs.ownerId = :memberId
      AND qs.deletedAt IS NULL
      """)
  List<QuestionSet> findByMemberId(@Param("memberId") Long memberId);

  List<QuestionSet> findAllByCommonFolderId(Long commonFolderId);

  long countByCommonFolderId(Long commonFolderId);

  @Query(
      """
        SELECT DISTINCT qs
        FROM QuestionSet qs
        LEFT JOIN FETCH qs.questions q
        JOIN q.wrongAnswer wa
        WHERE qs.id = :id
        AND wa.memberId = :memberId
        AND wa.isReviewed = false
        AND qs.status = 'COMPLETE'
        AND qs.deletedAt IS NULL
      """)
  Optional<QuestionSet> findWrongAnswersByIdAndMemberId(
      @Param("id") Long id, @Param("memberId") Long memberId);

  Optional<QuestionSet> findByIdAndOwnerId(Long id, Long ownerId);

  @Query(
      """
        SELECT qs
        FROM QuestionSet qs
        WHERE qs.id = :id
        AND qs.ownerId = :memberId
        AND qs.status != 'COMPLETE'
        AND qs.deletedAt IS NULL
      """)
  Optional<QuestionSet> findQuestionSetWhenHaveNoQuestionsYet(Long id, Long memberId);

  @Query(
      """
        SELECT qs
        FROM QuestionSet qs
        WHERE qs.ownerId = :memberId
        AND qs.commonFolder.id = :folderId
        AND (:cursor IS NULL OR qs.id < :cursor)
        AND qs.deletedAt IS NULL
        ORDER BY qs.createdAt DESC, qs.id DESC
      """)
  List<QuestionSet> findByMemberIdAndFolderIdWithCursor(
      @Param("memberId") Long memberId,
      @Param("folderId") Long folderId,
      @Param("cursor") Long cursor,
      Pageable pageable);

  @Query(
      """
        SELECT qs
        FROM QuestionSet qs
        WHERE qs.ownerId = :memberId
        AND (:cursor IS NULL OR qs.id < :cursor)
        AND qs.deletedAt IS NULL
        ORDER BY qs.createdAt DESC, qs.id DESC
      """)
  List<QuestionSet> findByMemberIdWithCursor(
      @Param("memberId") Long memberId, @Param("cursor") Long cursor, Pageable pageable);

  long countByOwnerId(Long memberId);

  @Query(
      """
          SELECT SUM(q.questionLength) FROM QuestionSet q
          WHERE q.ownerId = :memberId
          AND q.status = 'COMPLETE'
          AND q.createdAt BETWEEN :start AND :end
          AND q.deletedAt IS NULL
      """)
  Long countCompletedQuestionsByMemberIdAndDateBetween(
      @Param("memberId") Long memberId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query(
      """
          SELECT DISTINCT q.createdAt
          FROM QuestionSet q
          WHERE q.ownerId = :memberId
          AND q.status = 'COMPLETE'
          AND q.deletedAt IS NULL
          ORDER BY q.createdAt ASC
      """)
  List<LocalDateTime> findCompletedDatesByMemberId(@Param("memberId") Long memberId);

  @Modifying(clearAutomatically = true)
  @Query(
      value =
          """
            UPDATE question_set
            SET common_folder_id = :defaultFolderId
            WHERE common_folder_id = :folderId
          """,
      nativeQuery = true)
  void relocateAllByFolderIdToDefaultFolder(
      @Param("folderId") Long folderId, @Param("defaultFolderId") Long defaultFolderId);
}
