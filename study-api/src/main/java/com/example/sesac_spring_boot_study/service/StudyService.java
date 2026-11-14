package com.example.sesac_spring_boot_study.service;

import com.example.sesac_spring_boot_study.domain.StudyMember;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyService {

    private final List<StudyMember> memberList = new ArrayList<>();
    private long nextId = 1L;
    private int capacity = 2;

    public String register(String name) {

        if (name == null || name.trim().isEmpty()) {
            return "신청 실패: 입력 오류.";
        }

        String realName = name.trim();

        if (isDuplicateName(realName)) {
            return "신청 실패: 중복 이름.";
        }

        boolean confirm = getConfirmedCount() < capacity;

        StudyMember member = new StudyMember(nextId++, realName, confirm);
        memberList.add(member);

        return "id = " + member.getId() + ", 이름 = " + member.getName() + ", 상태 = " + (confirm ? "확정" : "대기");
    }

    private boolean isDuplicateName(String name) {

        for (StudyMember member : memberList) {
            if (member.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    private long getConfirmedCount() {
        return memberList.stream().filter(StudyMember::isConfirm).count();
    }

    public String cancel(String memberId) {

        Long id = parseId(memberId);

        if (id == null) {
            return "취소 실패: ID는 숫자로 입력.";
        }

        StudyMember targetMember = findMemberById(id);

        if (targetMember == null) {
            return "취소 실패: 존재하지 않는 ID.";
        }

        memberList.remove(targetMember);

        String result = "취소 성공: " + targetMember.getName();

        if (targetMember.isConfirm()) {
            String promoted = promoteWaitingMembers(1);

            if (!promoted.isEmpty()) {
                result += ", 대기자 승격: " + promoted;
            }
        }

        return result;
    }

    private Long parseId(String memberId) {

        if (memberId == null || memberId.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(memberId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private StudyMember findMemberById(Long id) {

        for (StudyMember member : memberList) {
            if (member.getId().equals(id)) {
                return member;
            }
        }

        return null;
    }

    private String promoteWaitingMembers(int available) {

        List<String> promoted = new ArrayList<>();

        for (StudyMember member : memberList) {

            if (available == 0) {
                break;
            }

            if (!member.isConfirm()) {
                member.setConfirmed(true);
                promoted.add(member.getName());
                available--;
            }
        }

        return String.join(", ", promoted);
    }

    public String changeCapacity(String value) {

        Integer newCapacity = parseCapacity(value);

        if (newCapacity == null) {
            return "변경 실패: 정원은 1~10 사이의 정수. 현재 정원=" + capacity;
        }

        long confirmedCount = getConfirmedCount();

        if (newCapacity < confirmedCount) {
            return "변경 실패: 현재 확정 인원보다 작게 설정 불가. 현재 정원=" + capacity;
        }

        capacity = newCapacity;

        int available = capacity - (int) confirmedCount;
        String promoted = promoteWaitingMembers(available);

        return "변경 성공: 현재 정원=" + capacity + ", 승격자=" + (promoted.isEmpty() ? "없음" : promoted);
    }

    private Integer parseCapacity(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            int number = Integer.parseInt(value.trim());

            if (number < 1 || number > 10) {
                return null;
            }
            return number;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getMembers() {

        if (memberList.isEmpty()) {
            return "참가자 없음.";
        }

        StringBuilder result = new StringBuilder();

        result.append("현재 정원: ").append(capacity).append("\n\n");

        for (StudyMember member : memberList) {
            result.append(formatMember(member)).append("\n");
        }

        return result.toString();
    }

    private String formatMember(StudyMember member) {

        return "id=" + member.getId() + ", 이름=" + member.getName() + ", 상태=" + (member.isConfirm() ? "확정" : "대기");
    }
}