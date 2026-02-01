# climbing-nav backend

클라이밍 관련 서비스의 백엔드 API 서버입니다.
(예: 암장/루트/리뷰/즐겨찾기/검색 등 도메인 데이터를 제공)

> Tech: Java, (Spring Boot), Gradle, Docker, GitHub Actions

---

## ✅ 주요 기능
- 회원/인증: 로그인, JWT 인증
- 암장/루트: 목록/상세/검색
- 리뷰/평점: 작성/조회

---

## 🧱 아키텍처 / 구성
- `src/` : 애플리케이션 소스
- `.github/workflows/` : CI/CD (테스트/빌드/배포 자동화)
- `Dockerfile` : 컨테이너 빌드
- `build.gradle` : 의존성/빌드 설정

---

## ⚙️ 실행 환경
- Java: 17
- Gradle: wrapper 사용
- DB: MySQL
- OS: Linux

---

## 🔐 환경변수

| key | 설명 | 예시 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | 실행 프로파일 | `dev` |
| `DB_URL` | DB URL | `jdbc:mysql://localhost:3306/climbing` |
| `DB_USERNAME` | DB 계정 | `root` |
| `DB_PASSWORD` | DB 비밀번호 | `password` |

---

## 🚀 로컬 실행 (Gradle)
```bash
./gradlew clean test
./gradlew bootRun
