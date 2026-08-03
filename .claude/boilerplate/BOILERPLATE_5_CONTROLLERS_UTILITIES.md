# KiemThuPhanMem - Controllers & Utilities

## Controllers

### 1. AuthController.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/controller/AuthController.java`

```java
package com.uit.nhom7.KiemThuPhanMem.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uit.nhom7.KiemThuPhanMem.domain.requestDTO.ReqLoginDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.ResLoginDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.table.User;
import com.uit.nhom7.KiemThuPhanMem.service.UserService;
import com.uit.nhom7.KiemThuPhanMem.util.SecurityUtil;
import com.uit.nhom7.KiemThuPhanMem.util.annotation.ApiMessage;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${se113.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityUtil securityUtil, UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    /**
     * POST /api/v1/auth/login - Đăng nhập
     */
    @PostMapping("/login")
    @ApiMessage("Đăng nhập")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
        System.out.println(">>>AUTH MODULE: Login attempt for email: " + loginDTO.getEmail());
        
        UsernamePasswordAuthenticationToken authenticationToken = 
            new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        User currentUserDB = this.userService.handleFindByEmail(loginDTO.getEmail());
        if (currentUserDB == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName());
        resLoginDTO.setUser(userLogin);
        
        if (currentUserDB.getRole() != null) {
            resLoginDTO.setRole(new ResLoginDTO.Role(
                    currentUserDB.getRole().getId(),
                    currentUserDB.getRole().getName()));
        }

        String accessToken = securityUtil.createAccessToken(loginDTO.getEmail(), resLoginDTO);
        String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getEmail(), resLoginDTO);
        
        resLoginDTO.setAccessToken(accessToken);
        this.userService.updateUserRefreshToken(refreshToken, loginDTO.getEmail());

        ResponseCookie resCookies = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        System.out.println(">>>AUTH MODULE: Login successful for email: " + loginDTO.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(resLoginDTO);
    }

    /**
     * GET /api/v1/auth/account - Lấy thông tin tài khoản hiện tại
     */
    @GetMapping("/account")
    @ApiMessage("Lấy thông tin tài khoản")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        System.out.println(">>>AUTH MODULE: Fetching account information");

        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentUserDB = this.userService.handleFindByEmail(email);
        if (currentUserDB == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName());

        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        userGetAccount.setUser(userLogin);

        if (currentUserDB.getRole() != null) {
            userGetAccount.setRole(new ResLoginDTO.Role(
                    currentUserDB.getRole().getId(),
                    currentUserDB.getRole().getName()));
        }
        return ResponseEntity.ok(userGetAccount);
    }

    /**
     * GET /api/v1/auth/refresh - Refresh access token
     */
    @GetMapping("/refresh")
    @ApiMessage("Lấy token mới bằng refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "No cookies") String refreshToken)
            throws BadRequestException {
        System.out.println(">>>AUTH MODULE: Refresh token attempt");
        if (refreshToken.equals("No cookies")) {
            System.out.println(">>>AUTH MODULE: No refresh token provided");
            throw new BadRequestException("No refresh token provided");
        }
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        User currentUser = this.userService.handleFindByEmailAndRefreshToken(email, refreshToken);
        if (currentUser == null) {
            System.out.println(">>>AUTH MODULE: Invalid refresh token for email: " + email);
            throw new BadRequestException("Invalid refresh token");
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName());

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setUser(userLogin);

        if (currentUser.getRole() != null) {
            resLoginDTO.setRole(new ResLoginDTO.Role(
                    currentUser.getRole().getId(),
                    currentUser.getRole().getName()));
        }

        String accessToken = securityUtil.createAccessToken(email, resLoginDTO);
        String newRefreshToken = this.securityUtil.createRefreshToken(currentUser.getEmail(), resLoginDTO);

        resLoginDTO.setAccessToken(accessToken);
        this.userService.updateUserRefreshToken(newRefreshToken, currentUser.getEmail());

        ResponseCookie resCookies = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        System.out.println(">>>AUTH MODULE: Refresh token successful for email: " + email);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(resLoginDTO);
    }

    /**
     * POST /api/v1/auth/logout - Đăng xuất
     */
    @PostMapping("/logout")
    @ApiMessage("Đăng xuất")
    public ResponseEntity<Void> logout() throws BadRequestException {
        System.out.println(">>>AUTH MODULE: Logout attempt");

        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        if (email == null) {
            throw new BadRequestException("No user logged in");
        }

        this.userService.handleLogOutUser(email);

        ResponseCookie deleteCookies = ResponseCookie.from("refresh_token", null)
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        System.out.println(">>>AUTH MODULE: Logout successful for email: " + email);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookies.toString())
                .build();
    }
}
```

**Endpoints**:
- `POST /api/v1/auth/login` - Login
- `GET /api/v1/auth/account` - Get current user
- `GET /api/v1/auth/refresh` - Refresh token
- `POST /api/v1/auth/logout` - Logout

---

## Utilities

