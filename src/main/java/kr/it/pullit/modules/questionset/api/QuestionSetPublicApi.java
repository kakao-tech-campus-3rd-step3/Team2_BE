package kr.it.pullit.modules.questionset.api;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;

public interface QuestionSetPublicApi {

  QuestionSetResponse getQuestionSetById(Long id);

  QuestionSetResponse create(QuestionSetCreateRequestDto request, Long ownerId);

  void updateStatus(Long questionSetId, QuestionSetStatus status);

  Optional<QuestionSet> findEntityById(Long id);

  List<MyQuestionSetsResponse> getUserQuestionSets(Long userId);
}
