package kr.it.pullit.modules.auth.repository;

import java.util.Optional;
import kr.it.pullit.modules.auth.domain.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);
}
