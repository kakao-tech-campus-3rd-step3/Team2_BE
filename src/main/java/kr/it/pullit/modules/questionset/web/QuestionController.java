package kr.it.pullit.modules.questionset.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import kr.it.pullit.modules.questionset.service.QuestionService;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionCreateRequest;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Question API", description = "문제 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question")
public class QuestionController {

  private final QuestionService questionService;

  @PostMapping
  public ResponseEntity<Void> createQuestion(
      @Valid @RequestBody QuestionCreateRequest questionCreateRequest) {

    QuestionResponse questionResponse = questionService.createQuestion(questionCreateRequest);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(questionResponse.id())
            .toUri();

    return ResponseEntity.created(location).build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long id) {
    QuestionResponse questionResponse = questionService.getQuestionById(id);
    return ResponseEntity.ok(questionResponse);
  }

  @PutMapping("/{id}")
  public ResponseEntity<QuestionResponse> updateQuestion(
      @PathVariable Long id,
      @Valid @RequestBody QuestionUpdateRequestDto questionUpdateRequestDto) {

    QuestionResponse questionResponse =
        questionService.updateQuestion(id, questionUpdateRequestDto);
    return ResponseEntity.ok(questionResponse);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
    questionService.deleteQuestion(id);
    return ResponseEntity.noContent().build();
  }
}
