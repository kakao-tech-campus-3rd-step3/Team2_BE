package kr.it.pullit.modules.wronganswer.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.modules.wronganswer.service.dto.WrongAnswerSetDto;

public interface WrongAnswerRepository {
  WrongAnswer save(WrongAnswer wrongAnswer);

  List<WrongAnswer> saveAll(Iterable<WrongAnswer> entities);

  Optional<WrongAnswer> findByMemberIdAndQuestionId(Long memberId, Long questionId);

  List<WrongAnswer> findByMemberIdAndQuestionIdIn(Long memberId, List<Long> questionIds);

  List<WrongAnswerSetDto> findAllWrongAnswerSetAndCountByMemberId(Long memberId);

  List<WrongAnswerSetDto> findWrongAnswerSetWithCursor(Long memberId, Long cursor, int size);
}
