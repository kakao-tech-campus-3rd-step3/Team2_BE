package kr.it.pullit.modules.commonfolder.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.api.FolderFacade;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.QuestionSetFolderRequest;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AuthenticatedMvcSliceTest(controllers = CommonFolderController.class)
@DisplayName("CommonFolderController 슬라이스 테스트")
class CommonFolderControllerTest extends ControllerTest {

  @MockitoBean private CommonFolderPublicApi commonFolderPublicApi;

  @MockitoBean private FolderFacade folderFacade;

  @Nested
  @DisplayName("폴더 조회")
  class DescribeGetFolders {

    @Test
    @DisplayName("타입으로 조회하면 폴더 목록을 반환한다")
    @WithMockMember
    void getQuestionSetFolders() throws Exception {
      given(commonFolderPublicApi.getFolders(CommonFolderType.QUESTION_SET))
          .willReturn(
              List.of(
                  new CommonFolderResponse(1L, "폴더1", CommonFolderType.QUESTION_SET, 0),
                  new CommonFolderResponse(2L, "폴더2", CommonFolderType.QUESTION_SET, 1)));

      mockMvc
          .perform(get("/api/common-folders").param("type", "QUESTION_SET"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].name").value("폴더1"))
          .andExpect(jsonPath("$[1].sortOrder").value(1));

      verify(commonFolderPublicApi).getFolders(CommonFolderType.QUESTION_SET);
    }

    @Test
    @DisplayName("ID로 조회하면 단일 폴더를 반환한다")
    @WithMockMember
    void getQuestionSetFolderById() throws Exception {
      given(commonFolderPublicApi.getFolder(3L))
          .willReturn(new CommonFolderResponse(3L, "단일", CommonFolderType.QUESTION_SET, 0));

      mockMvc
          .perform(get("/api/common-folders/{id}", 3L))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(3L))
          .andExpect(jsonPath("$.name").value("단일"));

      verify(commonFolderPublicApi).getFolder(3L);
    }
  }

  @Nested
  @DisplayName("폴더 생성")
  class DescribeCreateFolder {

    @Test
    @DisplayName("생성하면 201과 Location 헤더를 반환한다")
    @WithMockMember
    void createFolder() throws Exception {
      given(commonFolderPublicApi.createFolder(any(QuestionSetFolderRequest.class)))
          .willReturn(new CommonFolderResponse(10L, "새 폴더", CommonFolderType.QUESTION_SET, 0));

      QuestionSetFolderRequest request =
          new QuestionSetFolderRequest("새 폴더", CommonFolderType.QUESTION_SET);

      mockMvc
          .perform(
              post("/api/common-folders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(header().string(LOCATION, endsWith("/api/common-folders/10")));

      ArgumentCaptor<QuestionSetFolderRequest> captor =
          ArgumentCaptor.forClass(QuestionSetFolderRequest.class);
      verify(commonFolderPublicApi).createFolder(captor.capture());
      assertThat(captor.getValue().name()).isEqualTo("새 폴더");
      assertThat(captor.getValue().type()).isEqualTo(CommonFolderType.QUESTION_SET);
    }
  }

  @Nested
  @DisplayName("폴더 수정")
  class DescribeUpdateFolder {

    @Test
    @DisplayName("수정하면 200과 수정된 내용을 반환한다")
    @WithMockMember
    void updateFolder() throws Exception {
      given(commonFolderPublicApi.updateFolder(eq(5L), any(QuestionSetFolderRequest.class)))
          .willReturn(new CommonFolderResponse(5L, "수정", CommonFolderType.QUESTION_SET, 0));

      QuestionSetFolderRequest request =
          new QuestionSetFolderRequest("수정", CommonFolderType.QUESTION_SET);

      mockMvc
          .perform(
              patch("/api/common-folders/{id}", 5L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("수정"));

      ArgumentCaptor<QuestionSetFolderRequest> captor =
          ArgumentCaptor.forClass(QuestionSetFolderRequest.class);
      verify(commonFolderPublicApi).updateFolder(eq(5L), captor.capture());
      assertThat(captor.getValue().name()).isEqualTo("수정");
      assertThat(captor.getValue().type()).isEqualTo(CommonFolderType.QUESTION_SET);
    }
  }

  @Nested
  @DisplayName("삭제 경고")
  class DescribeGetFolderDeleteWarning {

    @Test
    @DisplayName("폴더에 속한 문제집 수를 반환한다")
    @WithMockMember
    void getFolderDeleteWarning() throws Exception {
      given(folderFacade.getQuestionSetCountInFolder(7L)).willReturn(3L);

      mockMvc
          .perform(get("/api/common-folders/{id}/delete-warning", 7L))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.questionSetCount").value(3L));

      verify(folderFacade).getQuestionSetCountInFolder(7L);
    }
  }

  @Nested
  @DisplayName("폴더 삭제")
  class DescribeDeleteFolder {

    @Test
    @DisplayName("삭제하면 204를 반환한다")
    @WithMockMember
    void deleteFolder() throws Exception {
      mockMvc.perform(delete("/api/common-folders/{id}", 9L)).andExpect(status().isNoContent());

      verify(folderFacade).deleteFolderAndContents(9L);
    }
  }
}
