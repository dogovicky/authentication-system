package ke.co.legalbridge.authservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID userId; // Foreign Key to Users table

    @Column(nullable = false, unique = true, length = 500)
    private String refreshToken;

    @Column(length = 500)
    private String deviceInfo;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean isRevoked = false; // For manual logout

    private LocalDateTime revokedAt;

    private LocalDateTime lastUsedAt; // Track when the refresh token was last used

    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
    }

}
