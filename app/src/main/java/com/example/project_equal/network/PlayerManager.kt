import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.PlayerData
import com.example.project_equal.network.RefreshTokenRequest
import com.example.project_equal.network.RefreshTokenResponse
import com.example.project_equal.ui.activity.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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
                Log.d("TEST", "BEFORE FAIL")

                val itemJsonArray = json.getJSONArray("item")
                Log.d("TEST", "NOOOOOOOOOOTTTTTTT FAIL TO PARSE ITEM JSON ARRRAY")
                val itemList = mutableListOf<Int>()
                for (i in 0 until itemJsonArray.length()) {
                    itemList.add(itemJsonArray.getInt(i))
                }
//                PlayerData(
//                    userId = json.getString("user_id"),
//                    nickname = json.getString("nickname"),
//                    gold = json.getInt("gold"),
//                    item = itemList,
//                    highscore = json.getInt("highscore")
//                )

//                Log.d("TEST", "NOOOOOOOOOOTTTTTTT FAIL TO PARSE ITEM JSON ARRRAY")
                PlayerData(
                    userId = json.getString("user_id"),
                    nickname = json.getString("nickname"),
                    gold = json.getInt("gold"),
                    item = itemList,
                    highscore = json.getInt("highscore")
                )

            } catch (e: IOException) {
                throw IOException("Failed to fetch player information: ${e.message}", e)
            } catch (e: JSONException) {
                throw IOException("Failed to parse JSON response", e)
            }
        }
    }

    private fun navigateToLogin() {
        if (context is Activity) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            context.finish()

            context.startActivity(intent)
        } else {
            Log.e("navigateToLogin", "Provided context is not an Activity instance")
        }
    }
    suspend fun refreshAccessToken(): String {
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
                if (refreshResponse.code() == 401) {
                    Log.d("PlayerManager", "Refresh token expired or invalid. Redirecting to login.")
                    navigateToLogin()
                    throw IOException("Refresh token expired or invalid. Redirecting to login.")
                }
                throw IOException("Failed to refresh tokens: ${refreshResponse.code()}")
            }
        }
    }

//    suspend fun updatePlayerInfo(userId: String, playerData: PlayerData, accessToken: String): PlayerData {
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = apiService.updatePlayerInfo(userId, playerData).execute()
//                if (response.isSuccessful) {
//                    response.body() ?: throw IOException("Failed to update player information: empty response body")
//                } else {
//                    throw IOException("Failed to update player information: ${response.code()}")
//                }
//            } catch (e: IOException) {
//                throw IOException("Failed to update player information: ${e.message}", e)
//            }
//        }
//    }

    suspend fun getAccessToken(sharedPreferences: SharedPreferences): String {
        return withContext(Dispatchers.IO) {
            try {
                val tokensJsonString = sharedPreferences.getString("user_token", null)
                val tokensJson = JSONObject(tokensJsonString ?: throw IOException("User token not found or is null"))

                tokensJson.getString("access")
            } catch (e: IOException) {
                throw IOException("Failed to read tokens.json")
            }
        }
    }

    suspend fun updatePlayerInfo(userId: String, playerData: PlayerData, accessToken: String): PlayerData {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$base_url/api/players/$userId/update/"
                val json = JSONObject()
                json.put("nickname", playerData.nickname)
                json.put("gold", playerData.gold)
                json.put("highscore", playerData.highscore)
                json.put("item", playerData.item)
                // Add other fields as needed

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $accessToken")
                    .put(body)
                    .build()

                val response = OkHttpClient().newCall(request).execute()
//                if (!response.isSuccessful) {
//                    throw IOException("Failed to update player information: ${response.code}")
//                }
                if (!response.isSuccessful) {
                    if (response.code == 401) {
                        // 액세스 토큰이 만료된 경우 토큰 갱신 시도
                        Log.d("PlayerManager", "Access token expired. Refreshing token...")
                        val newAccessToken = refreshAccessToken()
                        // Retry with the new access token
                        return@withContext updatePlayerInfo(userId, playerData, newAccessToken)
                    } else {
                        throw IOException("Unexpected code $response")
                    }
                }

                val responseBody = response.body
                if (responseBody == null) {
                    throw IOException("Empty response body")
                }

                val jsonResponse = JSONObject(responseBody.string())
                PlayerData(
                    userId = jsonResponse.getString("user_id"),
                    nickname = jsonResponse.getString("nickname"),
                    gold = jsonResponse.getInt("gold"),
                    item = listOf(0),
                    highscore = jsonResponse.getInt("highscore")
                )
            } catch (e: IOException) {
                throw IOException("Failed to update player information: ${e.message}", e)
            } catch (e: JSONException) {
                throw IOException("Failed to parse JSON response", e)
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