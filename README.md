# Android Gas Meter OCR

ChatGPT API 를 사용해서 안드로이드 카메라로 가스검침기 이미지를 촬영한 뒤, 이미지 내의 숫자를 자동으로 인식하는 프로그램입니다.

## 동작 방식

1. 안드로이드 앱에서 카메라로 가스검침기를 촬영
2. 촬영 이미지를 ChatGPT Vision API 로 전송
3. 검침기 표시 숫자를 추출해 앱 화면에 표시

## Demo

동영상: https://youtube.com/shorts/_0Bngwweimk?feature=share

테스트 원본 이미지:

<img width="390" height="484" alt="가스검침기_이미지" src="https://github.com/user-attachments/assets/0a473f15-8f32-4ec2-b4fe-788f6ae7166c" />

## Tech

- Android (Java/Kotlin)
- OpenAI ChatGPT Vision API
- 카메라 촬영 이미지 기반 숫자 인식

## Notes

LLM 기반 기차표 예매 에이전트 프로젝트는 별도 저장소 [`korail-booking-llm-agent`](https://github.com/Deahyun/korail-booking-llm-agent) 로 분리되었습니다.
