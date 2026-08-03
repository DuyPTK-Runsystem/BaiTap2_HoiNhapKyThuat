package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqLoginDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqRegisterDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResLoginDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResUserDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.UserService;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.SecurityUtil;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${app.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    @PostMapping("/register")
    @ApiMessage("Đăng ký tài khoản")
    public ResponseEntity<ResUserDTO> register(@Valid @RequestBody ReqRegisterDTO request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    @ApiMessage("Đăng nhập")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO request) {
        User user = userService.authenticate(request);
        ResUserDTO userDTO = userService.convertToDTO(user);
        String accessToken = securityUtil.createAccessToken(user.getEmail(), userDTO);
        String refreshToken = securityUtil.createRefreshToken(user.getEmail(), userDTO);

        userService.updateUserRefreshToken(refreshToken, user.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshToken, refreshTokenExpiration))
                .body(ResLoginDTO.builder()
                        .accessToken(accessToken)
                        .user(userDTO)
                        .build());
    }

    @GetMapping("/account")
    @ApiMessage("Lấy thông tin tài khoản")
    public ResponseEntity<ResUserDTO> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadCredentialsException("Không xác định được tài khoản hiện tại"));
        User user = userService.handleFindByEmail(email);
        if (user == null) {
            throw new BadCredentialsException("Không xác định được tài khoản hiện tại");
        }
        return ResponseEntity.ok(userService.convertToDTO(user));
    }

    @GetMapping("/refresh")
    @ApiMessage("Lấy token mới bằng refresh token")
    public ResponseEntity<ResLoginDTO> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("Refresh token không hợp lệ");
        }

        Jwt decodedToken = securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();
        User user = userService.handleFindByEmailAndRefreshToken(email, refreshToken);
        if (user == null) {
            throw new BadCredentialsException("Refresh token không hợp lệ");
        }

        ResUserDTO userDTO = userService.convertToDTO(user);
        String accessToken = securityUtil.createAccessToken(email, userDTO);
        String newRefreshToken = securityUtil.createRefreshToken(email, userDTO);
        userService.updateUserRefreshToken(newRefreshToken, email);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(newRefreshToken, refreshTokenExpiration))
                .body(ResLoginDTO.builder()
                        .accessToken(accessToken)
                        .user(userDTO)
                        .build());
    }

    @PostMapping("/logout")
    @ApiMessage("Đăng xuất")
    public ResponseEntity<Void> logout() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadCredentialsException("Không xác định được tài khoản hiện tại"));
        userService.handleLogOutUser(email);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie("", 0L))
                .build();
    }

    private String buildRefreshTokenCookie(String refreshToken, long maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build()
                .toString();
    }
}
