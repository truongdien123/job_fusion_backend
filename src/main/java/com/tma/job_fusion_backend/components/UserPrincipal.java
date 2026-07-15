package com.tma.job_fusion_backend.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private UUID id;
    private String email;
    private UUID tenantId;
    private String fullName;
    private String role;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (StringUtils.isEmpty(role)) {
            return List.of();
        }
        return Arrays.stream(role.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public boolean hasRole(String roleName) {
        if (StringUtils.isEmpty(role)) {
            return false;
        }
        return Arrays.stream(role.split(","))
                .map(String::trim)
                .anyMatch(roleName::equalsIgnoreCase);
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
