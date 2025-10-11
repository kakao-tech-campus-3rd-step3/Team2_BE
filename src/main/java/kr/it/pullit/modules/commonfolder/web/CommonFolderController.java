package kr.it.pullit.modules.commonfolder.web;

import java.util.List;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.service.CommonFolderService;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/folders")
public class CommonFolderController {

  private final CommonFolderService commonFolderService;

  @GetMapping
  public List<CommonFolder> getAllFolders() {
    return commonFolderService.getAllFolders();
  }

  @GetMapping("/type/{type}")
  public List<CommonFolder> getFoldersByType(@PathVariable String type) {
    return commonFolderService.getFoldersByType(type);
  }

  @GetMapping("/{id}")
  public CommonFolder getFolderById(@PathVariable Long id) {
    return commonFolderService.getFolderById(id);
  }

  @PostMapping
  public CommonFolder createFolder(@RequestBody CommonFolderRequestDto dto) {
    return commonFolderService.createFolder(
        dto.getName(), dto.getType(), dto.getParentId(), dto.getSortOrder());
  }

  @PutMapping("/{id}")
  public CommonFolder updateFolder(@PathVariable Long id, @RequestBody CommonFolderRequestDto dto) {
    return commonFolderService.updateFolder(
        id, dto.getName(), dto.getType(), dto.getParentId(), dto.getSortOrder());
  }

  @DeleteMapping("/{id}")
  public void deleteFolder(@PathVariable Long id) {
    commonFolderService.deleteFolder(id);
  }
}
