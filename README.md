# Peak-Time


## 모니터링 스택 구성 (Prometheus + Grafana)

운영 환경에서 Spring Boot Actuator 메트릭을 기반으로 Prometheus/Grafana 모니터링 스택을 구성했습니다.

### 구성 요소
- Prometheus: Spring Boot `/actuator/prometheus` 메트릭을 주기적으로 수집(scrape)
- Grafana: Prometheus 데이터를 시각화(대시보드) 및 알림(Alert) 설정

### 네트워크 구성
모니터링 컨테이너는 애플리케이션과 동일한 Docker 네트워크(`peak-time_default`)에 연결되어,
Docker DNS를 통해 `peaktime-app:8080`으로 접근합니다.

### 실행 방법 (EC2)
> 모니터링 스택은 애플리케이션 배포(CI/CD)와 분리하여 운영 서버에서 별도로 실행합니다.

```bash
cd monitoring
docker compose -f docker-compose.monitoring.yml up -d
