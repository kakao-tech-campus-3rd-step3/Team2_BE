package kr.it.pullit.modules.questionset.service;

import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionSetService implements QuestionSetPublicApi {

  private final QuestionSetRepository questionSetRepository;

  @Transactional(readOnly = true)
  public QuestionSetDto questionSetGetById(Long id) {
    QuestionSet questionSet =
        questionSetRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문제집을 찾을 수 없습니다"));
    return new QuestionSetDto(questionSet);
  }

  @Transactional
  public QuestionSetDto create(QuestionSetDto questionSetDto) {
    QuestionSet questionSet =
        new QuestionSet(
            questionSetDto.getOwnerID(),
            questionSetDto.getSourceIds(),
            questionSetDto.getTitle(),
            questionSetDto.getDifficulty(),
            questionSetDto.getType(),
            questionSetDto.getQuestionLength());
    QuestionSet savedQuestionSet = questionSetRepository.save(questionSet);
    return new QuestionSetDto(savedQuestionSet);
  }
}
