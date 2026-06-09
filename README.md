# Learning Vocabulary Platform — Backend

API server cho nền tảng học từ vựng tiếng Anh: quản lý bài học, ôn tập Spaced Repetition, quiz, contest, lớp học và thống kê học tập.

> **Frontend (React):** [learningVocabularyFrontend](https://github.com/CuongTu1806/learningVocabularyFrontend)  
> Dự án full-stack: repo này là **backend**; giao diện người dùng nằm ở repo FE ở trên.

---

## Tính năng chính

| Module | Mô tả |
|--------|--------|
| **Auth** | Đăng ký, đăng nhập, JWT + refresh token, đổi mật khẩu, profile |
| **Bài học** | CRUD lesson, thêm/sửa/xóa từ trong bài (user vocabulary) |
| **Từ điển** | Tìm từ theo `word` hoặc `meaning` (bảng `vocabulary` + từ người dùng) |
| **Quiz** | Tạo quiz theo bài, nộp bài, lịch sử và chi tiết kết quả |
| **Spaced Repetition** | Lịch ôn, flashcard, cài đặt bước học, lịch calendar |
| **Profile stats** | Biểu đồ reviews, thời gian học, phân loại thẻ, ease, khoảng ôn, từ mới |
| **Leaderboard** | Bảng xếp hạng global theo tổng điểm contest |
| **Lớp học** | Classroom, thành viên, mời tham gia |
| **Bài tập / Contest** | Assignment, contest và nộp bài |
| **Media** | Phục vụ audio/ảnh từ thư mục `mediaFull` qua `/mediaFull/**` |

---

## Tech stack

- **Java 21**
- **Spring Boot 4** (Web, Security, Data JPA, Validation)
- **MySQL**
- **JWT** (jjwt)
- **Lombok**, **Maven**

---

## Yêu cầu

- JDK **21+**
- **Maven** 3.9+
- **MySQL** 8.x
- (Tuỳ chọn) Thư mục `mediaFull` chứa file audio/ảnh từ vựng

---

## Cài đặt & chạy

### 1. Clone repository

```bash
git clone https://github.com/CuongTu1806/learningVocabularyPlatform.git
cd learningVocabularyPlatform
```

### 2. Tạo database

```sql
CREATE DATABASE vocabulary_learning_platform
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Hibernate `ddl-auto=update` sẽ tự tạo/cập nhật bảng khi chạy lần đầu.

### 3. Cấu hình

Tạo biến môi trường (khuyến nghị — **không commit mật khẩu thật**):

| Biến | Mô tả | Mặc định |
|------|--------|----------|
| `MYSQL_USER` | User MySQL | `root` |
| `MYSQL_PASSWORD` | Mật khẩu MySQL | (xem `application.properties`) |

Tuỳ chọn trong `src/main/resources/application.properties` hoặc profile riêng:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vocabulary_learning_platform
app.media.root=./mediaFull
```

`app.media.root`: đường dẫn tới folder chứa media (audio/ảnh). File trong DB thường tham chiếu dạng `/mediaFull/...`.

### 4. Chạy ứng dụng

```bash
mvn spring-boot:run
```

API mặc định: **http://localhost:8080**

### 5. Chạy kèm Frontend

```bash
# Terminal 1 — backend (repo này)
mvn spring-boot:run

# Terminal 2 — frontend
git clone https://github.com/CuongTu1806/learningVocabularyFrontend.git
cd learningVocabularyFrontend
npm install
npm run dev
```

Mở **http://localhost:5173**. FE proxy `/api` → `http://localhost:8080` (cấu hình trong `vite.config.js`).

---

## API (prefix `/api`)

| Nhóm | Base path |
|------|-----------|
| Auth | `/api/auth` |
| Lessons | `/api/lessons` |
| Vocabulary search | `/api/vocabulary` |
| Quiz | `/api/quiz` |
| Spaced repetition | `/api/spaced_repetition` |
| Profile stats | `/api/profile` |
| Leaderboard | `/api/leaderboard` |
| Classes | `/api/classes` |
| Assignments | `/api/assignments` |
| Contests | `/api/contests` |

Ví dụ:

- `POST /api/auth/login` — đăng nhập  
- `GET /api/vocabulary/search?query=hello` — gợi ý từ vựng  
- `GET /api/leaderboard/global` — bảng xếp hạng  
- `GET /api/profile/stats?period=month` — thống kê profile  

Phần lớn endpoint yêu cầu header `Authorization: Bearer <token>`.

---

## Dữ liệu mẫu (leaderboard)

```bash
mysql -u root -p vocabulary_learning_platform < src/main/resources/db/seed_demo_leaderboard.sql
```

Tạo user demo `seed_player_01` … `seed_player_08` (mật khẩu: `demo123`) và dữ liệu contest submission cho bảng xếp hạng.

---

## Cấu trúc thư mục (rút gọn)

```
src/main/java/.../
  controller/     # REST API
  service/        # Business logic
  repository/     # JPA
  entity/         # Database models
  dto/            # Request / Response
  config/         # Security, CORS, JWT, static media
src/main/resources/
  application.properties
  db/seed_demo_leaderboard.sql
```

---

## Ghi chú bảo mật

- Đổi `app.jwt.secret` trước khi deploy production.
- Dùng `MYSQL_PASSWORD` qua biến môi trường, không đẩy credential lên GitHub.
- CORS hiện cho phép `localhost:5173`, `5170`, `3000`.

---

## Liên kết

| Thành phần | Repository |
|------------|------------|
| **Backend (repo này)** | https://github.com/CuongTu1806/learningVocabularyPlatform |
| **Frontend** | https://github.com/CuongTu1806/learningVocabularyFrontend |

---

## License

Dự án học tập / đồ án — tuỳ chỉnh license khi public repo.
