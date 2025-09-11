package kr.it.pullit.modules.questionset.web;

import kr.it.pullit.modules.questionset.domain.enums.PublishStatus;
import kr.it.pullit.modules.questionset.service.QuestionSetService;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreateResponseDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question-set")
public class QuestionSetController {
  private final QuestionSetService questionSetService;

  @Autowired
  public QuestionSetController(QuestionSetService questionSetService) {
    this.questionSetService = questionSetService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<QuestionSetDto> getQuestionSet(@PathVariable Long id) {
    QuestionSetDto questionSetDto = questionSetService.questionSetGetById(id);
    return ResponseEntity.ok(questionSetDto);
  }

  @PostMapping
  public ResponseEntity<QuestionSetCreateResponseDto> createQuestionSet(
      @RequestBody QuestionSetCreateRequestDto questionSetCreateRequestDto) {
    questionSetService.generateQuestion(
        questionSetCreateRequestDto.questionCount(),
        questionSetCreateRequestDto.type(),
        questionSetCreateRequestDto.difficulty(),
        questionSetCreateRequestDto.filePath(),
        questions -> {});

    QuestionSetCreateResponseDto responseDto =
        new QuestionSetCreateResponseDto(PublishStatus.SUCCESS, "문제 생성이 등록됐습니다.");

    return ResponseEntity.ok(responseDto);
  }
}
