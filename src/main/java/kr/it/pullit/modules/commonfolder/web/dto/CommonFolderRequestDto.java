package kr.it.pullit.modules.commonfolder.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonFolderRequestDto {
  private String name;
  private String type;
  private Long parentId;
  private int sortOrder;
}
