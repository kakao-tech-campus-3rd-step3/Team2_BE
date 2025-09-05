package kr.it.pullit.modules.questionset.web;

import kr.it.pullit.modules.questionset.service.QuestionSetService;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
