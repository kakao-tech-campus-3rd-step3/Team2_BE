package kr.it.pullit.modules.questionset.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;

public interface QuestionSetPublicApi {

  QuestionSetResponse getQuestionSetForSolving(Long id, Long memberId, Boolean isReviewing);

  QuestionSetResponse create(QuestionSetCreateRequestDto request, Long ownerId);

  void markAsComplete(Long questionSetId);

  void markAsFailed(Long questionSetId);

  void markAsUnprocessable(Long questionSetId);

  void update(Long questionSetId, QuestionSetUpdateRequestDto request, Long memberId);

  void updateAndMarkAsComplete(
      Long questionSetId, QuestionSetUpdateRequestDto request, Long memberId);

  void delete(Long questionSetId, Long memberId);

  void deleteAllByFolderId(Long folderId);

  void relocateQuestionSetsToDefaultFolder(Long memberId, Long folderId);

  List<QuestionSet> findStalePending(LocalDateTime threshold);

  Optional<QuestionSet> claimOneForRetry(int maxRetryCount);

  Optional<QuestionSet> findEntityByIdAndMemberId(Long id, Long memberId);

  List<LocalDateTime> findCompletedDatesByMemberId(Long memberId);

  long countByQuestionSetOwnerId(Long ownerId);

  long countCompletedQuestionsByMemberIdAndDateBetween(
      Long memberId, LocalDateTime start, LocalDateTime end);

  CursorPageResponse<MyQuestionSetsResponse> getMemberQuestionSets(
      Long memberId, Long cursor, int size);

  CursorPageResponse<MyQuestionSetsResponse> getMemberQuestionSets(
      Long memberId, Long cursor, int size, Long folderId);

  List<MyQuestionSetsResponse> getMemberQuestionSets(Long memberId);

  long countByFolderId(Long folderId);

  long countByMemberId(Long memberId);

  QuestionSetResponse getQuestionSetWhenHaveNoQuestionsYet(Long questionSetId, Long ownerId);
}
