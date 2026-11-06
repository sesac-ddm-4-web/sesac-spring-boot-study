package com.example.sesac_spring_boot_study.service;

import com.example.sesac_spring_boot_study.StudyMember;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudyService {

    List<StudyMember> members = new ArrayList<>();
    public int capacity = 3;
    public long id = 1;

    public String apply (String name){ //참가신청
        if (name==null || name.trim().isEmpty()){
            return "올바른 이름을 입력하세요";
        } //이름 정리: 앞뒤 공백 제거 · 누락·빈 값·공백뿐인 이름은 거절
        name = name.trim();

        for (StudyMember m:members){
            if(m.getName().equals(name)){
                return "중복된 이름입니다.";
            }
        }//중복 이름: 정리한 이름이 명단에 있으면 거절 · 취소 후 재신청 가능

        long newId = id++;

        int confCount = 0;
        for (StudyMember m : members) {
            if (m.isConfirmed()) confCount++;
        }
        boolean confirmed = confCount < capacity;

        StudyMember member = new StudyMember(newId, name, confirmed); //처음 정원은 3명: 정원 안은 확정 · 초과하면 신청 순서대로 대기
        members.add(member);
        return "추가 완료: ID - "+ member.getId()+", 이름 - "+member.getName()+", 대기 상태 - "+(member.isConfirmed() ? "확정" : "대기"); //신청 성공: 프로그램이 고유 id 부여 · id·이름·확정/대기 반환
    }

    public String list() { //명단
        if (members.isEmpty()) {
            return "참가자가 없습니다";
        } //명단이 비었을 때: “참가자가 없습니다” 반환

        String result = "";
        for (StudyMember m : members) {
            result += m.getId()+". "+m.getName() + " ("+(m.isConfirmed() ? "확정" : "대기")+"), ";
        }
        return result;
    } //명단 보관: Service의 List 또는 Map 사용 · 서버 재시작 시 초기화

    public String cancel(String memberId){ //취소
        //없는 id·빈 값·숫자 아님: 실패 이유 안내 · 명단 전체 유지
        if (memberId == null || memberId.strip().isEmpty()) {
            return "잘못된 ID입니다.";
        } //빈 값

        long id;
        try {
            id = Long.parseLong(memberId.strip());
        } catch (NumberFormatException e) {
            return "ID는 숫자여야 합니다.";
        } //숫자 아님

        StudyMember tempMember = null;
        for (StudyMember m:members){
            if (m.getId()==id){
                tempMember = m;
                break;
            }
        }
        if (tempMember == null) {
            return "존재하지 않는 ID입니다.";
        } //없는 ID

        //확정 참가자 취소: 삭제 후 가장 먼저 신청한 대기자 1명을 확정으로 변경
        //대기자 취소: 그 대기자만 삭제 · 다른 사람의 상태 유지
        members.remove(tempMember);
        if (tempMember.isConfirmed()) {
            for (StudyMember m : members) {
                if (!m.isConfirmed()) {
                    m.setConfirmed(true);
                    break;
                }
            }
        }
        return tempMember.getName() + "님 취소 완료.";
    }

    public String changeCapacity(String cap){ //정원
        //0·음수·11 이상·숫자 아님: 변경 거절 · 같은 정원 재요청은 명단 변화 없음
        if (cap == null || cap.strip().isEmpty()) {
            return "정원을 입력해주세요.";
        } //빈 값

        int tempCap;
        try {
            tempCap = Integer.parseInt(cap.strip());
        } catch (NumberFormatException e) {
            return "정원은 정수여야 합니다.";
        } //숫자 아님

        if (tempCap < 1 || tempCap > 10) {
            return "정원은 1~10 사이여야 합니다.";
        } //0·음수·11 이상

        int count = 0;
        for (StudyMember m : members) {
            if (m.isConfirmed()) {
                count++;
            }
        }
        if (tempCap < count) {
            return "현재 확정 인원보다 작게 변경할 수 없습니다.";
        }//현재 확정 인원보다 작으면: 변경 거절 · 기존 정원과 명단 유지

        //정원을 늘리면: 빈자리 수만큼 대기자를 신청 순서대로 확정으로 변경
        capacity = tempCap;
        int readyCount = 0;
        for (StudyMember m : members) {
            if (count >= capacity) {
                break;
            }
            if (!m.isConfirmed()) {
                m.setConfirmed(true);
                count++;
                readyCount++;
            }
        }

        return "정원이 " + capacity + "명으로 변경되었습니다.";
    }
}

