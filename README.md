# Android Gas Meter OCR

ChatGPT API 를 사용해서 안드로이드 카메라로 가스검침기 이미지를 촬영한 뒤, 이미지 내의 숫자를 자동으로 인식하는 프로그램입니다.

전통적인 OCR 파이프라인처럼 이미지를 전처리해 인식 모델에 넣는 대신, **촬영한 사진을 그대로 Vision API 에 보내 값을 받는 방식**을 확인하기 위해 만들었습니다.

## 동작 방식

1. 안드로이드 앱에서 카메라로 가스검침기를 촬영
2. 촬영 이미지를 JPEG 로 압축하고 Base64 로 인코딩
3. ChatGPT Vision API 로 텍스트 지시와 이미지를 함께 전송
4. 검침기 표시 숫자를 추출해 앱 화면에 표시

## Demo

동영상: https://youtube.com/shorts/_0Bngwweimk?feature=share

테스트 원본 이미지:

<img width="390" height="484" alt="가스검침기_이미지" src="https://github.com/user-attachments/assets/0a473f15-8f32-4ec2-b4fe-788f6ae7166c" />

## 인식 대상을 프롬프트로 한정

계기판에는 읽어야 할 숫자와 읽지 말아야 할 숫자가 섞여 있습니다. 전처리로 영역을 잘라내는 대신, 프롬프트에서 대상을 한정했습니다.

```
이 이미지 내의 빨간색 박스내의 숫자는 제외하고
검정색 바탕에 하얀색 숫자를 인식해줘.
빨간색 박스를 제외한 숫자의 길이는 5자리야.
```

색상으로 대상을 구분하고 자릿수로 결과를 검증하는 조건을 함께 준 형태입니다. 전처리 코드를 작성하는 대신 지시문으로 같은 목적을 달성할 수 있는지 확인한 부분입니다.

## Tech

| 항목 | 내용 |
|---|---|
| 언어 | Kotlin |
| 최소 SDK | 26 (Android 8.0) |
| 네트워크 | OkHttp 4.12.0 |
| 비동기 | Kotlin Coroutines |
| UI | View Binding, ConstraintLayout |
| 모델 | OpenAI `gpt-4o-mini` (Chat Completions, image_url) |
| 권한 | CAMERA, INTERNET |

## 빌드 및 실행

1. 저장소를 클론합니다.

   ```bash
   git clone https://github.com/Deahyun/android-gas-meter-ocr.git
   ```

2. 프로젝트 루트의 `local.properties` 에 API 키를 추가합니다.

   ```properties
   OPENAI_API_KEY=sk-...
   ```

   `local.properties` 는 `.gitignore` 에 포함되어 있어 저장소에 올라가지 않습니다.

3. Android Studio 에서 열고 실기기에 설치합니다. 카메라를 사용하므로 에뮬레이터에서는 동작을 확인하기 어렵습니다.

키는 `BuildConfig.OPENAI_API_KEY` 로 주입되며, `local.properties` 에 키가 없으면 빈 문자열이 되어 요청이 실패합니다.

## 확인 범위

가스검침기를 여러 장 촬영해 동작을 확인했고, 위 Demo 에는 그중 한 장에 대한 결과를 첨부했습니다. **정량적인 인식률을 측정한 프로젝트가 아닙니다.** 촬영 각도, 조명, 반사에 따른 편차를 체계적으로 기록하지 않았으므로 수치로 성능을 주장하지 않습니다.

## 알려진 한계

- **외부 API 에 의존합니다.** 네트워크가 없거나 폐쇄망 환경에서는 동작하지 않습니다. 같은 목적을 온프레미스에서 처리하려면 로컬 Vision 모델이나 전통적인 OCR 파이프라인이 필요합니다.
- **같은 이미지에도 결과가 흔들릴 수 있습니다.** 생성 모델의 특성이며, 정확도가 중요한 값은 별도 검증 경로가 필요합니다.
- 프롬프트가 특정 계기판 형태(검정 바탕 흰 숫자 5자리, 빨간 박스 제외)를 전제로 작성되어 있습니다. 다른 계기판에는 지시문을 다시 써야 합니다.

## 관련 저장소

같은 OCR 문제를 반대 방식으로 접근한 프로젝트입니다.

- [fast-region-ocr](https://github.com/Deahyun/fast-region-ocr) — 화면 UI 에서 정해진 영역의 문자를 읽는 서버. 검출 단계를 생략하고 전처리 후 인식 모델에 직접 투입하는 전통적인 파이프라인. 온프레미스 동작, CPU 기준 한 줄 약 245ms.

두 저장소는 대상과 실행 환경에 따라 어떤 방식이 유리한지 비교하기 위해 만들었습니다.

## License

MIT
