package com.example.inventory.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class SignupRequestDTO {
    @NotBlank @Size(min=3, max=50) private String username;
    @NotBlank @Email private String email;
    @NotBlank @Size(min=6) private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
}
