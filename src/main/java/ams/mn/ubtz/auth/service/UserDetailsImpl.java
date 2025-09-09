package ams.mn.ubtz.auth.service;


import ams.mn.ubtz.auth.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Хэрэглэгчийн permission-г ашиглаж эрх олгох боломжтой,
        // одоогоор ямар ч эрх олгохгүй байна
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPass();
    }

    @Override
    public String getUsername() {
        return user.getUname();
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
        return true;
    }
    
    
}
