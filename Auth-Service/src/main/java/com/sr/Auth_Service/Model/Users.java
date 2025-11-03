// package com.sr.Auth_Service.Model;

// import java.time.LocalDateTime;
// import java.util.Set;

// import jakarta.persistence.*;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Entity
// @AllArgsConstructor
// @NoArgsConstructor
// @Data
// @Table(name = "users", uniqueConstraints = {
//         @UniqueConstraint(columnNames = "username") // username must be unique
// })
// public class Users {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @NotBlank(message = "Username cannot be empty")
//     @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
//     @Column(nullable = false, length = 50)
//     private String username;

//     @NotBlank(message = "Password cannot be empty")
//     @Size(min = 4, message = "Password must be at least 4 characters")
//     @Column(nullable = false)
//     private String password;

//     @ElementCollection(fetch = FetchType.EAGER)
//     @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "users_id"))
//     @Column(name = "roles")
//     private Set<String> roles;

//     @Column(name = "created_at", nullable = false, updatable = false)
//     private LocalDateTime createdAt;

//     @Column(name = "updated_at")
//     private LocalDateTime updatedAt;

//     @PrePersist
//     protected void onCreate() {
//         this.createdAt = LocalDateTime.now();
//     }

//     @PreUpdate
//     protected void onUpdate() {
//         this.updatedAt = LocalDateTime.now();
//     }

// }

package com.sr.Auth_Service.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // 👈 Import this

import java.time.LocalDateTime;
import java.util.Collection; // 👈 Import this
import java.util.Set;
import java.util.stream.Collectors; // 👈 Import this

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
// ✨ Implement the UserDetails interface
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    @Column(nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 4, message = "Password must be at least 4 characters")
    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER) // Fetch roles eagerly with the user
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "users_id"))
    @Column(name = "roles")
    private Set<String> roles;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ✨ --- UserDetails Methods --- ✨
    // These methods provide user details to Spring Security

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // This is the crucial part: we convert our Set<String> of roles
        // into a Collection of SimpleGrantedAuthority objects.
        // Spring Security uses these authorities to perform role checks.
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        // For simplicity, we'll return true. You could add logic here
        // to check if a user's subscription has expired, for example.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Return true if the account is not locked.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Return true if the password is not expired.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Return true if the user account is enabled.
        return true;
    }
}