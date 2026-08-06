# Dev Plan Summary FE

## Tổng quan

| Module/Feature | Plan | Trạng thái | Cập nhật gần nhất | Ghi chú |
|---|---|---|---|---|
| Project Foundation | `.claude/dev_plan/ProjectFoundation-FE.md` | Hoàn thành | 2026-08-06 | Đã dựng nền tảng cấu trúc FE, API client/service/type, auth session và app shell tối thiểu; lint/build pass |
| Auth & Authz | `.claude/dev_plan/AuthAuthz-FE.md` | Hoàn thành | 2026-08-06 | Đã hoàn thiện login/register/session refresh/logout, authz boundary, follow-up label `(*)`, link cùng dòng và logo; lint/build pass |
| Routing & Navigation | `.claude/dev_plan/RoutingNavigation-FE.md` | Hoàn thành | 2026-08-06 | Đã triển khai route map và route guards bằng `react-router-dom`; lint/build pass; npm audit còn 2 high severity vulnerabilities |
| Library / Organization | `.claude/dev_plan/organization/Library-FE.md` | Hoàn thành | 2026-08-06 | Đã triển khai cây Folder/VocabSet, create, search/path và add Vocab theo API Guide; VocabSet detail chỉ summary/action vì chưa có endpoint danh sách vocab; lint/build pass |

## Quy ước trạng thái

- `Chờ phê duyệt`: Plan đã được lập/cập nhật, chưa được phép triển khai source code.
- `Đã phê duyệt`: Người dùng đã xác nhận rõ ràng, được phép triển khai đúng phạm vi plan.
- `Đang triển khai`: Đang code theo plan đã duyệt.
- `Hoàn thành`: Đã triển khai, kiểm tra và báo cáo kết quả.
- `Tạm dừng`: Cần quyết định hoặc thông tin bổ sung.

## Ghi chú chung

- `Frontend_API_Guide.md` là nguồn sự thật chính cho API contract.
- Postman collection chỉ dùng để đối chiếu request và hỗ trợ kiểm tra.
- Development Plan Frontend được lưu riêng theo từng module trong `.claude/dev_plan`.
