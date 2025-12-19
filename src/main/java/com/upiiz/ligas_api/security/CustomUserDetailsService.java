package com.upiiz.ligas_api.security;

import com.upiiz.ligas_api.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("No existe el usuario"));
        var auths = u.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).toList();
        return new org.springframework.security.core.userdetails.User(u.getEmail(), u.getPassword(), auths);
    }
}
