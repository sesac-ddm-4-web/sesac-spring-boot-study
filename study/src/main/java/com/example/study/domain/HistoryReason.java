package com.example.study.domain;

public enum HistoryReason {
    /** 스터디장이 삭제함 */
    OWNER_DELETED,
    /** 직접 탈퇴함 */
    LEFT,
    /** 참여 중이던 스터디가 삭제됨 */
    STUDY_DELETED
}
