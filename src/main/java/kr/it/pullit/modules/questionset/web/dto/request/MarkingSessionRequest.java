package kr.it.pullit.modules.questionset.web.dto.request;

import java.util.List;

public record MarkingSessionRequest(List<MarkingRequest> answers) {}
