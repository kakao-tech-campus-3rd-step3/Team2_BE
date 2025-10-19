package kr.it.pullit.modules.questionset.domain.dto;

import java.util.List;

public record QuestionUpdateParam(
    String questionText, String explanation, List<String> options, String answer) {}
