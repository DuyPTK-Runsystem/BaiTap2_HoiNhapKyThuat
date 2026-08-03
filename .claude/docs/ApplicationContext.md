# Application Context: English Learning Support System

## 1. Project Overview
Phần mềm hỗ trợ học tiếng Anh với các tính năng cốt lõi: quản lý kho từ vựng theo cấu trúc cây (Folder/VocabSet), hỗ trợ học qua Flashcard, làm bài kiểm tra trắc nghiệm (Multiple Choice) với khả năng tracking tiến độ, và hỗ trợ AI/Automated IPA/Audio.

## 2. Core Business Rules (High-Level)
- **User Model:** Hệ thống chỉ có một tác nhân duy nhất là `Logged-in User`.
- **Vocabulary Hierarchy:** 
    - `Folder` và `VocabSet` là các lớp dẫn xuất (sub-classes) của `Item`.
    - `Folder` có thể chứa `Folder` hoặc `VocabSet` (Cấu trúc cây).
    - Một `VocabSet` chỉ nằm trong một `Folder` duy nhất.
    - Quan hệ `Vocab` <-> `VocabSet` là **n-n**.
- **Import Logic:** 
    - Hỗ trợ thủ công và qua file `.xlsx`.
    - Quy tắc `Partial Failure`: Nếu một từ lỗi, chỉ bỏ qua từ đó, không hủy toàn bộ tiến trình.
    - Tự động hóa: Hệ thống tự tìm IPA và Audio (nếu có thể).
- **Testing Logic:**
    - **Multiple Choice:** Câu hỏi được generate tự động (không dùng ngân hàng câu hỏi có sẵn). Có giới hạn thời gian (nếu `TimeInMinute > 0`).
    - **Flashcard:** Không lưu trữ phiên học vào Database.
    - **Source Selection:** Có thể lấy từ một `Item` cụ thể (phải quét đệ quy các con) hoặc lấy ngẫu nhiên toàn bộ CSDL.

## 3. Technical Architecture Principles
- **Entity Inheritance:** Sử dụng mô hình kế thừa để quản lý `Item` (Folder/VocabSet).
- **Data Integrity:** 
    - `Option` phải Unique theo cặp `(question_id, option_order)`.
    - Mỗi câu hỏi chỉ có duy nhất một `isCorrect = true`.
- **API Design:** Ưu tiên tính nhất quán giữa các biểu mẫu (BM) và các User Stories.

## 4. Module Map
Hệ thống được chia thành các module sau:
1.  [Auth Module](./modules/Auth_Module.md) - Quản lý người dùng và phiên làm việc.
2.  [Vocabulary Management Module](./modules/Vocabulary_Module.md) - Import, quản lý từ vựng và IPA.
3.  [Organization Module](./modules/Organization_Module.md) - Quản lý Folder và VocabSet (Cấu trúc cây).
4.  [Testing & Learning Module](./modules/Testing_Module.md) - Tạo bài test, Flashcard và Tracking.