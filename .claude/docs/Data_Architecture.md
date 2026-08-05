# Data Architecture Design

## 1. Overview
Hệ thống sử dụng mô hình quan hệ (Relational Database) để quản lý người dùng, kho từ vựng, cấu trúc thư mục phân cấp và các phiên kiểm tra/học tập. 

**Chiến lược thiết kế chính:**
* **Inheritance (Kế thừa):** Sử dụng mô hình *Class Table Inheritance* để quản lý `Item` (gốc), từ đó dẫn xuất ra `Folder` và `VocabSet`.
* **Hierarchy (Phân cấp):** Sử dụng trường `parent_id` để thiết lập cấu trúc cây cho `Folder` và `Item`.
* **Many-to-Many (N-N):** Sử dụng bảng trung gian để quản lý mối quan hệ giữa `Vocab` và `VocabSet`.

---

## 2. Entity Relationship Diagram (ERD) - Logical Schema

### 2.1. User Module
#### Table: `users`
Lưu trữ thông tin định danh người dùng.
| Column          | Type   | Constraints        | Description                      |
| :-------------- | :----- | :----------------- | :------------------------------- |
| `user_id`       | Long   | PK, Auto-increment | Định danh duy nhất               |
| `email`         | String | Unique, Not Null   | Email đăng nhập                  |
| `hash_password` | String | Not Null           | Mật khẩu đã băm                  |
| `refresh_token` | String | Nullable           | Token để làm mới phiên đăng nhập |
| `created_at`    | Instant | Not Null          | Thời điểm tạo dữ liệu            |
| `updated_at`    | Instant | Nullable          | Thời điểm cập nhật dữ liệu       |
| `created_by`    | String | Nullable           | Tác nhân tạo dữ liệu             |
| `updated_by`    | String | Nullable           | Tác nhân cập nhật dữ liệu        |

### 2.2. Organization Module (Inheritance & Tree)
Sử dụng mô hình kế thừa để quản lý cấu trúc cây.

#### Table: `items` (Base Class)
Bảng gốc chứa các thuộc tính chung của cả Folder và VocabSet.
| Column      | Type | Constraints                  | Description                          |
| :---------- | :--- | :--------------------------- | :----------------------------------- |
| `item_id`   | Long | PK, Auto-increment           | Định danh duy nhất                   |
| `type`      | Enum | Not Null (FOLDER, VOCAB_SET) | Loại item                            |
| `user_id`   | Long | FK (users.user_id)           | Chủ sở hữu                           |
| `parent_id` | Long | FK (items.item_id), Nullable | ID của Folder cha (tạo cấu trúc cây) |

#### Table: `folders` (Derived Class)
| Column        | Type   | Constraints            | Description              |
| :------------ | :----- | :--------------------- | :----------------------- |
| `folder_id`   | Long   | PK, FK (items.item_id) | ID kế thừa từ bảng items |
| `folder_name` | String | Not Null               | Tên thư mục              |

#### Table: `vocab_sets` (Derived Class)
| Column            | Type   | Constraints            | Description              |
| :---------------- | :----- | :--------------------- | :----------------------- |
| `vocab_set_id`    | Long   | PK, FK (items.item_id) | ID kế thừa từ bảng items |
| `vocab_set_name`  | String | Not Null               | Tên tập từ vựng          |
| `vocab_set_descp` | String | Nullable               | Mô tả tập từ             |

### 2.3. Vocabulary Module
#### Table: `vocabs`
| Column      | Type   | Constraints        | Description        |
| :---------- | :----- | :----------------- | :----------------- |
| `vocab_id`  | Long   | PK, Auto-increment | Định danh duy nhất |
| `word`      | String | Not Null, Unique   | Từ vựng            |
| `meaning`   | String | Nullable           | Nghĩa của từ       |
| `ipa`       | String | Nullable           | Phiên âm quốc tế   |
| `audio_url` | String | Nullable           | Link file âm thanh |

#### Table: `vocab_vocab_set` (Junction Table)
Quản lý quan hệ N-N giữa `Vocab` và `VocabSet`.
| Column         | Type | Constraints                      | Description |
| :------------- | :--- | :------------------------------- | :---------- |
| `vocab_id`     | Long | PK, FK (vocabs.vocab_id)         |             |
| `vocab_set_id` | Long | PK, FK (vocab_sets.vocab_set_id) |             |

