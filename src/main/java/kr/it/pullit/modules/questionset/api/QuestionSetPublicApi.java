package kr.it.pullit.modules.questionset.api;

import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetDto;

public interface QuestionSetPublicApi {

  QuestionSetDto getQuestionSetById(Long id);

  QuestionSetDto create(QuestionSetDto questionSetDto);
}
