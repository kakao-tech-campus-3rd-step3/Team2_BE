package kr.it.pullit.modules.auth.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import kr.it.pullit.modules.auth.domain.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}
