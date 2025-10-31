package kr.it.pullit.modules.wronganswer.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WrongAnswerSetResponse 단위 테스트")
class WrongAnswerSetResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("of 정적 팩토리는 모든 필드를 채운 응답을 생성한다")
  void shouldCreateResponseWithFactoryMethod() {
    WrongAnswerSetResponse response =
        WrongAnswerSetResponse.of(
            10L, "자료구조 요약", List.of("교재", "강의"), DifficultyType.HARD, "트리", 3L, "CS", 99L);

    assertThat(response.questionSetId()).isEqualTo(10L);
    assertThat(response.questionSetTitle()).isEqualTo("자료구조 요약");
    assertThat(response.sourceNames()).containsExactly("교재", "강의");
    assertThat(response.difficulty()).isEqualTo(DifficultyType.HARD);
    assertThat(response.majorTopic()).isEqualTo("트리");
    assertThat(response.incorrectCount()).isEqualTo(3L);
    assertThat(response.category()).isEqualTo("CS");
    assertThat(response.lastWrongAnswerId()).isEqualTo(99L);
  }

  @Test
  @DisplayName("lastWrongAnswerId는 JSON 직렬화 시 필드에 포함되지 않는다")
  void shouldIgnoreLastWrongAnswerIdWhenSerialized() throws JsonProcessingException {
    WrongAnswerSetResponse response =
        WrongAnswerSetResponse.of(
            1L, "네트워크", List.of("블로그"), DifficultyType.EASY, "OSI", 1L, "네트워크", 42L);

    String json = objectMapper.writeValueAsString(response);

    assertThat(json).contains("\"questionSetId\":1");
    assertThat(json).doesNotContain("lastWrongAnswerId");
  }
}
