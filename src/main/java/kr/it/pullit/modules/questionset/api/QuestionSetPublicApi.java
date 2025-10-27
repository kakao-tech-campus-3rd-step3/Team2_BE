package kr.it.pullit.modules.questionset.api;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsWithProgressResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;

public interface QuestionSetPublicApi {

  QuestionSetResponse getQuestionSetForSolving(Long id, Long memberId, Boolean isReviewing);

  QuestionSetResponse create(QuestionSetCreateRequestDto request, Long ownerId);

  void markAsComplete(Long questionSetId);

  void markAsFailed(Long questionSetId);

  void update(Long questionSetId, QuestionSetUpdateRequestDto request, Long memberId);

  void delete(Long questionSetId, Long memberId);

  void deleteAllByFolderId(Long folderId);

  Optional<QuestionSet> findEntityByIdAndMemberId(Long id, Long memberId);

  List<QuestionSet> findCompletedEntitiesByMemberId(Long memberId);

  MyQuestionSetsWithProgressResponse getMemberQuestionSets(
      Long memberId, Long cursor, int size, Long folderId);

  List<MyQuestionSetsResponse> getMemberQuestionSets(Long memberId);

  long countByFolderId(Long folderId);

  long countByMemberId(Long memberId);

  QuestionSetResponse getQuestionSetWhenHaveNoQuestionsYet(Long id, Long memberId);
}
