package kr.it.pullit.modules.commonfolder.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.api.FolderFacade;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.QuestionSetFolderRequest;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;

@AuthenticatedMvcSliceTest(controllers = CommonFolderController.class)
@DisplayName("CommonFolderController 슬라이스 테스트")
class CommonFolderControllerTest extends ControllerTest {

  @MockitoBean private CommonFolderPublicApi commonFolderPublicApi;

  @MockitoBean private FolderFacade folderFacade;

  @Test
  @WithMockMember
  @DisplayName("로그인한 사용자는 자신의 폴더 목록을 성공적으로 조회한다")
  void shouldSuccessfullyRetrieveFoldersWhenLoggedIn() throws Exception {
    // given
    var folder1 = new CommonFolderResponse(1L, "폴더1", CommonFolderType.QUESTION_SET, 0);
    var folder2 = new CommonFolderResponse(2L, "폴더2", CommonFolderType.QUESTION_SET, 1);
    given(commonFolderPublicApi.getFolders(1L, CommonFolderType.QUESTION_SET))
        .willReturn(List.of(folder1, folder2));

    // when & then
    mockMvc
        .perform(get("/api/common-folders").param("type", CommonFolderType.QUESTION_SET.name()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].name").value("폴더1"))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].name").value("폴더2"));
  }

  @Test
  @WithMockMember
  @DisplayName("로그인한 사용자는 특정 폴더를 성공적으로 조회한다")
  void shouldSuccessfullyRetrieveFolderByIdWhenLoggedIn() throws Exception {
    // given
    var folder = new CommonFolderResponse(1L, "폴더1", CommonFolderType.QUESTION_SET, 0);
    given(commonFolderPublicApi.getFolder(1L, 1L)).willReturn(folder);

    // when & then
    mockMvc
        .perform(get("/api/common-folders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("폴더1"));
  }

  @Test
  @WithMockMember
  @DisplayName("로그인한 사용자는 새 폴더를 성공적으로 생성한다")
  void shouldSuccessfullyCreateFolderWhenLoggedIn() throws Exception {
    // given
    var request = new QuestionSetFolderRequest("새 폴더", CommonFolderType.QUESTION_SET);
    var response = new CommonFolderResponse(1L, "새 폴더", CommonFolderType.QUESTION_SET, 0);
    given(commonFolderPublicApi.createFolder(1L, request)).willReturn(response);

    // when & then
    mockMvc
        .perform(
            post("/api/common-folders")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/common-folders/1"));
  }

  @Test
  @WithMockMember
  @DisplayName("로그인한 사용자는 자신의 폴더를 성공적으로 수정한다")
  void shouldSuccessfullyUpdateFolderWhenLoggedIn() throws Exception {
    // given
    var request = new QuestionSetFolderRequest("수정된 폴더", CommonFolderType.QUESTION_SET);
    var response = new CommonFolderResponse(1L, "수정된 폴더", CommonFolderType.QUESTION_SET, 0);
    given(commonFolderPublicApi.updateFolder(1L, 1L, request)).willReturn(response);

    // when & then
    mockMvc
        .perform(
            patch("/api/common-folders/1")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("수정된 폴더"));
  }

  @Test
  @WithMockMember
  @DisplayName("로그인한 사용자는 폴더 삭제 경고 정보를 성공적으로 조회한다")
  void shouldSuccessfullyRetrieveDeleteWarningWhenLoggedIn() throws Exception {
    // given
    given(folderFacade.getQuestionSetCountInFolder(1L, 1L)).willReturn(5L);

    // when & then
    mockMvc
        .perform(get("/api/common-folders/1/delete-warning"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questionSetCount").value(5L));
  }

  @Test
  @WithMockMember
  @DisplayName("로그인한 사용자는 자신의 폴더를 성공적으로 삭제한다")
  void shouldSuccessfullyDeleteFolderWhenLoggedIn() throws Exception {
    // given

    // when & then
    mockMvc.perform(delete("/api/common-folders/1")).andExpect(status().isNoContent());

    // verify
    org.mockito.Mockito.verify(folderFacade).deleteFolderAndContents(1L, 1L);
  }
}

