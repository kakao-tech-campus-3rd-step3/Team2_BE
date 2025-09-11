package kr.it.pullit.platform.storage.s3.dto;

public record PresignedUrlResponse(String uploadUrl, String filePath) {}
