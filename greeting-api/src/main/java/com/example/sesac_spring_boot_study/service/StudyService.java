package com.example.sesac_spring_boot_study.service;

import com.example.sesac_spring_boot_study.domain.StudyMember;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudyService {
    private int capacity = 3;
    private final Map<Integer, StudyMember> members = new LinkedHashMap<>();
    private final Set<String> names = new HashSet<>();
    private int nextId;

    public synchronized String registerMember(String name) {
        if (names.contains(name)) {
            return "명단에 이미 있는 이름입니다.";
        }

        // id와 확정 상태 결정
        int id = nextId++;
        boolean confirmed = members.size() < capacity;

        StudyMember member = new StudyMember(id, name, confirmed);

        // 명단에 추가
        members.put(id, member);
        names.add(member.getName());

        return String.format("등록: %s", member);
    }

    public synchronized String getMembers() {
        if (members.isEmpty()) {
            return "참가자가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        members.values().forEach(member ->
                sb.append(member).append(System.lineSeparator())
        );

        return sb.toString().strip();
    }

    public synchronized String cancelRegistration(int id) {
        // 명단에서 멤버 삭제
        StudyMember removedMember = members.remove(id);
        if (removedMember == null) {
            return String.format("%d번 id는 참가자 명단에 없습니다.", id);
        }
        names.remove(removedMember.getName());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("취소: %s", removedMember));

        // 확정자 취소 시 승격자 발생 가능
        if (!removedMember.isWaiting()) {
            for (var member : members.values()) {
                if (member.isWaiting()) {
                    member.confirm();
                    sb.append(String.format("%n승격: %s", member));
                    break;
                }
            }
        }

        return sb.toString();
    }

    public synchronized String changeCapacity(int newCapacity) {
        if (newCapacity <= 0 || newCapacity > 10) {
            return "정원은 1명 이상 10명 이하여야 합니다.";
        }

        int confirmedCount = Math.min(capacity, members.size());

        if (newCapacity < confirmedCount) {
            return String.format("정원을 %d명으로 변경할 수 없습니다.%n현재 정원: %d명%n확정 인원: %d명", newCapacity, capacity, confirmedCount);
        }

        // 정원 변경 가능 -> 승격 대상 확인
        capacity = newCapacity;
        int spareCount = capacity - confirmedCount;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("정원을 %d명으로 변경했습니다.%n", capacity));
        for (var member : members.values()) {
            if (spareCount == 0) {
                break;
            }
            if (member.isWaiting()) {
                member.confirm();
                spareCount--;
                sb.append(String.format("승격: %s%n", member));
            }
        }

        return sb.toString().strip();
    }
}
