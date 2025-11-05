package kr.it.pullit.modules.questionset.client.dto.request;

import java.nio.file.Path;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationSpecification;

public record LlmGeneratedQuestionRequest(
    String prompt,
    List<Path> fileDataList,
    String model,
    QuestionGenerationSpecification specification) {}