### 2.4. Testing Module
#### Table: `tests`
Lưu trữ thông tin phiên kiểm tra (Multiple Choice).
| Column                   | Type | Constraints        | Description                  |
| :----------------------- | :--- | :----------------- | :--------------------------- |
| `test_id`                | Long | PK, Auto-increment |                              |
| `user_id`                | Long | FK (users.user_id) |                              |
| `number_of_question`     | Int  | Min = 1            |                              |
| `time_in_minute`         | Int  | Nullable           | 0 hoặc null = không giới hạn |
| `correct_answer_count`   | Int  | Default 0          |                              |
| `incorrect_answer_count` | Int  | Default 0          |                              |

#### Table: `test_items` (Junction Table)
Lưu trữ nguồn `VocabSource` của bài test (có thể là nhiều Folder/VocabSet).
| Column    | Type | Constraints            | Description |
| :-------- | :--- | :--------------------- | :---------- |
| `test_id` | Long | PK, FK (tests.test_id) |             |
| `item_id` | Long | PK, FK (items.item_id) |             |

#### Table: `questions`
| Column             | Type   | Constraints          | Description                    |
| :----------------- | :----- | :------------------- | :----------------------------- |
| `question_id`      | Long   | PK, Auto-increment   |                                |
| `test_id`          | Long   | FK (tests.test_id)   |                                |
| `vocab_id`         | Long   | FK (vocabs.vocab_id) | Từ vựng gốc để sinh câu hỏi    |
| `question_content` | String | Not Null             | Nội dung câu hỏi theo template |
| `correct_answer`   | String | Not Null             | Text của đáp án đúng           |
| `audio_url`        | String | Nullable             |                                |

#### Table: `options`
| Column           | Type    | Constraints                | Description          |
| :--------------- | :------ | :------------------------- | :------------------- |
| `option_id`      | Long    | PK, Auto-increment         |                      |
| `question_id`    | Long    | FK (questions.question_id) |                      |
| `option_order`   | Int     | Min=1, Max=4               | Thứ tự 1, 2, 3, 4    |
| `option_content` | String  | Not Null                   | Nội dung đáp án      |
| `is_correct`     | Boolean | Not Null                   | Đánh dấu đáp án đúng |
| `audio_url`      | String  | Nullable                   |                      |

#### Table: `test_answers`
Lưu đáp án cuối cùng của người dùng cho từng câu hỏi khi kết thúc bài test.
| Column               | Type    | Constraints                    | Description                         |
| :------------------- | :------ | :----------------------------- | :---------------------------------- |
| `test_answer_id`     | Long    | PK, Auto-increment             |                                     |
| `test_id`            | Long    | FK (tests.test_id)             | Bài test chứa câu trả lời           |
| `question_id`        | Long    | FK (questions.question_id)     | Câu hỏi được trả lời                |
| `selected_option_id` | Long    | FK (options.option_id), Nullable | Option cuối cùng người dùng chọn |
| `is_correct`         | Boolean | Not Null                       | Kết quả đúng/sai của câu trả lời    |

---

## 3. Implementation Notes (Ghi chú triển khai)

### 3.1. Querying the Tree (Truy vấn cây)
Để lấy toàn bộ `vocab_id` từ một `item_id` (có thể là Folder), Backend cần thực hiện:
1. Sử dụng **Recursive Common Table Expression (CTE)** trong SQL để tìm tất cả `item_id` con.
2. Lọc các `item_id` có `type = 'VOCAB_SET'`.
3. Join với bảng `vocab_vocab_set` để lấy danh sách `vocab_id`.

### 3.2. Data Integrity Constraints
* **Database Level:**
    * `ON DELETE CASCADE` cho các quan hệ `test_items`, `questions`, `options` khi một `test` bị xóa.
    * `UNIQUE` constraint trên `(question_id, option_order)`.
    * `UNIQUE` constraint trên `(test_id, question_id)` cho `test_answers`.
    * `UNIQUE` constraint trên `(question_id)` cho cột `is_correct` (nếu DB hỗ trợ partial index) hoặc đảm bảo bằng Logic Code.
* **Application Level:**
    * Đảm bảo tên item unique trong cùng parent của cùng user; rule này áp dụng chung giữa `folders.folder_name` và `vocab_sets.vocab_set_name` vì tên đang nằm ở hai bảng dẫn xuất khác nhau.
    * Đảm bảo khi tạo `Option`, luôn có đúng 1 option có `is_correct = true`.
    * Đảm bảo `option_content` của đáp án đúng khớp với `correct_answer` của câu hỏi.
    * Đảm bảo `selected_option_id` của `test_answers` thuộc đúng `question_id`.

### 3.3. Performance Optimization
* Đánh Index cho các cột: `user_id`, `parent_id`, `vocab_id`, `test_id`, `email`.
* Sử dụng Eager Loading cho các quan hệ quan trọng để tránh lỗi N+1 khi lấy thông tin VocabSet kèm theo Vocab.
