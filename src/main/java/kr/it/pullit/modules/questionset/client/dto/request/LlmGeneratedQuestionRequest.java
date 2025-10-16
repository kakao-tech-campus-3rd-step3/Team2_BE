package kr.it.pullit.modules.questionset.client.dto.request;

import java.io.InputStream;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.QuestionGenerationSpecification;

public record LlmGeneratedQuestionRequest(
    String prompt,
    List<InputStream> fileDataList,
    String model,
    QuestionGenerationSpecification specification) {}
