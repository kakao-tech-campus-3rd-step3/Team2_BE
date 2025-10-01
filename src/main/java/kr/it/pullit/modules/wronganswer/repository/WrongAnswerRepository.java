package kr.it.pullit.modules.wronganswer.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import org.springframework.data.domain.Pageable;

public interface WrongAnswerRepository {
  WrongAnswer save(WrongAnswer wrongAnswer);

  List<WrongAnswer> saveAll(Iterable<WrongAnswer> entities);

  Optional<WrongAnswer> findByMemberIdAndQuestionId(Long memberId, Long questionId);

  List<Object[]> findAllWrongAnswerQuestionSetAndCountByMemberId(Long memberId);

  List<Object[]> findWrongAnswerQuestionSetWithCursor(
      Long memberId, Long cursor, Pageable pageable);
}
