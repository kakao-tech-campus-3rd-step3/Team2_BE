package kr.it.pullit.modules.questionset.web;

import kr.it.pullit.modules.notification.service.NotificationService;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.service.QuestionService;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionCreationCompleteResponseDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question-set")
public class QuestionSetController {
  private final QuestionSetPublicApi questionSetPublicApi;
  private final QuestionService questionService;
  private final NotificationService notificationService;

  @GetMapping("/{id}")
  public ResponseEntity<QuestionSetDto> getQuestionSetById(@PathVariable Long id) {
    QuestionSetDto questionSetDto = questionSetPublicApi.getQuestionSetById(id);
    return ResponseEntity.ok(questionSetDto);
  }

  @PostMapping
  public ResponseEntity<QuestionSetDto> createQuestionSet(
      @RequestBody QuestionSetCreateRequestDto questionSetCreateRequestDto) {
    // TODO: 인증 적용 후 ownerID 동적으로 변경
    Long userId = 1L;
    QuestionSetDto questionSetDto =
        new QuestionSetDto(
            userId,
            questionSetCreateRequestDto.sourceIds(),
            questionSetCreateRequestDto.title(),
            questionSetCreateRequestDto.difficulty(),
            questionSetCreateRequestDto.type(),
            questionSetCreateRequestDto.questionCount());

    questionSetDto = questionSetPublicApi.create(questionSetDto);

    QuestionCreationCompleteResponseDto responseDto =
        new QuestionCreationCompleteResponseDto(
            true, questionSetDto.getId(), "QuestionSet created");
    questionService.generateQuestions(
        questionSetDto,
        llmGeneratedQuestionDtoList -> {
          notificationService.publishQuestionCreationComplete(userId, responseDto);
        });

    return ResponseEntity.ok(questionSetDto);
  }
}
