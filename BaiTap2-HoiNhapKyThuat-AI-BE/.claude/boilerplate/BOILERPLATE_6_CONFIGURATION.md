# KiemThuPhanMem - Configuration & Main Application

## Configuration Files

### 1. SecurityConfiguration.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/config/SecurityConfiguration.java`

```java
package com.uit.nhom7.KiemThuPhanMem.config;

import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.util.Base64;
import com.uit.nhom7.KiemThuPhanMem.util.SecurityUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {
    @Value("${se113.jwt.base64-secret}")
    private String jwtKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        return token -> {
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                System.out.println(">>>> JWT error: " + e.getMessage());
                throw e;
            }
        };
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length,
                SecurityUtil.JWT_ALGORITHM.getName());
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("permission");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {

        String[] whiteList = {
                "/",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/register",
                "/actuator/**",
                "/api/v1/actuator/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/storage/**"
        };
        
        http
                .csrf(c -> c.disable())
                .authorizeHttpRequests(
                        authz -> authz
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(whiteList).permitAll()
                                .anyRequest().permitAll())
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .formLogin(f -> f.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
```

**Key Configuration**:
- **JWT Decoder & Encoder**: Decode/encode JWT tokens
- **Password Encoder**: BCrypt password hashing
- **CORS Configuration**: Allow requests from `http://localhost:5173`
- **Security Filter Chain**: Define public/protected endpoints
- **Session Management**: Stateless (no server sessions)

---

### 2. OpenAPIConfig.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/config/OpenAPIConfig.java`

```java
package com.uit.nhom7.KiemThuPhanMem.config;

import org.springframework.context.annotation.Configuration;
import java.util.List;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    private Server createServer(String url, String description) {
        Server server = new Server();
        server.setUrl(url);
        server.setDescription(description);
        return server;
    }

    private Contact createContact() {
        return new Contact()
                .email("ads.hoidanit@gmail.com")
                .name("Hỏi Dân IT")
                .url("https://hoidanit.vn");
    }

    private License createLicense() {
        return new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");
    }

    private Info createApiInfo() {
        return new Info()
                .title("Nhom 7 - KiemThuPhanMem API")
                .version("1.0")
                .description("REST API for Software Testing System")
                .license(createLicense());
    }

    @Bean
    public OpenAPI myOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(List.of(
                        createServer("http://localhost:8081", "Development"),
                        createServer("https://api.example.com", "Production")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }
}
```

**Swagger/OpenAPI Access**:
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- OpenAPI YAML: `http://localhost:8081/v3/api-docs.yaml`

---

### 3. CustomAuthenticationEntryPoint.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/config/CustomAuthenticationEntryPoint.java`

```java
package com.uit.nhom7.KiemThuPhanMem.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.RestResponse;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        String authHeader = request.getHeader("Authorization");
        String jwtToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }

        System.out.println(">>> Authorization Header: " + authHeader);
        System.out.println(">>> JWT Token: " + jwtToken);
        System.out.println(">>> Exception Message: " + authException.getMessage());

        this.delegate.commence(request, response, authException);
        response.setContentType("application/json;charset=UTF-8");

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        String errorMessage = Optional.ofNullable(authException.getCause())
                .map(Throwable::getMessage)
                .orElse(authException.getMessage());

        res.setError(errorMessage);
        res.setMessage("Token is invalid or expired");

        mapper.writeValue(response.getWriter(), res);
    }
}
```

---

## Main Application Class

### KiemThuPhanMemApplication.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/KiemThuPhanMemApplication.java`

```java
package com.uit.nhom7.KiemThuPhanMem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KiemThuPhanMemApplication {

    public static void main(String[] args) {
        SpringApplication.run(KiemThuPhanMemApplication.class, args);
    }
}
```

---

## application.properties

**Path**: `src/main/resources/application.properties`

```properties
# Application
spring.application.name=KiemThuPhanMem
server.port=8081

# Database
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://localhost:3306/se113
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.show-sql=true

# JWT Configuration
se113.jwt.base64-secret=${JWT_SECRET:noVGO4KXfRQijWLkkHTdwMZzJcsvohOLNTzXHkWOEOwwj50/QWunAGce8b6XKqUwss6ozCb5A/e++2SPZN/d2Q==}
se113.jwt.access-token-validity-in-seconds=864000
se113.jwt.refresh-token-validity-in-seconds=864000

# File Upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Development Tools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true

# Pagination
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=2000
spring.data.web.pageable.one-indexed-parameters=true

# Actuator
management.endpoints.web.exposure.include=health,info,metrics,env,beans,mappings
management.endpoint.health.show-details=when-authorized
management.health.defaults.enabled=true
```

---

## build.gradle.kts

**Path**: `build.gradle.kts`

```kotlin
plugins {
	java
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.uit.nhom7"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("com.github.f4b6a3:uuid-creator:5.3.3")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
	
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.mysql:mysql-connector-j")
	annotationProcessor("org.projectlombok:lombok")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
```

---

## Key Beans & Configurations

| Bean                         | Purpose                       |
| ---------------------------- | ----------------------------- |
| `PasswordEncoder`            | BCrypt password hashing       |
| `JwtEncoder`                 | Create JWT tokens             |
| `JwtDecoder`                 | Decode/validate JWT tokens    |
| `JwtAuthenticationConverter` | Convert JWT to authentication |
| `SecurityFilterChain`        | Define security rules         |
| `CorsConfigurationSource`    | CORS configuration            |
| `OpenAPI`                    | Swagger documentation         |
