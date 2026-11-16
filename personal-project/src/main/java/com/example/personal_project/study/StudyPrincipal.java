package com.example.personal_project.study;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public record StudyPrincipal(Long id, String loginId, String passwordHash, String displayName, String role)
        implements UserDetails {
    public String getUsername() { return loginId; }
    public String getPassword() { return passwordHash; }
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role));
    }
    public Map<String,Object> publicInfo() { return Map.of("id",id,"displayName",displayName,"role",role); }
}
