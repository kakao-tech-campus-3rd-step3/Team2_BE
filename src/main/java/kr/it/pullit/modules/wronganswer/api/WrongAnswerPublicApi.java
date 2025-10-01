package kr.it.pullit.modules.wronganswer.api;

import java.util.List;
import kr.it.pullit.modules.wronganswer.web.dto.WrongAnswerSetResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;

public interface WrongAnswerPublicApi {

  CursorPageResponse<WrongAnswerSetResponse> getMyWrongAnswers(
      Long memberId, Long cursor, int size);

  List<WrongAnswerSetResponse> getAllMyWrongAnswers(Long memberId);

  void markAsWrongAnswers(Long memberId, List<Long> questionIds);

  void markAsCorrectAnswers(Long memberId, List<Long> questionIds);
}
