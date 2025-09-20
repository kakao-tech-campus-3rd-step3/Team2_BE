package kr.it.pullit.modules.questionset.client.dto.request;

import java.util.List;

public record LlmGeneratedQuestionRequest(
    String prompt, List<byte[]> fileDataList, int questionCount, String model) {}
