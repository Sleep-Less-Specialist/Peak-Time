package com.github.sleeplessspecialist.peaktime.domain.lecture.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 강의 영상 등록 요청 시 클라이언트로부터 전달받는 데이터를 담는 DTO입니다.
 * <p>
 * 영상 제목(Text)과 실제 영상 파일(MultipartFile)을 포함합니다.
 * 불변성을 위해 Setter를 제거하고 생성자 주입을 사용합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Getter
@RequiredArgsConstructor
public class LectureCreateReq {

	@NotBlank(message = "강의 제목은 필수입니다.")
	private final String title;

	@NotNull(message = "영상 파일은 필수입니다.")
	private final MultipartFile videoFile;
}