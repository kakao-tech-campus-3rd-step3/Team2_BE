package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionSetJpaRepository extends JpaRepository<QuestionSet, Long> {

  @Query("SELECT qs FROM QuestionSet qs LEFT JOIN FETCH qs.questions WHERE qs.id = :id")
  Optional<QuestionSet> findByIdWithQuestions(@Param("id") Long id);

  @Query("SELECT qs FROM QuestionSet qs WHERE qs.owner.id = :userId")
  List<QuestionSet> findByUserId(@Param("userId") Long userId);
}
