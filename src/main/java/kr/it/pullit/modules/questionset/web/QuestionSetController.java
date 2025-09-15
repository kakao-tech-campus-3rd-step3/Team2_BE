package kr.it.pullit.modules.questionset.web;

import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.service.QuestionService;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question-set")
public class QuestionSetController {

    private final QuestionSetPublicApi questionSetPublicApi;
    private final QuestionService questionService;
    private final MemberPublicApi memberPublicApi;

    @GetMapping("/{id}")
    public ResponseEntity<QuestionSetDto> getQuestionSetById(@PathVariable Long id) {
        QuestionSetDto questionSetDto = questionSetPublicApi.getQuestionSetById(id);
        return ResponseEntity.ok(questionSetDto);
    }

    @PostMapping
    public ResponseEntity<QuestionSetDto> createQuestionSet(
            @RequestBody QuestionSetCreateRequestDto questionSetCreateRequestDto,
            @AuthenticationPrincipal OAuth2User oAuth2User) {

        Long kakaoId = oAuth2User.getAttribute("id");
        Long memberId = memberPublicApi.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당하는 사용자를 찾을 수 없습니다. kakaoId : " + kakaoId))
                .getId();

        QuestionSetDto questionSetDto =
                new QuestionSetDto(
                        memberId,
                        questionSetCreateRequestDto.sourceIds(),
                        questionSetCreateRequestDto.title(),
                        questionSetCreateRequestDto.difficulty(),
                        questionSetCreateRequestDto.type(),
                        questionSetCreateRequestDto.questionCount());

        questionSetDto = questionSetPublicApi.create(questionSetDto);

        questionService.generateQuestions(questionSetDto, llmGeneratedQuestionDtoList -> {
        });

        return ResponseEntity.ok(questionSetDto);
    }
}
