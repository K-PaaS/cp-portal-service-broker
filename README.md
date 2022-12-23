# container-platform-portal-service-broker

PaaS-TA에서 제공하는 Container Platform 포털 서비스 브로커로 클라우드 컨트롤러와 서비스 브로커간의 v2 서비스 API 를 제공합니다.

- [시작하기](#시작하기)
  - [Container Platform 포털 서비스 브로커 설치 방법](#Container-Platform-포털-서비스-브로커-설치-방법)
  - [Container Platform 포털 서비스 브로커 빌드 방법](#Container-Platform-포털-서비스-브로커-빌드-방법)
- [개발 환경](#개발-환경)
- [가능한 명령 목록 (로컬 환경)](#가능한-명령-목록-(로컬-환경))
- [라이선스](#라이선스)

## 시작하기

Container Platform 포털 서비스 브로커가 수행하는 서비스 관리 작업은 다음과 같습니다.
- Catalog : Container Platform 포털 서비스 카탈로그 조회
- Provisioning : Container Platform 포털 서비스 인스턴스 생성 ( parameters "owner", "org_name", "space_name" 필수 )
- Updateprovisioning : Container Platform 포털 서비스 인스턴스 갱신
- Deprovisioning : Container Platform 포털 서비스 인스턴스 삭제

### Container Platform 포털 서비스 브로커 설치 방법

[서비스팩 설치 가이드](https://github.com/PaaS-TA/Guide-5.0-Ravioli/blob/master/service-guide/tools/PAAS-TA_CONTAINER_SERVICE_INSTALL_GUIDE_V2.0.md)의 가이드를 참고하시면 아키텍쳐와 설치 및 사용법에 대해 자세히 알 수 있습니다.

### Container Platform 포털 서비스 브로커 빌드 방법

Container Platform 포털 서비스 브로커를 활용하여 로컬 환경에서 빌드하고 싶을 때 다음 명령어를 입력합니다.
```
$ gradle build
```

## 개발 환경

Container Platform 포털 서비스 브로커의 개발 환경은 다음과 같습니다.

| Situation                      | Version |
| ------------------------------ | ------- |
| JDK                            | 8       |
| Gradle                         | 6.9.2   |
| Spring Boot                    | 2.7.3   |
| JSch                           | 0.1.54  |
| Hibernate Core                 | 5.6.10   |
| Json Path                      | 2.2.0   |
| Jacoco                         | 0.8.1   |
| MariaDB Java Client            | 2.7.5   |


## 가능한 명령 목록 (로컬 환경)

- Catalog 조회 : http://localhost:8888/v2/catalog
  - Method : GET 
  - Header
    > Authorization : Bearer type \
      X-Broker-Api-Version : 2.4 \
      Content-Type : application/json
  - Body : None 
  - Parameters : None

- 서비스 인스턴스 생성 : http://localhost:8888/v2/service_instances/[new-instance-name]
  - Method : PUT 
  - Header
    > Authorization : Bearer type \
      X-Broker-Api-Version : 2.4 \
      Content-Type : application/json \
      Accept: application/json
  - Body
    > { \
        "service_id": \<service-id-string\>, \
        "plan_id": \<plan-id-string\>, \
        "organization_guid": \<organization-guid-string\>, \
        "space_guid": \<space-guid-string\>, \
        "parameters": { "userName": \<user-name-in-caas-service\> } \
      }
  - Parameters : None

- 서비스 인스턴스 갱신 : http://localhost:8888/v2/service_instances/[instance-name]
  - Method : PATCH 
  - Header
    > Authorization : Bearer type \
      X-Broker-Api-Version : 2.4 \
      Content-Type : application/json 
  - Body
    > { \
        "plan_id": \<plan-id-string\>, \
        "service_id": \<service-id-string\> \
      } 
  - Parameters : None

- 서비스 인스턴스 삭제 : http://localhost:8888/v2/service_instances/[instance-name]
  - Method : DELETE 
  - Header
    > Authorization : Bearer type \
      X-Broker-Api-Version : 2.4 \
      Content-Type : application/json 
  - Body : None
  - Parameters : 
    > service_id : \<predefined_service_id\> \
      plan_id : \<plan_id_of_service\>

## 라이선스
Container Platform 포털 서비스 브로커는 [Apache-2.0 License](http://www.apache.org/licenses/LICENSE-2.0)를 사용합니다.