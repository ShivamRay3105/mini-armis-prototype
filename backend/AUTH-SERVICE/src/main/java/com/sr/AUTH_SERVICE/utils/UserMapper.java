package com.sr.AUTH_SERVICE.utils;

import com.sr.AUTH_SERVICE.DB.User;
import com.sr.AUTH_SERVICE.dto.AuthResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps the User entity and a generated token into an immutable AuthResponse
     * record.
     * Note: MapStruct 1.6.0 handles Java Records automatically using their accessor
     * methods.
     */
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "accessToken", source = "token")
    AuthResponse toAuthResponse(User user, String token);
}