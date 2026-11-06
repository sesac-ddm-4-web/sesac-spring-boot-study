package com.example.sesac_spring_boot_study;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class StudyService {
    private List<StudyMember> members = new ArrayList<>();
    private long nextId = 1;
    private int capacity = 3;


    public String participate(String name){
        if (name == null) {
            return "신청 실패: 이름을 입력해 주세요.";
        }
        name = name.strip();

        if (name.isEmpty()) {
            return "신청 실패: 이름을 입력해 주세요.";
        }
        for (StudyMember m : members) {
            if (m.getName().equals(name)) {
                return "신청 실패: 이미 신청한 이름입니다. (" + name + ")";
            }
        }

        boolean confirmed = countConfirmed() < capacity;
        StudyMember member = new StudyMember(nextId++, name, confirmed);
        members.add(member);

        return "신청 완료: " + member.getId() + " | " + member.getName() + " | " + statusText(member);
    }

    private int countConfirmed() {
        int count = 0;
        for (StudyMember m : members) {
            if (m.isConfirmed()) {
                count++;
            }
        }
        return count;
    }

    private String statusText(StudyMember m) {
        return m.isConfirmed() ? "확정" : "대기";
    }

    public String getMemberList() {
        if (members.isEmpty()) {
            return "참가자가 없습니다";
        }

        String result = "";
        for (StudyMember m : members) {
            result += m.getId() + " | " + m.getName() + " | " + statusText(m) + "\n";
        }
        return result;
    }

    public String cancel(String memberId) {
        if (memberId == null || memberId.strip().isEmpty()) {
            return "취소 실패: 잘못된 id 입니다.";
        }

        long id;
        try {
            id = Long.parseLong(memberId.strip());
        } catch (NumberFormatException e) {
            return "취소 실패: id는 숫자로 입력해 주세요.";
        }

        StudyMember target = null;
        for (StudyMember m : members) {
            if (m.getId() == id) {
                target = m;
                break;
            }
        }
        if (target == null) {
            return "취소 실패: 해당 id의 참가자가 없습니다.";
        }

        members.remove(target);
        String result = "취소 완료: " + target.getId() + " | " + target.getName() + " | " + statusText(target);

        if (target.isConfirmed()) {
            StudyMember promoted = promoteFirstWaiting();
            if (promoted != null) {
                result += "\n승격: " + promoted.getId() + " | " + promoted.getName() + " | " + statusText(promoted);
            }
        }
        return result;
    }

    private StudyMember promoteFirstWaiting() {
        for (StudyMember m : members) {
            if (!m.isConfirmed()) {
                m.setConfirmed();
                return m;
            }
        }
        return null;
    }
}
