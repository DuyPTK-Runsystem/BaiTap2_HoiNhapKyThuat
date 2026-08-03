# Developer Plan Summary

## 1. Mục đích

File này là mục lục và bản tóm tắt của toàn bộ Developer Plan trong thư mục `.claude/dev_plan`.

Mỗi khi thêm Developer Plan mới, cần cập nhật file này với:

- Tên và đường dẫn file.
- Module/feature.
- Mục tiêu chính.
- Phạm vi bao gồm và loại trừ.
- API/database bị ảnh hưởng nếu có.
- Trạng thái phê duyệt.
- Trạng thái triển khai.

## 2. Quy tắc quản lý Developer Plan

- Developer Plan phải được tạo trước khi chỉnh sửa mã nguồn cho feature tương ứng.
- Không được triển khai code khi Developer Plan chưa được người dùng phê duyệt rõ ràng.
- Developer Plan phải phù hợp với requirement, kiến trúc và workflow của project.
- Nếu phát hiện phạm vi hoặc phương án không còn phù hợp, phải cập nhật plan và xin phê duyệt lại.
- Không tự ý thêm feature, dependency, refactor diện rộng hoặc thay đổi kiến trúc ngoài plan đã duyệt.
- Sau khi triển khai, cần đối chiếu code thực tế với Developer Plan và cập nhật trạng thái.

## 3. Danh sách Developer Plan

| Developer Plan                                                                           | Module/Feature                                | Trạng thái phê duyệt | Trạng thái triển khai | Tóm tắt                                                                                                           |
| ---------------------------------------------------------------------------------------- | --------------------------------------------- | -------------------- | --------------------- | ----------------------------------------------------------------------------------------------------------------- |
| [`auth_authz/Auth_Authz_DeveloperPlan.md`](auth_authz/Auth_Authz_DeveloperPlan.md)       | Authentication và authorization tối giản      | Đã phê duyệt         | Đã triển khai         | Cấu hình Spring Boot, User, JWT, BCrypt, register/login/refresh/account/logout; không có Roles và Permissions     |
| [`auth_authz/Auth_UnitTest_DeveloperPlan.md`](auth_authz/Auth_UnitTest_DeveloperPlan.md) | Unit Test cho Authentication và Authorization | Đã phê duyệt         | Đã triển khai         | JUnit test cho `UserService` và HTML report có test case, module và coverage; không test app/repo/`SecurityUtil` |

## 4. Tóm tắt từng Developer Plan

### 4.1. Authentication và Authorization tối giản

**File:** [`auth_authz/Auth_Authz_DeveloperPlan.md`](auth_authz/Auth_Authz_DeveloperPlan.md)

**Mục tiêu:**

- Cấu hình project theo boilerplate.
- Xây dựng nền tảng Authentication bằng JWT và BCrypt.
- Bảo vệ endpoint bằng trạng thái authenticated.

**Bao gồm:**

- Build/dependency configuration.
- User entity và UserRepository.
- UserService.
- Request/response DTO.
- AuthController.
- Register, login, account, refresh và logout.
- JWT, BCrypt, CORS, OpenAPI và exception handling.
- Unit/integration test cho Auth và Security.

**Không bao gồm:**

- Role.
- Permission.
- Role/Permission repository, service, DTO hoặc controller.
- Quan hệ User–Role hoặc Role–Permission.
- Role/Permission claims trong JWT.
- Vocabulary, Organization và Testing implementation trong phase này.

**Các quyết định kỹ thuật dự kiến:**

- Giữ Java 21 và Spring Boot 4.0.7.
- Không bắt buộc sử dụng UUID; mặc định dự kiến dùng `Long` hoặc kiểu ID quan hệ phù hợp với schema.
- Map database column thành `user_id` và `hash_password`.
- Email unique.
- Password luôn được hash trước khi lưu.
- Các endpoint ngoài nhóm public yêu cầu JWT hợp lệ.
- CORS mặc định dự kiến là `http://localhost:5173`, chờ xác nhận.
- Secret database/JWT lấy từ environment variables.

**API dự kiến:**

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/account
GET  /api/v1/auth/refresh
POST /api/v1/auth/logout
```

**Rủi ro chính:**

- Khác biệt schema User giữa boilerplate và tài liệu kiến trúc.
- Dependency JWT có thể chưa đầy đủ.
- Cấu hình CORS và secret cần xác nhận.
- Boilerplate có cấu hình `.anyRequest().permitAll()` cần thay bằng `.anyRequest().authenticated()`.
- Phải loại bỏ toàn bộ tham chiếu Roles/Permissions.

## 5. Developer Plan trong tương lai

Chưa có Developer Plan cho các module sau:

- Vocabulary Management.
- Organization.
- Testing & Learning.
- Flashcard.
- Import `.xlsx`.

Khi tạo plan mới, cần thêm vào bảng ở mục 3 và phần tóm tắt tương ứng ở mục 4.

## 6. Lịch sử cập nhật

| Ngày       | Nội dung                                                                                                    | Người cập nhật      |
| ---------- | ----------------------------------------------------------------------------------------------------------- | ------------------- |
| 2026-08-03 | Tạo Developer Plan Summary và plan Auth/Authz ban đầu                                                       | RunSystem Assistant |
| 2026-08-03 | Cập nhật plan: project không bắt buộc sử dụng UUID                                                          | RunSystem Assistant |
| 2026-08-03 | Người dùng phê duyệt plan Auth/Authz; cho phép bắt đầu triển khai                                           | Người dùng          |
| 2026-08-03 | Cập nhật trạng thái Auth/Authz sang đã triển khai                                                           | Codex               |
| 2026-08-03 | Bổ sung audit fields cho entity Auth/User theo yêu cầu người dùng                                           | Codex               |
| 2026-08-03 | Tạo Developer Plan cho Unit Test Auth/Authz để ghi nhận trạng thái tạm dừng do chưa có phê duyệt triển khai | Codex               |
| 2026-08-03 | Di chuyển Developer Plan Unit Test vào thư mục `auth_authz` theo yêu cầu người dùng                         | Codex               |
| 2026-08-03 | Cập nhật và triển khai Unit Test Auth/Authz bằng JUnit, bổ sung PDF report generator                        | Codex               |
| 2026-08-03 | Điều chỉnh Unit Test Auth/Authz: thay PDF report bằng HTML report có danh sách test, module và coverage     | Codex               |
| 2026-08-03 | Điều chỉnh HTML Unit Test report: coverage chỉ filter theo module cần test                                  | Codex               |
| 2026-08-03 | Cập nhật plan Auth/Authz cho refactor Lombok theo convention boilerplate                                    | Codex               |
