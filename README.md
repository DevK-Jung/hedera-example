# Hedera SDK와 Spring Boot를 활용한 블록체인 예제

---

>  Hedera SDK를 사용하여 Spring Boot 기반으로 블록체인 관련 기능을 구현한 예제 프로젝트입니다. 

## 기술 스펙

---

- **Framework**: Spring Boot 3.4.1

- **Build Tool**: Gradle

- **모듈 구성**: 멀티모듈 (common, consensus)

- **Dependencies**:

  ```gradle
  implementation 'com.hedera.hashgraph:sdk:2.46.0'
  implementation 'io.grpc:grpc-netty-shaded:1.64.0'
  ```



## 디렉토리 구조

---

```
hedera-example
├── common
├── consensus

```

### 모듈 설명

#### **common** 모듈

- **`ClientConfig`**: Hedera `Client` 객체를 생성하고, Bean으로 등록하여 싱글톤으로 관리합니다.
  - testnet으로 설정되어있습니다. mainnet으로 변경을 원하면 `Client client = Client.forMainnet();`으로 변경해주세요
- **`AbstractHederaHelper`**: 공통 로직 및 응답 객체 생성을 담당하는 추상 클래스입니다.
- **`HederaTransactionResponseVo`**: 트랜잭션 응답 정보를 관리하는 공통 VO 클래스입니다.
- **`HederaResponseUtils`**: 응답 생성을 돕는 유틸리티 클래스입니다.

#### **consensus** 모듈

- **`ConsensusHelperV1.java`**
  - Topic 관련 CRUD 및 메시지 전송 기능 구현:
    - **Topic 생성**: 새로운 Topic을 생성합니다.
    - **Topic 수정**: 기존 Topic의 속성을 업데이트합니다.
    - **Topic 삭제**: Topic을 삭제합니다.
    - **메시지 전송**: 지정된 Topic에 메시지를 제출합니다.
    - **Topic 정보 조회**: Topic의 세부 정보를 조회합니다.

## 실행 방법

---

### 1. 환경설정

`consensus` 모듈의 `application.yml`에서 Hedera 계정 정보를 설정합니다. 설정값은 Hedera 네트워크와의 통신을 위해 필수적입니다.

```yaml
hedera:
  account-id: ${accountId}    # Hedera Account ID
  private-key: ${privateKey}  # Hedera Private Key
```

### 2. 프로젝트 빌드 및 실행

아래 명령어를 통해 `consensus` 모듈을 실행할 수 있습니다.

```bash
./gradlew :consensus:bootRun
```



## 참고 자료

---

- [Hedera 공식 문서](https://docs.hedera.com/hedera/sdks-and-apis/sdks)

## 블로그 정리

---

- [개인블로그](https://devk-jung.github.io/posts/blockchain-hedera-1/)


## 특이사항

---

- 이 프로젝트는 Hedera Testnet 환경에서 작동합니다. 실제 Mainnet 환경에서 사용하려면 ClientConfig.java 설정과 Hedera 계정 및 설정을 변경해야 합니다.
