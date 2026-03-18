package com.example.inventory.dto;
import lombok.*;
import java.util.Set;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private Boolean isActive;
    private Set<String> roles;
}
