package com.example.aiocrdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aiocrdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()
    private val API_KEY = BuildConfig.OPENAI_API_KEY
    private val CAMERA_PERMISSION = Manifest.permission.CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 카메라 권한 확인
        if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION), 0)
        }

        // 🔹 카메라 촬영 후 결과 받기
        val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                binding.imageView.setImageBitmap(bitmap)
                sendImageToGPT(bitmap)
            }
        }

        // 🔹 버튼 클릭 시 촬영 시작
        binding.btnCamera.setOnClickListener {
            takePicture.launch(null)
        }
    }

    private fun sendImageToGPT(bitmap: Bitmap) {
        binding.progressBar.visibility = View.VISIBLE
        binding.txtResult.text = "이미지 분석 중입니다..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val base64 = bitmapToBase64(bitmap)
                val imageUrl = "data:image/jpeg;base64,$base64"

                // ✅ 수정된 부분: image_url → 객체로 전달해야 함
                val jsonBody = """
                    {
                      "model": "gpt-4o-mini",
                      "messages": [
                        {
                          "role": "user",
                          "content": [
                            {
                              "type": "text",
                              "text": "이 이미지 내의 빨간색 박스내의 숫자는 제외하고 검정색 바탕에 하얀색 숫자를 인식해줘. 빨간색 박스를 제외한 숫자의 길이는 5자리야."
                            },
                            {
                              "type": "image_url",
                              "image_url": {
                                "url": "$imageUrl"
                              }
                            }
                          ]
                        }
                      ]
                    }
                """.trimIndent()

                val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful && body != null) {
                        val text = parseOCRResult(body)
                        binding.txtResult.text = "✅ OCR 결과:\n\n$text"
                    } else {
                        val msg = try {
                            JSONObject(body ?: "{}").optJSONObject("error")?.optString("message")
                                ?: "알 수 없는 오류"
                        } catch (e: Exception) {
                            "요청 오류 (code ${response.code})"
                        }
                        binding.txtResult.text = "❌ 오류 발생: $msg"
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.txtResult.text = "❌ 네트워크 또는 처리 오류: ${e.message}"
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        // 🔹 이미지 크기 축소 및 압축 (400KB 이하)
        val scaled = Bitmap.createScaledBitmap(bitmap, 800, 800, true)
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun parseOCRResult(responseText: String): String {
        val json = JSONObject(responseText)
        val content = json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
        return content.trim()
    }
}