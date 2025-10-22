package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import java.util.List;
import kr.it.pullit.modules.questionset.client.dto.response.LlmGeneratedQuestionResponse;
import kr.it.pullit.modules.questionset.domain.dto.QuestionUpdateParam;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.exception.InvalidQuestionException;
import kr.it.pullit.modules.questionset.exception.QuestionErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
@Entity(name = "question_multiple_choice")
@DiscriminatorValue("MultipleChoiceQuestion")
public class MultipleChoiceQuestion extends Question {

  @ElementCollection
  @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
  @Column(name = "option_text")
  private List<String> options;

  private String answer;

  @Override
  public void update(QuestionUpdateParam param) {
    setQuestionText(param.questionText());
    setExplanation(param.explanation());
    this.options = param.options();
    this.answer = param.answer();
    validate();
  }

  @Override
  public boolean isCorrect(Object userAnswer) {
    if (userAnswer == null || this.answer == null) {
      return false;
    }
    return userAnswer.toString().trim().equalsIgnoreCase(this.answer.trim());
  }

  @Override
  public QuestionType getQuestionType() {
    return QuestionType.MULTIPLE_CHOICE;
  }

  private void validate() {
    if (this.options == null || this.options.isEmpty()) {
      throw new InvalidQuestionException(QuestionErrorCode.MULTIPLE_CHOICE_OPTIONS_REQUIRED);
    }
  }

  /**
   * @param questionSet 문제가 속한 문제집
   * @param llmGeneratedQuestionResponse LLM이 생성한 문제 응답
   * @return 생성된 객관식 문제 엔티티
   */
  public static MultipleChoiceQuestion createFromLlm(
      QuestionSet questionSet, LlmGeneratedQuestionResponse llmGeneratedQuestionResponse) {

    MultipleChoiceQuestion question =
        MultipleChoiceQuestion.builder()
            .questionSet(questionSet)
            .questionText(llmGeneratedQuestionResponse.questionText())
            .options(llmGeneratedQuestionResponse.options())
            .answer(llmGeneratedQuestionResponse.answer())
            .explanation(llmGeneratedQuestionResponse.explanation())
            .build();

    question.validate();
    return question;
  }
}
