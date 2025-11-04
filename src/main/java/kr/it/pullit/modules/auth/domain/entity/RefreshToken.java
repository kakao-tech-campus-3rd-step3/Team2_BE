package kr.it.pullit.modules.auth.domain.entity;

import kr.it.pullit.modules.member.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("refreshToken")
public class RefreshToken {

  @Id private Long memberId;

  @Indexed private String token;

  private String email;

  private String role;

  @TimeToLive private Long expiration;

  public static RefreshToken of(
      Long memberId, String token, String email, Role role, Long expiration) {
    return new RefreshToken(memberId, token, email, role.name(), expiration);
  }
}
