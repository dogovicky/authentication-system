package ke.co.legalbridge.authservice.repository;

import ke.co.legalbridge.authservice.model.MfaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MfaTokenRepository extends JpaRepository<MfaToken, UUID> {

}