### 1. SecurityUtil.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/util/SecurityUtil.java`

```java
package com.uit.nhom7.KiemThuPhanMem.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

import com.nimbusds.jose.util.Base64;
import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.ResLoginDTO;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Service
public class SecurityUtil {
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.from("HS256");

    private final JwtEncoder jwtEncoder;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Value("${se113.jwt.base64-secret}")
    private String jwtKey;

    @Value("${se113.jwt.access-token-validity-in-seconds}")
    private Long accessTokenExpiration;

    @Value("${se113.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    /**
     * Create access token
     */
    public String createAccessToken(String email, ResLoginDTO dto) {
        ResLoginDTO.UserInsideToken userInsideToken = new ResLoginDTO.UserInsideToken();
        userInsideToken.setId(dto.getUser().getId());
        userInsideToken.setEmail(dto.getUser().getEmail());
        userInsideToken.setName(dto.getUser().getName());

        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(accessTokenExpiration);

        ResLoginDTO.Role role = dto.getRole();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expirationTime)
                .subject(email)
                .claim("user", userInsideToken)
                .claim("role", role)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    /**
     * Create refresh token
     */
    public String createRefreshToken(String email, ResLoginDTO dto) {
        ResLoginDTO.UserInsideToken userInsideToken = new ResLoginDTO.UserInsideToken();
        userInsideToken.setId(dto.getUser().getId());
        userInsideToken.setEmail(dto.getUser().getEmail());
        userInsideToken.setName(dto.getUser().getName());

        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(refreshTokenExpiration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expirationTime)
                .subject(email)
                .claim("user", userInsideToken)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    /**
     * Check if refresh token is valid
     */
    public Jwt checkValidRefreshToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        try {
            Jwt jwt = jwtDecoder.decode(token);
            System.out.println(">>>> Refresh token is valid");
            return jwt;
        } catch (Exception e) {
            System.out.println(">>>> Refresh token error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get the login of the current user
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    /**
     * Check if current user has any of authorities
     */
    public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && 
                getAuthorities(authentication)
                    .anyMatch(authority -> Arrays.asList(authorities).contains(authority)));
    }

    /**
     * Check if current user has specific authority
     */
    public static boolean hasCurrentUserThisAuthority(String authority) {
        return hasCurrentUserAnyOfAuthorities(authority);
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }
}
```

**Key Methods**:
- `createAccessToken()` - Tạo access token
- `createRefreshToken()` - Tạo refresh token
- `checkValidRefreshToken()` - Kiểm tra refresh token hợp lệ
- `getCurrentUserLogin()` - Lấy email user hiện tại
- `hasCurrentUserAnyOfAuthorities()` - Kiểm tra quyền user

---

### 2. FormatRestResponse.java

```java
package com.uit.nhom7.KiemThuPhanMem.util;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.RestResponse;
import com.uit.nhom7.KiemThuPhanMem.util.annotation.ApiMessage;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Global response formatter - tự động wrap tất cả responses
 */
@RestControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, 
            MediaType selectedContentType, Class selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        
        HttpServletResponse httpServletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int code = httpServletResponse.getStatus();

        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(code);

        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return body;
        }

        if (body instanceof String || body instanceof Resource) {
            return body;
        }

        if (code >= 400) {
            return body;
        } else {
            restResponse.setData(body);
            ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
            restResponse.setMessage(apiMessage != null ? apiMessage.value() : "CALL API SUCCESS");
        }
        return restResponse;
    }

    // Static helper methods
    public static <T> RestResponse<T> success(T data) {
        return RestResponse.<T>builder()
                .statusCode(200)
                .data(data)
                .message("Success")
                .build();
    }

    public static <T> RestResponse<T> error(String message) {
        return RestResponse.<T>builder()
                .statusCode(400)
                .error(message)
                .message(message)
                .build();
    }
}
```

### 3. UuidV7Generator.java

```java
package com.uit.nhom7.KiemThuPhanMem.util;

import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UuidV7Generator {
    public static UUID generate() {
        return UuidCreator.getTimeOrderedEpoch(); // UUID v7
    }
}
```

### 4. Custom Exception

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/util/error/IdInvalidException.java`

```java
package com.uit.nhom7.KiemThuPhanMem.util.error;

public class IdInvalidException extends RuntimeException {
    public IdInvalidException(String message) {
        super(message);
    }
}
```

### 5. Custom Annotation

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/util/annotation/ApiMessage.java`

```java
package com.uit.nhom7.KiemThuPhanMem.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiMessage {
    String value();
}
```

---

## Usage Examples

### Using @ApiMessage
```java
@GetMapping("/users")
@ApiMessage("Lấy danh sách users")
public ResponseEntity<List<ResUserDTO>> getAllUsers() {
    // ...
}
```

### Using SecurityUtil
```java
// Get current user email
String email = SecurityUtil.getCurrentUserLogin().orElse(null);

// Check if user has authority
if (SecurityUtil.hasCurrentUserThisAuthority("ROLE_ADMIN")) {
    // ...
}
```