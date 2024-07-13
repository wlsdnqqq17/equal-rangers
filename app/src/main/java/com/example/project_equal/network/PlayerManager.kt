import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.PlayerData
import com.example.project_equal.network.RefreshTokenRequest
import com.example.project_equal.network.RefreshTokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class PlayerManager(private val apiService: ApiService, private val context: Context) {

    private val base_url = "http://52.78.68.85:8000"

    suspend fun getPlayerInfo(userId: String, accessToken: String): PlayerData {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$base_url/api/players/$userId/"
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                if (!response.isSuccessful) {
                    if (response.code == 401) {
                        // 액세스 토큰이 만료된 경우 토큰 갱신 시도
                        Log.d("PlayerManager", "Access token expired. Refreshing token...")
                        val newAccessToken = refreshAccessToken()
                        return@withContext getPlayerInfo(userId, newAccessToken)
                    } else {
                        throw IOException("Unexpected code $response")
                    }
                }

                val responseBody = response.body
                if (responseBody == null) {
                    throw IOException("Empty response body")
                }

                val json = JSONObject(responseBody.string())
                PlayerData(
                    userId = json.getString("user_id"),
                    nickname = json.getString("nickname"),
                    email = json.getString("email"),
                    gold = json.getInt("gold"),
                    item = listOf(0),
                    highscore = json.getInt("highscore")
                )
            } catch (e: IOException) {
                throw IOException("Failed to fetch player information: ${e.message}", e)
            } catch (e: JSONException) {
                throw IOException("Failed to parse JSON response", e)
            }
        }
    }

    private suspend fun refreshAccessToken(): String {
        return withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val tokensJson = sharedPreferences.getString("user_token", null) ?: throw IOException("Tokens not found")
            val refreshToken = JSONObject(tokensJson).getString("refresh")

            val refreshRequest = RefreshTokenRequest(refreshToken)
            val refreshResponse: Response<RefreshTokenResponse> = apiService.refreshToken(refreshRequest).execute()

            if (refreshResponse.isSuccessful) {
                val newTokens = refreshResponse.body()
                if (newTokens != null) {
                    // 새로운 토큰을 저장
                    val tokensJson = JSONObject()
                    tokensJson.put("access", newTokens.accessToken)
                    tokensJson.put("refresh", newTokens.refreshToken)

                    val tokens = tokensJson.toString()
                    sharedPreferences.edit().putString("user_token", tokens).apply()

                    newTokens.accessToken
                } else {
                    throw IOException("Failed to refresh tokens: empty response body")
                }
            } else {
                throw IOException("Failed to refresh tokens: ${refreshResponse.code()}")
            }
        }
    }

    suspend fun updatePlayerInfo(userId: String, playerData: PlayerData, accessToken: String): PlayerData {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updatePlayerInfo(userId, playerData).execute()
                if (response.isSuccessful) {
                    response.body() ?: throw IOException("Failed to update player information: empty response body")
                } else {
                    throw IOException("Failed to update player information: ${response.code()}")
                }
            } catch (e: IOException) {
                throw IOException("Failed to update player information: ${e.message}", e)
            }
        }
    }

    suspend fun updatePlayerInfoPartial(userId: String, partialData: Map<String, Any>, accessToken: String): PlayerData {
        return withContext(Dispatchers.IO) {
            try {
                val currentPlayer = getPlayerInfo(userId, accessToken)
                val updatedData = currentPlayer.copy(
                    userId = partialData["userId"] as String? ?: currentPlayer.userId,
                    nickname = partialData["nickname"] as String? ?: currentPlayer.nickname,
                    email = partialData["email"] as String? ?: currentPlayer.email,
                    gold = partialData["gold"] as Int? ?: currentPlayer.gold,
                    highscore = partialData["highscore"] as Int? ?: currentPlayer.highscore
                    // Handle other fields similarly
                )
                val response = apiService.updatePlayerInfo(userId, updatedData).execute()
                if (response.isSuccessful) {
                    response.body() ?: throw IOException("Failed to update player information: empty response body")
                } else {
                    throw IOException("Failed to update player information: ${response.code()}")
                }
            } catch (e: IOException) {
                throw IOException("Failed to update player information: ${e.message}", e)
            }
        }
    }
}