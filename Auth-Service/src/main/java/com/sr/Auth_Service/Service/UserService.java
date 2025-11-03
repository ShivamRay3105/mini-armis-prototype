package com.sr.Auth_Service.Service;

import org.springframework.stereotype.Service;

import com.sr.Auth_Service.Auth.AuthRequestDTO;
import com.sr.Auth_Service.Auth.UserDTO;


@Service
public interface UserService {
    UserDTO register(AuthRequestDTO dto);

    UserDTO authenticate(AuthRequestDTO dto);
}
