package ke.co.legalbridge.authservice.mappers;

import ke.co.legalbridge.authservice.dto.ResponseDTO;
import ke.co.legalbridge.authservice.dto.SignUpRequestDTO;
import ke.co.legalbridge.authservice.model.User;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AuthMapper {

    /*
     * Map SignUpRequestDTO to User entity
     * Password is handled separately in service (encoding)
     */

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "verified", constant = "false")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lockedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User signUpRequestToUser(SignUpRequestDTO dto);

    /*
     * Map User entity to RegistrationResponseDTO
     * Tokens are added separately in service
     */

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "userType", target = "userType")
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    ResponseDTO userToResponse(User user);

    /*
     * Map LoginRequest To User
     */
//    @Mapping(target = "email")
//    @Mapping(target = "passwordHash")
//    User loginRequestDTO(LoginRequestDTO dto);

    /*
     * Add tokens to response DTO
     */
    @AfterMapping
    default void addTokens(@MappingTarget ResponseDTO.ResponseDTOBuilder builder, String accessToken, String refreshToken, long expiresIn) {
        builder.accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn);
    }

}
