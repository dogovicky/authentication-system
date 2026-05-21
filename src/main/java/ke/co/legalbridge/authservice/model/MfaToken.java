package ke.co.legalbridge.authservice.model;

import jakarta.persistence.*;
import ke.co.legalbridge.authservice.enumerations.MfaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mfa_tokens")
public class MfaToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private UUID id;

    private UUID userId;
    private String otp;
    private String deviceInfo;
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private MfaStatus MfaStatus;

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

}
