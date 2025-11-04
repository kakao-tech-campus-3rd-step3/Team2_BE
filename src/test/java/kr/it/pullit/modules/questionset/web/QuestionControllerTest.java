package kr.it.pullit.modules.questionset.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.service.QuestionService;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionCreateRequest;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionUpdateRequestDto;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionResponse;
import kr.it.pullit.platform.security.config.SecurityConfig;
import kr.it.pullit.platform.security.jwt.JwtAuthenticator;
import kr.it.pullit.platform.security.jwt.JwtTokenProvider;
import kr.it.pullit.platform.security.jwt.exception.JwtAuthenticationEntryPoint;
import kr.it.pullit.platform.security.jwt.handler.LocalAuthenticationHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = QuestionController.class,
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
class QuestionControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private QuestionService questionService;

  @MockitoBean private LocalAuthenticationHandler localAuthenticationHandler;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private JwtAuthenticator jwtAuthenticator;

  @MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Test
  @DisplayName("문제를 생성하면 생성된 리소스 위치를 반환한다")
  void createQuestionReturnsLocationHeader() throws Exception {
    QuestionCreateRequest request =
        new QuestionCreateRequest(
            1L, QuestionType.MULTIPLE_CHOICE, "문제를 작성합니다.", List.of("A", "B", "C"), "A", "해설");

    QuestionResponse response =
        QuestionResponse.builder()
            .id(10L)
            .questionType(QuestionType.MULTIPLE_CHOICE)
            .questionText("문제를 작성합니다.")
            .options(List.of("A", "B", "C"))
            .answer("A")
            .explanation("해설")
            .build();

    given(questionService.createQuestion(any(QuestionCreateRequest.class))).willReturn(response);

    mockMvc
        .perform(
            post("/api/question")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/question/10"));

    then(questionService).should().createQuestion(request);
  }

  @Test
  @DisplayName("문제 ID로 조회하면 응답 본문을 반환한다")
  void getQuestionByIdReturnsResponse() throws Exception {
    QuestionResponse response =
        QuestionResponse.builder()
            .id(5L)
            .questionType(QuestionType.TRUE_FALSE)
            .questionText("네트워크 문제")
            .answer(Boolean.TRUE)
            .explanation("해설")
            .build();

    given(questionService.getQuestionById(5L)).willReturn(response);

    mockMvc
        .perform(get("/api/question/{id}", 5L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5L))
        .andExpect(jsonPath("$.questionType").value("TRUE_FALSE"))
        .andExpect(jsonPath("$.questionText").value("네트워크 문제"))
        .andExpect(jsonPath("$.answer").value(true))
        .andExpect(jsonPath("$.explanation").value("해설"));

    then(questionService).should().getQuestionById(5L);
  }

  @Test
  @DisplayName("문제를 수정하면 수정된 결과를 반환한다")
  void updateQuestionReturnsUpdatedResponse() throws Exception {
    QuestionUpdateRequestDto updateRequest =
        new QuestionUpdateRequestDto("수정된 문제", List.of("A", "B"), "A", "새로운 해설");

    QuestionResponse response =
        QuestionResponse.builder()
            .id(20L)
            .questionType(QuestionType.MULTIPLE_CHOICE)
            .questionText("수정된 문제")
            .options(List.of("A", "B"))
            .answer("A")
            .explanation("새로운 해설")
            .build();

    given(questionService.updateQuestion(eq(20L), any(QuestionUpdateRequestDto.class)))
        .willReturn(response);

    mockMvc
        .perform(
            put("/api/question/{id}", 20L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(20L))
        .andExpect(jsonPath("$.questionText").value("수정된 문제"))
        .andExpect(jsonPath("$.options[0]").value("A"))
        .andExpect(jsonPath("$.answer").value("A"))
        .andExpect(jsonPath("$.explanation").value("새로운 해설"));

    then(questionService).should().updateQuestion(20L, updateRequest);
  }

  @Test
  @DisplayName("문제를 삭제하면 204 상태 코드를 반환한다")
  void deleteQuestionReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/api/question/{id}", 7L)).andExpect(status().isNoContent());

    then(questionService).should().deleteQuestion(7L);
  }
}
