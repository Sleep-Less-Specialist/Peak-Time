# Nginx + HTTPS 적용 (Let’s Encrypt / Certbot)

## 목표
- 사용자 접근 경로를 **HTTP(80) → HTTPS(443)** 로 전환한다.
- EC2 앞단에 **Nginx Reverse Proxy** 를 두고 Spring Boot(8080)으로 라우팅한다.
- 최종 접속 URL을 `https://peaktime-edu.xyz` 로 통일한다.

---

## 시스템 구성
- EC2: Amazon Linux 2023
- Nginx: 80 / 443 리스닝
- Spring Boot: 8080 (내부 애플리케이션)
- DNS: `peaktime-edu.xyz` → EC2 Public IP
- 인증서: Let’s Encrypt (Certbot)

트래픽 흐름:
`Client → Nginx(80/443) → Spring(8080)`

---

## 작업 전 체크
- 보안그룹 인바운드: **80(HTTP), 443(HTTPS)** 허용
- 도메인 DNS가 EC2 Public IP를 정상적으로 가리키는지 확인

---

## 1) Certbot 설치
Amazon Linux 2023(dnf) 기준으로 Certbot과 Nginx 플러그인을 설치한다.

```bash
sudo dnf install -y certbot python3-certbot-nginx
```

> `python3-certbot-nginx`는 Certbot이 **Nginx 설정을 읽고(서버블록 탐색), 인증서 발급 후 SSL 설정을 자동 반영**할 수 있게 해주는 플러그인이다.

---

## 2) 인증서 발급 및 Nginx 자동 적용
Nginx 플러그인을 사용해 도메인에 대한 인증서를 발급하고, SSL 설정을 Nginx 설정에 자동 반영한다.

```bash
sudo certbot --nginx -d peaktime-edu.xyz
```

진행 중 입력 항목
- 이메일 입력(갱신 실패/보안 공지 수신용)
- 약관 동의: `Y`
- EFF 이메일 공유 여부: 필요 시 `N`

발급 결과(경로)
- 인증서: `/etc/letsencrypt/live/peaktime-edu.xyz/fullchain.pem`
- 개인키: `/etc/letsencrypt/live/peaktime-edu.xyz/privkey.pem`

---

## 3) Reverse Proxy 설정 개요 (80/443 → 8080)
외부 요청은 Nginx가 받되, 실제 애플리케이션 응답은 Spring Boot(8080)에서 처리하도록 **Reverse Proxy** 구조로 구성한다.

핵심 포인트
- `proxy_pass http://127.0.0.1:8080;`
- 클라이언트 정보 전달을 위한 헤더 세팅
    - `Host`, `X-Real-IP`, `X-Forwarded-For`, `X-Forwarded-Proto`

예시(템플릿)

```nginx
server {
    listen 443 ssl;
    server_name peaktime-edu.xyz;

    ssl_certificate     /etc/letsencrypt/live/peaktime-edu.xyz/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/peaktime-edu.xyz/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }
}
```

> 실제 서버에서는 `/etc/nginx/conf.d/peaktime-edu.xyz.conf`에 적용되어 있으며, 문서에는 이해를 돕기 위해 템플릿 형태로만 포함한다.

---

## 4) HTTP → HTTPS 리다이렉트 확인
HTTP로 들어온 요청이 HTTPS로 강제 이동되는지 확인한다.

```bash
curl -I http://peaktime-edu.xyz
```

기대 결과
- `301 Moved Permanently` 또는 `308 Permanent Redirect`
- `Location: https://peaktime-edu.xyz/...`

---

## 5) HTTPS 응답 확인
HTTPS로 요청했을 때 정상적으로 응답이 내려오는지 확인한다.

```bash
curl -I https://peaktime-edu.xyz
```

기대 결과
- `200 OK` (정적 페이지) 또는
- 인증이 필요한 API라면 `401`도 정상(백엔드 도달 확인)

---

## 6) 443 리스닝 확인
Nginx가 443 포트를 정상적으로 LISTEN하고 있는지 확인한다.

```bash
sudo ss -tulpn | grep :443
```

---

## 7) 설정 반영/점검 (필요 시)
문법 검사

```bash
sudo nginx -t
```

무중단 반영

```bash
sudo systemctl reload nginx
```

---

## 8) 인증서 자동 갱신
Let’s Encrypt 인증서는 90일 단위이며, 운영에서는 자동 갱신을 사용한다.

갱신 동작 테스트

```bash
sudo certbot renew --dry-run
```

Amazon Linux 2023에서는 **자동 갱신 타이머가 기본으로 시작되지 않을 수 있음**
(설치 로그에 `Certbot auto renewal timer is not started by default.`가 출력됨).

필요 시 타이머 활성화

```bash
sudo systemctl enable --now certbot-renew.timer
systemctl list-timers | grep certbot
```

---

## 9) 작업 결과(실제 검증 로그)

### 9-1. 인증서 발급 성공
- 인증서 만료일: `2026-05-11`
- Nginx 설정 반영: `/etc/nginx/conf.d/peaktime-edu.xyz.conf`

### 9-2. HTTPS 응답
```bash
curl -I https://peaktime-edu.xyz
```

```text
HTTP/1.1 200
Server: nginx/1.28.1
```

### 9-3. HTTP → HTTPS 리다이렉트
```bash
curl -I http://peaktime-edu.xyz
```

```text
HTTP/1.1 301 Moved Permanently
Location: https://peaktime-edu.xyz/
```

### 9-4. 443 LISTEN 확인
```bash
sudo ss -tulpn | grep :443
```

```text
tcp LISTEN ... 0.0.0.0:443 ... users:(('nginx',pid=...))
```

---

## 10) 보안 고려 사항(권장)
- 외부 노출 포트는 최종적으로 **80/443만** 유지한다.
- Spring Boot(8080)는 Nginx 내부 통신 용도로만 사용한다.
- HTTPS 적용이 완료되면 **보안그룹에서 8080 인바운드 제거**를 권장한다.

> 목표: 외부에서 애플리케이션 포트로 직접 접근하는 경로를 차단해 공격 표면을 줄인다.