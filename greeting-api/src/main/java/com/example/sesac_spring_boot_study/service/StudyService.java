package com.example.sesac_spring_boot_study.service;

import com.example.sesac_spring_boot_study.entity.StudyMember;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyService {
    private final List<StudyMember> members = new ArrayList<>();
    private long id = 1;
    private int capacity = 3;

    public String apply(String name) {
        // 누락, 빈 값, 공백 거절
        if (name == null || name.isBlank()) {
            return "신청 실패 (이름을 정확히 입력해 주세요.)";
        }

        // 앞뒤 공백 제거
        String trimmedName = name.strip();

        // 명단 확인
        for(StudyMember m : members){
            if(m.getName().equals(trimmedName)){
                return "신청 실패 (이미 명단에 있는 이름입니다.)";
            }
        }

        // 정원 안이면 확정, 넘으면 대기
        String state = countConfirmed() < capacity ? "확정" : "대기";

        StudyMember member = new StudyMember(id++, trimmedName, state);
        members.add(member);

        return "신청 성공: " + memberInfo(member);
    }

    public String getParticipants() {
        if(members.isEmpty()){
            return "참가자가 없습니다";
        }

        String result = "";
        for(StudyMember m : members){
            result += memberInfo(m) + "\n";
        }
        return result;
    }

    public String cancel(String memberId){
        // 누락, 빈 값 거절
        if(memberId == null || memberId.isBlank()){
            return "취소 실패 (memberId를 입력해 주세요.)";
        }

        // 숫자 아니면 거절
        long targetId;
        try {
            targetId = Long.parseLong(memberId.strip());
        } catch (NumberFormatException e) {
            return "취소 실패 (memberId는 숫자만 입력할 수 있습니다.)";
        }

        // 취소할 사람 찾기
        StudyMember target = null;
        for(StudyMember m : members){
            if(m.getMemberId() == targetId){
                target = m;
                break;
            }
        }

        if(target == null){
            return "취소 실패 (명단에 없는 id입니다.)";
        }

        members.remove(target);
        String result = "취소 성공: " + memberInfo(target);

        // 확정자가 나가면 제일 먼저 신청한 대기자 1명 승격
        if(target.getState().equals("확정")){
            for(StudyMember m : members){
                if(m.getState().equals("대기")){
                    m.setState("확정");
                    result += "\n승격: " + memberInfo(m);
                    break;
                }
            }
        }

        return result;
    }

    public String changeCapacity(String value){
        // 누락, 빈 값 거절
        if(value == null || value.isBlank()){
            return "정원 변경 실패 (정원을 입력해 주세요.), 현재 정원=" + capacity;
        }

        // 숫자 아니면 거절
        int newCapacity;
        try {
            newCapacity = Integer.parseInt(value.strip());
        } catch (NumberFormatException e) {
            return "정원 변경 실패 (정원은 1~10 정수만 가능합니다.), 현재 정원=" + capacity;
        }

        // 0, 음수, 11 이상 거절
        if(newCapacity < 1 || newCapacity > 10){
            return "정원 변경 실패 (정원은 1~10 정수만 가능합니다.), 현재 정원=" + capacity;
        }

        // 현재 확정 인원보다 작으면 거절
        int confirmedCount = countConfirmed();
        if(newCapacity < confirmedCount){
            return "정원 변경 실패 (현재 확정 인원보다 작습니다.), 현재 정원=" + capacity;
        }

        capacity = newCapacity;
        String result = "정원 변경 성공: 현재 정원=" + capacity;

        // 빈자리만큼 대기자 승격
        for(StudyMember m : members){
            if(confirmedCount >= capacity){
                break;
            }
            if(m.getState().equals("대기")){
                m.setState("확정");
                confirmedCount++;
                result += "\n승격: " + memberInfo(m);
            }
        }

        return result;
    }

    // 현재 리스트에 확정인 갯수
    private int countConfirmed(){
        int count = 0;
        for(StudyMember m : members){
            if(m.getState().equals("확정")){
                count++;
            }
        }
        return count;
    }

    private String memberInfo(StudyMember m){
        return "id=" + m.getMemberId()
                + ", 이름=" + m.getName()
                + ", 상태=" + m.getState();
    }

}
