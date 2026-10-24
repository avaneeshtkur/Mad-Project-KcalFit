package com.kcalfit.app.data.premium

import com.kcalfit.app.BuildConfig
import com.kcalfit.app.CalFitApplication
import com.kcalfit.app.data.repository.SupabaseAuthService
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.net.*

data class ScanIngredient(val name:String,val grams:Double)
data class Entitlement(val premium:Boolean=false,val freeRemaining:Int=5)
class PremiumApi {
    companion object {private val sessionLock=Mutex()}
    suspend fun call(function:String,body:JSONObject=JSONObject()):JSONObject=withContext(Dispatchers.IO){
        require(BuildConfig.SUPABASE_URL.startsWith("https://")){"Secure backend is not configured."}
        val prefs=CalFitApplication.instance.userPreferences
        val session=sessionLock.withLock {SupabaseAuthService().refresh(prefs.getRefreshToken()).also{prefs.saveAuthSession(it.userId,it.refreshToken)}}
        val connection=(URL("${BuildConfig.SUPABASE_URL.trimEnd('/')}/functions/v1/$function").openConnection() as HttpURLConnection).apply{
            requestMethod="POST";doOutput=true;connectTimeout=15000;readTimeout=65000
            setRequestProperty("Authorization","Bearer ${session.accessToken}");setRequestProperty("apikey",BuildConfig.SUPABASE_PUBLISHABLE_KEY);setRequestProperty("Content-Type","application/json")
        }
        try{connection.outputStream.use{it.write(body.toString().toByteArray())};val code=connection.responseCode
            val raw=(if(code in 200..299)connection.inputStream else connection.errorStream)?.bufferedReader()?.use{it.readText()}.orEmpty()
            val json=runCatching{JSONObject(raw)}.getOrDefault(JSONObject())
            if(code !in 200..299)throw IllegalStateException(when(code){404->if(function=="fitness-coach") "The AI coach is not configured on the server yet." else "Food scanning and subscriptions are not configured on the server yet. Manual food logging is available.";402->"Your five free scans are used. Premium is required for further scans.";409->"This scan is already being processed. Retry shortly.";else->json.optString("error","The service is unavailable. Try again.")})
            json
        }finally{connection.disconnect()}
    }
    suspend fun entitlement():Entitlement {val j=call("account-entitlement");return Entitlement(j.optBoolean("premium"),j.optInt("free_remaining",5).coerceIn(0,5))}
    suspend fun scan(base64:String,requestId:String):List<ScanIngredient>{val j=call("scan-food",JSONObject().put("image",base64).put("request_id",requestId));val a=j.getJSONArray("foods");return (0 until a.length()).map{val f=a.getJSONObject(it);ScanIngredient(f.getString("name"),f.getDouble("grams"))}}
}
