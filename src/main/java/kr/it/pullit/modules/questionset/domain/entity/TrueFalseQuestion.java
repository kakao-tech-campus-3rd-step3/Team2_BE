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
@Entity(name = "question_true_false")
@DiscriminatorValue("TrueFalseQuestion")
public class TrueFalseQuestion extends Question {

  private boolean answer;

  @Override
  public void update(QuestionUpdateParam param) {
    setQuestionText(param.questionText());
    setExplanation(param.explanation());
    this.answer = Boolean.parseBoolean(param.answer());
    validate();
  }

  @Override
  public boolean isCorrect(String userAnswer) {
    if (userAnswer == null) {
      return false;
    }
    boolean parsedUserAnswer = Boolean.parseBoolean(userAnswer.trim());
    return this.answer == parsedUserAnswer;
  }

  private void validate() {
    // TrueFalseQuestion에 대한 특별한 유효성 검사 규칙이 있다면 여기에 추가
  }

  /**
   * @param questionSet 문제가 속한 문제집
   * @param questionDto LLM이 생성한 문제 응답
   * @return 생성된 OX 문제 엔티티
   */
  public static TrueFalseQuestion createFromLlm(
      QuestionSet questionSet, LlmGeneratedQuestionResponse questionDto) {

    TrueFalseQuestion question =
        TrueFalseQuestion.builder()
            .questionSet(questionSet)
            .questionText(questionDto.questionText())
            .answer("true".equalsIgnoreCase(questionDto.answer()))
            .explanation(questionDto.explanation())
            .build();

    question.validate();
    return question;
  }
}
