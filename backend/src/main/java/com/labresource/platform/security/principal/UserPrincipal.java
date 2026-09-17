package com.labresource.platform.security.principal;

import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Long institutionId;
    private final Long departmentId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public UserPrincipal(Long id,
                         String email,
                         String password,
                         Long institutionId,
                         Long departmentId,
                         Collection<? extends GrantedAuthority> authorities,
                         boolean enabled) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.institutionId = institutionId;
        this.departmentId = departmentId;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static UserPrincipal fromUser(User user) {
        Set<Role> userRoles = user.getRoles() != null ? user.getRoles() : Collections.emptySet();
        List<GrantedAuthority> authorities = userRoles.stream()
                .filter(role -> role.getName() != null)
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        boolean isEnabled = user.getStatus() == UserStatus.ACTIVE && user.getDeletedAt() == null;
        Long instId = user.getInstitution() != null ? user.getInstitution().getId() : null;
        Long deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                instId,
                deptId,
                authorities,
                isEnabled
        );
    }

    public static UserPrincipal fromClaims(Long userId, String email, Long institutionId, List<String> roles) {
        return fromClaims(userId, email, institutionId, null, roles);
    }

    public static UserPrincipal fromClaims(Long userId, String email, Long institutionId, Long departmentId, List<String> roles) {
        List<GrantedAuthority> authorities = roles != null
                ? roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                : Collections.emptyList();

        return new UserPrincipal(
                userId,
                email,
                "",
                institutionId,
                departmentId,
                authorities,
                true
        );
    }

    public Long getId() {
        return id;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
