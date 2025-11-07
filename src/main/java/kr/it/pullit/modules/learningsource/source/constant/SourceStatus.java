package kr.it.pullit.modules.learningsource.source.constant;

public enum SourceStatus {
  UPLOADED, // 업로드 완료
  PROCESSING, // 처리 중
  READY, // 사용 가능
  FAILED, // 처리 실패
  NOT_EXIST, // 파일 S3에 존재하지 않음
  DELETED // 삭제 요청됨
}
