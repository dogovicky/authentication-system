package ke.co.legalbridge.Auth_Service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import ke.co.legalbridge.sharedlibraries.enums.UserType;
import lombok.Getter;

@Getter
public class SignUpRequestDTO {

    @Email(message = "Please enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Create password")
    //@Pattern(regexp = "[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]")
    private String password;

    //@NotBlank
    private UserType userType;

}
