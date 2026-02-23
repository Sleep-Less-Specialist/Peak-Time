package com.github.sleeplessspecialist.peaktime.domain.notice.repository;

import com.github.sleeplessspecialist.peaktime.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * 공지사항(Notice) 엔티티에 대한 데이터 접근을 담당하는 Repository 입니다.
 *
 * <p>
 * 공지사항 조회 및 저장 등 기본 CRUD 기능을 제공합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.12
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
