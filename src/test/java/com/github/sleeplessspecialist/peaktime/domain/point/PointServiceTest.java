package com.github.sleeplessspecialist.peaktime.domain.point;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.sleeplessspecialist.peaktime.domain.point.entity.PointTransaction;
import com.github.sleeplessspecialist.peaktime.domain.point.repository.PointTransactionRepository;
import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;

/**
 * PointService 단위 테스트
 * <p>
 * 포인트 적립/차감 비즈니스 로직 검증
 * 포인트 변동 이력(PointTransaction) 생성 여부 검증
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 25.
 */
@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

	@InjectMocks
	private PointService pointService;

	@Mock
	private PointTransactionRepository pointTransactionRepository;

	@Test
	@DisplayName("회원가입 성공 시 포인트 지급 및 이력저장 확인")
	void grantSignupBonus_success() {
		// given
		User user = User.createForSignup(
			"재원",
			"test@test.com",
			"encodedPassword",
			"010-1234-5678"
		);

		when(pointTransactionRepository.save(any(PointTransaction.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		// when
		pointService.grantSignupBonus(user);

		// then
		assertThat(user.getPoint()).isEqualTo(1000L);

		ArgumentCaptor<PointTransaction> captor = ArgumentCaptor.forClass(PointTransaction.class);
		verify(pointTransactionRepository).save(captor.capture());

		PointTransaction savedTx = captor.getValue();
		assertThat(savedTx.getAmount()).isEqualTo(1000L);
		assertThat(savedTx.getBalanceAfter()).isEqualTo(1000L);
		assertThat(savedTx.getUser()).isEqualTo(user);
		assertThat(savedTx.getType()).isEqualTo("SIGNUP_BONUS");
	}
}
