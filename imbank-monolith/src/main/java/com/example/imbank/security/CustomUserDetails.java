package com.example.imbank.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities (){

        return user.getRoles().stream() /// /convert to strean for processing.....
                //for ecch role object we extarct the nanr and wrap it in a SimpleGrantedAuthority

                // Role(id=1, name="ROLE_USER")
                //    → new SimpleGrantedAuthority("ROLE_USER")
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;  // Account never expires
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Passwords never expire
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    /// get the underlying user
    public User getUser() {
        return user;
    }

    public Long getId() {
        return user.getId();
    }


    public String getEmail() {
        return user.getEmail();
    }


}
