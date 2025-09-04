package kr.it.pullit.modules.questionset.service;

import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.QuestionSetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionSetService implements QuestionSetPublicApi {

  private final QuestionSetRepository questionSetRepository;

  @Autowired
  public QuestionSetService(QuestionSetRepository questionSetRepository) {
    this.questionSetRepository = questionSetRepository;
  }

  public QuestionSetDto questionSetGetById(Long id) {
    QuestionSet questionSet =
        questionSetRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문제집을 찾을 수 없습니다"));
    return new QuestionSetDto(questionSet);
  }
}
