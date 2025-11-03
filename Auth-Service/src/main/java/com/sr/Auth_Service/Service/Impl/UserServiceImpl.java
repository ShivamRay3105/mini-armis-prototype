// package com.sr.Auth_Service.Service.Impl;

// import java.util.Optional;
// import java.util.Set;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.sr.Auth_Service.Auth.AuthRequestDTO;
// import com.sr.Auth_Service.Auth.UserDTO;
// import com.sr.Auth_Service.Model.Users;
// import com.sr.Auth_Service.Repository.UserRepository;
// import com.sr.Auth_Service.Service.UserService;

// @Service
// public class UserServiceImpl implements UserService {

//     @Autowired
//     private UserRepository userRepository;

//     @Override
//     public UserDTO register(AuthRequestDTO dto) {
//         Users user = new Users();
//         user.setUsername(dto.getUsername());
//         user.setPassword(dto.getPassword()); // later you can encode this with BCrypt
//         user.setRoles(Set.of("USER")); // default role or pass in DTO

//         Users savedUser = userRepository.save(user);
//         return mapToDTO(savedUser);
//     }

//     @Override
//     public UserDTO authenticate(AuthRequestDTO dto) {
//         Optional<Users> userOpt = userRepository.findByUsername(dto.getUsername());
//         if (userOpt.isPresent() && userOpt.get().getPassword().equals(dto.getPassword())) {
//             return mapToDTO(userOpt.get());
//         }
//         return null; 
//     }

//     private UserDTO mapToDTO(Users user) {
//         return new UserDTO(user.getId(), user.getUsername(), user.getRoles());
//     }

// }

package com.sr.Auth_Service.Service.Impl;

import com.sr.Auth_Service.Auth.AuthRequestDTO;
import com.sr.Auth_Service.Auth.UserDTO;
import com.sr.Auth_Service.Model.Users;
import com.sr.Auth_Service.Repository.UserRepository;
import com.sr.Auth_Service.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 👈 Import this
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // ✨ Inject the password encoder

    @Override
    public UserDTO register(AuthRequestDTO dto) {
        Users user = new Users();
        user.setUsername(dto.getUsername());

        // ✨ ALWAYS encode the password before saving!
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Assign default role. You can make this more flexible if needed.
        user.setRoles(Set.of("USER"));

        Users savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    // ✨ This method is insecure and redundant. It should be REMOVED.
    // Authentication is handled by Spring Security's AuthenticationManager.
    @Override
    public UserDTO authenticate(AuthRequestDTO dto) {
        // The AuthenticationManager configured in SecurityConfig will handle this.
        // This manual check is a security risk because it bypasses the proper flow
        // and uses a plain text password comparison.
        throw new UnsupportedOperationException("Authentication should be handled by AuthenticationManager");
    }

    private UserDTO mapToDTO(Users user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getRoles());
    }
}