package kr.it.pullit.modules.questionset.web;

import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.service.QuestionService;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
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

  @GetMapping("/{id}")
  public ResponseEntity<QuestionSetDto> getQuestionSetById(@PathVariable Long id) {
    QuestionSetDto questionSetDto = questionSetPublicApi.getQuestionSetById(id);
    return ResponseEntity.ok(questionSetDto);
  }

  @PostMapping
  public ResponseEntity<QuestionSetDto> createQuestionSet(
      @RequestBody QuestionSetCreateRequestDto questionSetCreateRequestDto) {
    // TODO: 인증 적용 후 ownerID 동적으로 변경
    QuestionSetDto questionSetDto =
        new QuestionSetDto(
            1L,
            questionSetCreateRequestDto.sourceIds(),
            questionSetCreateRequestDto.title(),
            questionSetCreateRequestDto.difficulty(),
            questionSetCreateRequestDto.type(),
            questionSetCreateRequestDto.questionCount());

    questionSetDto = questionSetPublicApi.create(questionSetDto);

    questionService.generateQuestions(questionSetDto, llmGeneratedQuestionDtoList -> {});

    return ResponseEntity.ok(questionSetDto);
  }
}
