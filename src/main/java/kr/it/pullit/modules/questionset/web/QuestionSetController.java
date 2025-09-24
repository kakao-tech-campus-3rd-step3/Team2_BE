package kr.it.pullit.modules.questionset.web;

import java.net.URI;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question-set")
public class QuestionSetController {

  private final QuestionSetPublicApi questionSetPublicApi;

  @GetMapping("/{id}")
  public ResponseEntity<QuestionSetResponse> getQuestionSetById(@PathVariable Long id) {
    QuestionSetResponse questionSetResponse = questionSetPublicApi.getQuestionSetById(id);
    return ResponseEntity.ok(questionSetResponse);
  }

  @PostMapping
  public ResponseEntity<Void> createQuestionSet(
      @RequestBody QuestionSetCreateRequestDto questionSetCreateRequestDto) {
    // TODO: 인증 적용 후 ownerID 동적으로 변경
    Long userId = 1L;

    QuestionSetResponse questionSetResponse =
        questionSetPublicApi.create(questionSetCreateRequestDto, userId);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(questionSetResponse.getId())
            .toUri();

    return ResponseEntity.created(location).build();
  }
}
