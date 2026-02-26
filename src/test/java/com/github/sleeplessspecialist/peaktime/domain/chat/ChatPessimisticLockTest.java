package com.github.sleeplessspecialist.peaktime.domain.chat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomReq;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatParticipantRepository;
import com.github.sleeplessspecialist.peaktime.domain.chat.service.ChatService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

/**
 * 비관적 락 vs 일반 트랜잭션 동시성 비교 테스트 입니다.(Service 레이어)
 *
 * <p>
 * 동일한 조건에서
 * 1) addParticipantToChat(일반)
 * 2) addParticipantToChatWithPessimisticLock(비관적 락)
 * 을 각각 수행하여 최종 참여자 수를 비교합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.
 */
@SpringBootTest
@EnableJpaAuditing
@ActiveProfiles("test")
class ChatPessimisticLockTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Test
    void pessimisticLock_vs_normal_dataIntegrity() throws InterruptedException {

        int threadCount = 50;

        // =========================
        // 일반 트랜잭션 (동시성 문제 발생 가능)
        // =========================
        Long normalHostId = createUser("normal-host");

        CreateChatRoomRes normalRoomRes = chatService.createRoom(
                normalHostId,
                CreateChatRoomReq.builder()
                        .description("normal-room")
                        .build()
        );
        Long normalRoomId = normalRoomRes.getRoomId();

        ExecutorService normalExecutor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch normalLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long guestId = createUser("normal-guest-" + i);

            normalExecutor.submit(() -> {
                try {
                    chatService.addParticipantToChat(guestId, normalRoomId);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    normalLatch.countDown();
                }
            });
        }

        normalLatch.await();
        normalExecutor.shutdown();

        long normalParticipantCount = chatParticipantRepository.countByChatRoomId(normalRoomId);

        // =========================
        // 비관적 락 (동시성 문제 해결)
        // =========================
        Long lockHostId = createUser("lock-host");

        CreateChatRoomRes lockRoomRes = chatService.createRoom(
                lockHostId,
                CreateChatRoomReq.builder()
                        .description("lock-room")
                        .build()
        );
        Long lockRoomId = lockRoomRes.getRoomId();

        ExecutorService lockExecutor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch lockLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long guestId = createUser("lock-guest-" + i);

            lockExecutor.submit(() -> {
                try {
                    chatService.addParticipantToChatWithPessimisticLock(guestId, lockRoomId);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    lockLatch.countDown();
                }
            });
        }

        lockLatch.await();
        lockExecutor.shutdown();

        long lockParticipantCount = chatParticipantRepository.countByChatRoomId(lockRoomId);

        // =========================
        // 결과 출력
        // =========================
        System.out.println("=== 데이터 정합성 비교 ===");
        System.out.println("일반 트랜잭션 최종 참여자 수: " + normalParticipantCount + " (예상: 2 초과 가능)");
        System.out.println("비관적 락 최종 참여자 수: " + lockParticipantCount + " (예상: 2)");
    }

    private Long createUser(String name) {

        String email = name + "-" + System.nanoTime() + "@test.com";

        User user = User.createForSignup(
                name,
                email,
                "encodedPassword",
                "01012345678"
        );

        userRepository.save(user);

        return user.getId();
    }
}