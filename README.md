# Cloud Functions 2세대 - Kotlin 함수 배포 예제  

**Google Cloud Functions 2세대**에서 Kotlin 프로젝트의 기본 트리거 함수를 배포하는 방법을 담은 예제 소스 코드 입니다. 현재 가이드에서는 **감사 로그**를 사용하여 **Cloud Storage**에 파일이 업로드 되었을 때의 이벤트를 트리거 함수에 연동하는 방법을 설명합니다.  

---

## 기본 설정  
1. Google Cloud Console의 프로젝트 선택기 페이지에서 [Google Cloud 프로젝트를 선택하거나 만든다](https://cloud.google.com/resource-manager/docs/creating-managing-projects).


2. Cloud 프로젝트에 결제가 사용 설정되어 있는지 확인하고 안되어있다면 결제 계정을 설정한다.


3. [Cloud Functions, Cloud Build, Artifact Registry, Eventarc, Cloud Run, Logging, and Pub/Sub API를 사용 설정한다.](https://console.cloud.google.com/flows/enableapi?apiid=cloudbuild.googleapis.com,artifactregistry.googleapis.com,eventarc.googleapis.com,run.googleapis.com,logging.googleapis.com,pubsub.googleapis.com,cloudfunctions.googleapis.com&redirect=https://cloud.google.com/functions/quickstart&_ga=2.114454855.1179379734.1647931782-417356487.1645496611)


4. [Cloud SDK를 설치하고 초기화한다.](https://cloud.google.com/sdk/docs)


5. `gcloud` 구성요소를 업데이트하고 베타 명령어를 설치한다.  
```shell
gcloud components update  
gcloud components install beta    
```


6. Cloud Functions(2세대)에서 현재 사용할 수 있는 리전 중 하나를 사용하여 기본 리전을 설정한다.  
(아시아에서는 현재 도쿄만 사용 가능하다.)
```shell
gcloud config set functions/region asia-northeast1
```

## 트리거 이벤트 구성    

1. Cloud Console에서 [IAM 및 관리자 > 감사 로그](https://console.cloud.google.com/iam-admin/audit?_ga=2.81336023.1179379734.1647931782-417356487.1645496611) 페이지를 연다.  


2. Google Cloud Storage에서 Cloud 감사 로그 관리자 읽기, 데이터 읽기, 데이터 쓰기 로그 유형을 사용 설정한다.  
![](https://cloud.google.com/functions/img/audit-log-enable.png)


3. Cloud Console에서 [IAM 및 관리자 > 서비스 계정](https://console.cloud.google.com/iam-admin/serviceaccounts) 에서 **Default compute service account**를 확인하고 서비스 계정에 `eventarc.eventReceiver` 역할을 부여한다.  
```shell
gcloud projects add-iam-policy-binding $PROJECT_ID 
  --member serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com 
  --role roles/eventarc.eventReceiver
```

## 배포 방법

- **함수 배포 (JAR 방식)**  
  반드시 배포 전에 gradle shadowJar를 실행해서 JAR 파일을 빌드해야 한다.
    ```shell
    gcloud beta functions deploy test-function-1 --gen2 --runtime java11 --trigger-event-filters="type=google.cloud.audit.log.v1.written" --trigger-event-filters="serviceName=
    storage.googleapis.com" --trigger-event-filters="methodName=storage.objects.create" --entry-point functions.LogCloudEvent --source=build/libs
    ```

- ~~**함수 배포 (소스 방식)**~~   
  ~~Cloud Build를 통해서 소스 코드가 빌드되기 때문에 로컬 빌드보다 배포가 느리다.  
  (또한 pom.xml을 반드시 포함해야 한다. 따라서 현재 소스로는 배포 실패함)~~
    ```shell
    gcloud beta functions deploy test-function-1 --gen2 --runtime java11 --trigger-event-filters="type=google.cloud.audit.log.v1.written" --trigger-event-filters="serviceName=
    storage.googleapis.com" --trigger-event-filters="methodName=storage.objects.create" --entry-point functions.LogCloudEvent --source .
    ```

- **버킷 생성**  
    ```shell
    gsutil mb -l asia-northeast1 gs://functions-tokyo-test-bucket  
    ```

- **파일 생성**  
    ```shell
    echo "Hello World" > random.txt  
    ```

- **버킷에 파일 업로드**    
    ```shell
    gsutil cp random.txt gs://functions-tokyo-test-bucket/random.txt  
    ```

- **로그 보기**  
    ```shell
    gcloud beta functions logs read test-function-1 --gen2 --limit=100
    ```  
  
- **트리거 호출 성공 로그 예제**  
  아래와 같이 로그가 출력된다면 성공적으로 트리거 함수가 호출된 것이다.  
  (배포 직후 약 1~2분 사이에 호출된 이벤트는 무시될 수도 있다.)
  ```shell
  LEVEL  NAME             TIME_UTC                 LOG
  I      test-function-1  2022-03-23 05:30:25.571
  I      test-function-1  2022-03-23 05:30:25.558  Authenticated User: wonsuc@gmail.com
  I      test-function-1  2022-03-23 05:30:25.557  Resource name: projects/_/buckets/functions-tokyo-test-bucket/objects/random.txt
  I      test-function-1  2022-03-23 05:30:25.557  API Method: storage.objects.create
  I      test-function-1  2022-03-23 05:30:25.250  Event Subject: storage.googleapis.com/projects/_/buckets/functions-tokyo-test-bucket/objects/random.txt
  I      test-function-1  2022-03-23 05:30:25.155  Event Type: google.cloud.audit.log.v1.written
         test-function-1  2022-03-23 05:30:21.556  2022-03-23 05:30:21.556:INFO:oejs.Server:main: Started @5543ms
         test-function-1  2022-03-23 05:30:21.555  2022-03-23 05:30:21.555:INFO:oejs.AbstractConnector:main: Started ServerConnector@6a024a67{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
  D      test-function-1  2022-03-23 05:30:21.450  Container Sandbox: Unsupported syscall setsockopt(0xa,0x1,0xc,0x3e9888ff2f8c,0x4,0x9). It is very likely that you can safely ignore this message and that this is not the cause of any
  error you might be troubleshooting. Please, refer to https://gvisor.dev/docs/user_guide/compatibility/linux/amd64/#setsockopt for more information.
         test-function-1  2022-03-23 05:30:21.355  2022-03-23 05:30:21.355:INFO:oejsh.ContextHandler:main: Started o.e.j.s.ServletContextHandler@27808f31{/,null,AVAILABLE}
         test-function-1  2022-03-23 05:30:20.856  2022-03-23 05:30:20.855:INFO:oejs.Server:main: jetty-9.4.26.v20200117; built: 2020-01-17T12:35:33.676Z; git: 7b38981d25d14afb4a12ff1f2596756144edf695; jvm 11.0.13+8-Ubuntu-0ubuntu1.18
  .04
         test-function-1  2022-03-23 05:30:19.456  2022-03-23 05:30:19.455:INFO::main: Logging initialized @3348ms to org.eclipse.jetty.util.log.StdErrLog
  ```
