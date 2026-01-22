package com.github.sleeplessspecialist.peaktime.global.infra.s3;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.error.GlobalErrorCode;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AWS S3 파일 업로드를 담당하는 유틸리티 클래스입니다.
 * <p>
 * Spring Cloud AWS의 S3Template을 사용하여 파일을 업로드하고,
 * 업로드된 파일의 접근 가능한 URL을 반환합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

	private final S3Template s3Template;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	/**
	 * 파일을 S3에 업로드합니다.
	 *
	 * @param file    업로드할 파일 (MultipartFile)
	 * @param dirName S3 내부에 저장될 폴더 이름 (예: "video", "thumbnail")
	 * @return 업로드된 파일의 전체 URL
	 */
	public String upload(MultipartFile file, String dirName) {
		if (file.isEmpty()) {
			throw new CustomException(GlobalErrorCode.INVALID_REQUEST);
		}

		// 1. 파일 이름 중복 방지를 위한 UUID 생성
		String originalFilename = file.getOriginalFilename();
		String uuid = UUID.randomUUID().toString();
		String fileName = dirName + "/" + uuid + "_" + originalFilename;

		try (InputStream inputStream = file.getInputStream()) {
			// 2. S3에 파일 업로드 (Spring Cloud AWS 3.0 방식)
			s3Template.upload(bucket, fileName, inputStream, ObjectMetadata.builder()
				.contentType(file.getContentType())
				.build());

			log.info("S3 업로드 성공: {}", fileName);

		} catch (IOException e) {
			log.error("S3 업로드 실패: {}", e.getMessage());
			throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
		}

		// 3. 업로드된 파일의 접근 URL 반환
		return getFileUrl(fileName);
	}

	/**
	 * S3에 저장된 파일의 전체 URL을 가져옵니다.
	 */
	private String getFileUrl(String fileName) {
		// ap-northeast-2 (서울) 기준 URL 형식
		return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
	}
}