package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.auth;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqLoginDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqRegisterDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.error.IdInvalidException;

class UserServiceTests {
    private static final String LOGIN_EMAIL = "login@example.com";
    private static final String RAW_PASSWORD = "secret123";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final UserService userService = new UserService(passwordEncoder, userRepository);

    @Test
    void registerShouldHashPasswordBeforeSaving() {
        Mockito.when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        Mockito.when(userRepository.save(ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(new ReqRegisterDTO("new@example.com", RAW_PASSWORD));

        Mockito.verify(userRepository).save(ArgumentMatchers.argThat(user ->
                passwordEncoder.matches(RAW_PASSWORD, user.getPassword())
                        && !RAW_PASSWORD.equals(user.getPassword())));
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        Mockito.when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        ReqRegisterDTO request = new ReqRegisterDTO("exists@example.com", RAW_PASSWORD);

        Assertions.assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IdInvalidException.class)
                .hasMessage("Email đã tồn tại trong hệ thống");
    }

    @Test
    void authenticateShouldReturnUserWhenPasswordMatches() {
        User user = newUser(LOGIN_EMAIL, passwordEncoder.encode(RAW_PASSWORD));
        Mockito.when(userRepository.findByEmail(LOGIN_EMAIL)).thenReturn(Optional.of(user));

        User authenticatedUser = userService.authenticate(new ReqLoginDTO(LOGIN_EMAIL, RAW_PASSWORD));

        Assertions.assertThat(authenticatedUser).isSameAs(user);
    }

    @Test
    void authenticateShouldRejectWrongPassword() {
        User user = newUser(LOGIN_EMAIL, passwordEncoder.encode(RAW_PASSWORD));
        Mockito.when(userRepository.findByEmail(LOGIN_EMAIL)).thenReturn(Optional.of(user));

        Assertions.assertThatThrownBy(() -> userService.authenticate(new ReqLoginDTO(LOGIN_EMAIL, "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void updateUserRefreshTokenShouldPersistToken() {
        User user = newUser("refresh@example.com", passwordEncoder.encode(RAW_PASSWORD));
        Mockito.when(userRepository.findByEmail("refresh@example.com")).thenReturn(Optional.of(user));

        userService.updateUserRefreshToken("refresh-token", "refresh@example.com");

        Assertions.assertThat(user.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void logoutShouldClearRefreshToken() {
        User user = newUser("logout@example.com", passwordEncoder.encode(RAW_PASSWORD));
        user.setRefreshToken("refresh-token");
        Mockito.when(userRepository.findByEmail("logout@example.com")).thenReturn(Optional.of(user));

        userService.handleLogOutUser("logout@example.com");

        Assertions.assertThat(user.getRefreshToken()).isNull();
    }

    private User newUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
