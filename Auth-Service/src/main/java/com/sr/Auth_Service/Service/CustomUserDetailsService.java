package com.sr.Auth_Service.Service;

import com.sr.Auth_Service.Model.Users;
import com.sr.Auth_Service.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        // @Override
        // public UserDetails loadUserByUsername(String username) throws
        // UsernameNotFoundException {
        // Users user = userRepository.findByUsername(username)
        // .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // return org.springframework.security.core.userdetails.User.builder()
        // .username(user.getUsername())
        // .password(user.getPassword())
        // .authorities(user.getRoles().stream()
        // .map(SimpleGrantedAuthority::new)
        // .toList())
        // .build();
        // }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

}
