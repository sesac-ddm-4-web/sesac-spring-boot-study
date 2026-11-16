package com.example.personal_project.infrastructure.repository.study;

import com.example.personal_project.domain.study.model.Member;
import com.example.personal_project.domain.study.model.MemberRole;
import com.example.personal_project.domain.study.repository.MemberRepository;
import java.util.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMemberRepository implements MemberRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public JdbcMemberRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Member member(long id) { return memberQuery("id = :value", id); }
    public Member memberByLogin(String login) { return memberQuery("login_id = :value", login); }
    private Member memberQuery(String condition, Object value) {
        var rows = jdbc.query("SELECT id,login_id,password_hash,display_name,role FROM members WHERE " + condition,
                Map.of("value", value), (r,n) -> new Member(r.getLong(1),r.getString(2),r.getString(3),r.getString(4),MemberRole.valueOf(r.getString(5))));
        return rows.isEmpty() ? null : rows.getFirst();
    }

}
