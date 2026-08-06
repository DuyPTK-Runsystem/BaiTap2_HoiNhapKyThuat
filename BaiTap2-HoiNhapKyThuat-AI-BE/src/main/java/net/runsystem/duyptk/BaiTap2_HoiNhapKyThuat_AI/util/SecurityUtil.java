package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResUserDTO;

@Service
@RequiredArgsConstructor
public class SecurityUtil {
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.base64-secret}")
    private String jwtKey;

    @Value("${app.jwt.access-token-validity-in-seconds}")
    private Long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    public String createAccessToken(String email, ResUserDTO user) {
        return createToken(email, user, accessTokenExpiration);
    }

    public String createRefreshToken(String email, ResUserDTO user) {
        return createToken(email, user, refreshTokenExpiration);
    }

    public Jwt checkValidRefreshToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM)
                .build();
        return jwtDecoder.decode(token);
    }

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private String createToken(String email, ResUserDTO user, long expirationInSeconds) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(expirationInSeconds);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expirationTime)
                .subject(email)
                .claim("user", userClaims(user))
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private static Map<String, Object> userClaims(ResUserDTO user) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        return claims;
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        if (authentication.getPrincipal() instanceof String principal) {
            return principal;
        }
        return null;
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }
}
