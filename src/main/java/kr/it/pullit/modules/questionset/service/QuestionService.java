package kr.it.pullit.modules.questionset.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.questionset.api.LlmClient;
import kr.it.pullit.modules.questionset.api.QuestionPublicApi;
import kr.it.pullit.modules.questionset.client.dto.LlmGeneratedQuestionDto;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.QuestionRepository;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.service.callback.QuestionGenerationSuccessCallback;
import kr.it.pullit.modules.questionset.service.factory.DifficultyPolicyFactory;
import kr.it.pullit.modules.questionset.service.factory.QuestionTypePolicyFactory;
import kr.it.pullit.modules.questionset.service.policy.difficulty.DifficultyPolicy;
import kr.it.pullit.modules.questionset.service.policy.type.QuestionTypePolicy;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService implements QuestionPublicApi {

  private final QuestionRepository questionRepository;
  private final QuestionSetRepository questionSetRepository;
  private final DifficultyPolicyFactory difficultyPolicyFactory;
  private final QuestionTypePolicyFactory questionTypePolicyFactory;
  private final SourcePublicApi sourcePublicApi;
  private final LlmClient llmClient;

  @Override
  @Async("llmGeneratorAsyncExecutor")
  public void generateQuestions(QuestionSetResponse questionSetResponse,
      QuestionGenerationSuccessCallback callback) {

    QuestionSet questionSet = questionSetRepository.findById(questionSetResponse.getId())
        .orElseThrow(() -> new IllegalArgumentException(
            "QuestionSet not found with id: " + questionSetResponse.getId()));

    DifficultyPolicy difficultyPolicy =
        difficultyPolicyFactory.getInstance(questionSetResponse.getDifficulty());
    QuestionTypePolicy questionTypePolicy =
        questionTypePolicyFactory.getInstance(questionSetResponse.getType());

    String difficultyPrompt = difficultyPolicy.getDifficultyPrompt();
    String questionTypePrompt = questionTypePolicy.getQuestionTypePrompt();
    String examplePrompt = questionTypePolicy.getExamplePrompt();

    String prompt = LlmClient.getPrompt(difficultyPrompt, questionTypePrompt, examplePrompt,
        questionSetResponse.getQuestionLength());

    List<byte[]> sourceFileDataBytes = new ArrayList<>();
    for (Long sourceId : questionSetResponse.getSourceIds()) {
      try {
        log.info("학습 소스 파일 내용 조회를 시작합니다. Source ID: {}", sourceId);
        byte[] contentBytes =
            sourcePublicApi.getContentBytes(sourceId, questionSetResponse.getOwnerID());

        if (contentBytes == null || contentBytes.length == 0) {
          log.warn("학습 소스 파일 내용이 비어있습니다. Source ID: {}, Size: {}", sourceId,
              contentBytes == null ? "null" : "0 bytes");
        } else {
          log.info("학습 소스 파일 조회 성공. Source ID: {}, Size: {} bytes", sourceId, contentBytes.length);
        }
        sourceFileDataBytes.add(contentBytes);

      } catch (Exception e) {
        log.error("sourcePublicApi.getContentBytes 호출 중 심각한 오류 발생. Source ID: {}", sourceId, e);
        throw new RuntimeException(
            "Failed to get content for source ID: " + sourceId + ". See logs for details.", e);
      }
    }

    if (sourceFileDataBytes.isEmpty()) {
      log.warn("요청된 소스 파일 데이터가 비어있습니다. QuestionSet ID: {}", questionSetResponse.getId());
    } else {
      for (int i = 0; i < sourceFileDataBytes.size(); i++) {
        byte[] data = sourceFileDataBytes.get(i);
        log.info("Gemini API 요청 파일 데이터 크기 확인. QuestionSet ID: {}, Source Index: {}, Size: {} bytes",
            questionSetResponse.getId(), i, (data != null ? data.length : "null"));
      }
    }

    log.info("AI 문제 생성을 시작합니다. QuestionSet ID: {}, Model: {}", questionSetResponse.getId(),
        "gemini-2.5-flash-lite");

    // TODO: 정책에 따라 모델 변경
    // TODO: soureceId 여러개 등록 가능하도록
    List<LlmGeneratedQuestionDto> llmGeneratedQuestionDtoList =
        llmClient.getLlmGeneratedQuestionContent(prompt, sourceFileDataBytes,
            questionSetResponse.getQuestionLength(), "gemini-1.5-flash-latest");

    saveQuestions(questionSet.getId(), llmGeneratedQuestionDtoList);

    callback.onSuccess(llmGeneratedQuestionDtoList);
  }

  @Override
  @Transactional
  public void saveQuestions(Long questionSetId, List<LlmGeneratedQuestionDto> questions) {
    QuestionSet questionSet = questionSetRepository.findById(questionSetId).orElseThrow(
        () -> new IllegalArgumentException("QuestionSet not found with id: " + questionSetId));

    for (LlmGeneratedQuestionDto llmGeneratedQuestionDto : questions) {
      Question question = new Question(questionSet, llmGeneratedQuestionDto.questionText(),
          llmGeneratedQuestionDto.options(), llmGeneratedQuestionDto.answer(),
          llmGeneratedQuestionDto.explanation());
      questionRepository.save(question);
    }
  }
}
