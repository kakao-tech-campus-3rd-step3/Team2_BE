package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.domain.dto.QuestionUpdateParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
@Entity(name = "question_short_answer")
@DiscriminatorValue("ShortAnswerQuestion")
public class ShortAnswerQuestion extends Question {

  private String answer;

  @Override
  public void update(QuestionUpdateParam param) {
    setQuestionText(param.questionText());
    setExplanation(param.explanation());
    this.answer = param.answer();
    validate();
  }

  @Override
  public boolean isCorrect(Object userAnswer) {
    if (userAnswer == null || this.answer == null) {
      return false;
    }
    String processedAnswer = this.answer.replaceAll("\\s+", "").toLowerCase();
    String processedUserAnswer = userAnswer.toString().replaceAll("\\s+", "").toLowerCase();
    return processedUserAnswer.equals(processedAnswer);
  }

  private void validate() {
    // TODO: 추후 validation 추가.
  }

  /**
   * @param questionSet 문제가 속한 문제집
   * @param questionDto LLM이 생성한 문제 응답
   * @return 생성된 단답형 문제 엔티티
   */
  public static ShortAnswerQuestion createFromLlm(
      QuestionSet questionSet, LlmGeneratedQuestionResponse questionDto) {

    ShortAnswerQuestion question =
        ShortAnswerQuestion.builder()
            .questionSet(questionSet)
            .questionText(questionDto.questionText())
            .answer(questionDto.answer())
            .explanation(questionDto.explanation())
            .build();

    question.validate();
    return question;
  }
}
