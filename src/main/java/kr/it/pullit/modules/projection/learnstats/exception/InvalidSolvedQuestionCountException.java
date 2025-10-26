package kr.it.pullit.modules.projection.learnstats.exception;

import kr.it.pullit.shared.error.BusinessException;

public class InvalidSolvedQuestionCountException extends BusinessException {

  public InvalidSolvedQuestionCountException() {
    super(LearnStatsErrorCode.INVALID_SOLVED_QUESTION_COUNT);
  }
}
