package com.kcalfit.app.data.health

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.hardware.*
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.*
import com.kcalfit.app.R
import com.kcalfit.app.MainActivity
import java.time.*
import java.util.concurrent.TimeUnit

/** A source replaces another source; sensor counts are never added to Health Connect aggregates. */
class StepTracking(private val context:Context) {
    val prefs=context.getSharedPreferences("step_tracking",Context.MODE_PRIVATE)
    val available get()=HealthConnectClient.getSdkStatus(context)==HealthConnectClient.SDK_AVAILABLE
    val permissions=setOf(HealthPermission.getReadPermission(StepsRecord::class),HealthPermission.getReadPermission(DistanceRecord::class))
    fun count(date:String=LocalDate.now().toString()):Long? = when {
        prefs.getString("source","")=="health" && prefs.contains("health_$date") -> prefs.getLong("health_$date",0)
        prefs.getString("source","")=="sensor" && prefs.contains("sensor_$date") -> prefs.getLong("sensor_$date",0)
        else -> null
    }
    /** Only the device sensor updates as the user moves; Health Connect totals sync every 15 minutes. */
    fun liveSensorCount(date:String=LocalDate.now().toString()):Long? = if(prefs.getString("source","")=="sensor" && prefs.contains("sensor_$date")) prefs.getLong("sensor_$date",0) else null
    fun distance(date:String=LocalDate.now().toString()):Double? = if(prefs.getString("source","")=="health" && prefs.contains("distance_$date")) Double.fromBits(prefs.getLong("distance_$date",0)) else null
    suspend fun sync(background:Boolean=false):Boolean {
        if(!available || prefs.getString("source","")!="health") return false
        val client=HealthConnectClient.getOrCreate(context)
        val granted=client.permissionController.getGrantedPermissions()
        if(!granted.containsAll(permissions)) {prefs.edit().putString("source","").apply();return false}
        if(background && HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND !in granted)return false
        val zone=ZoneId.systemDefault();val date=LocalDate.now(zone)
        val result=client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL,DistanceRecord.DISTANCE_TOTAL),TimeRangeFilter.between(date.atStartOfDay(zone).toInstant(),Instant.now())))
        val edit=prefs.edit().putLong("health_$date",result[StepsRecord.COUNT_TOTAL]?:0L).putLong("synced",System.currentTimeMillis())
        result[DistanceRecord.DISTANCE_TOTAL]?.let{edit.putLong("distance_$date",it.inMeters.toBits())}
        edit.apply();return true
    }
    fun useHealth() {context.stopService(Intent(context,StepCounterService::class.java));prefs.edit().putString("source","health").apply()}
    fun stop() {context.stopService(Intent(context,StepCounterService::class.java));prefs.edit().putString("source","").apply();WorkManager.getInstance(context).cancelUniqueWork("kcalfit_health_sync")}
    fun schedule() {WorkManager.getInstance(context).enqueueUniquePeriodicWork("kcalfit_health_sync",ExistingPeriodicWorkPolicy.KEEP,PeriodicWorkRequestBuilder<StepSyncWorker>(15,TimeUnit.MINUTES).build())}
    fun backgroundAvailable():Boolean=available && HealthConnectClient.getOrCreate(context).features.getFeatureStatus(HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND)==HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
}
class StepSyncWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params) {
    override suspend fun doWork():Result=try{StepTracking(applicationContext).sync(true);Result.success()}catch(_:SecurityException){Result.success()}catch(_:Exception){Result.retry()}
}
class StepCounterService:Service(),SensorEventListener {
    private lateinit var tracking:StepTracking
    private lateinit var sensors:SensorManager
    override fun onBind(intent:Intent?):IBinder?=null
    override fun onCreate(){super.onCreate();tracking=StepTracking(this);sensors=getSystemService(SENSOR_SERVICE) as SensorManager}
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        if(Build.VERSION.SDK_INT>=29 && ContextCompat.checkSelfPermission(this,Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED){stopSelf();return START_NOT_STICKY}
        val sensor=sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?:run{stopSelf();return START_NOT_STICKY}
        val notifications=getSystemService(NotificationManager::class.java)
        notifications.createNotificationChannel(NotificationChannel("steps","Step tracking",NotificationManager.IMPORTANCE_LOW))
        val open=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE)
        startForeground(2101,NotificationCompat.Builder(this,"steps").setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("kcalfit step tracking").setContentText("Counting device steps. Manage in More → Automatic steps.").setOngoing(true).setContentIntent(open).build())
        tracking.prefs.edit().putString("source","sensor").apply()
        sensors.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL)
        return START_NOT_STICKY
    }
    override fun onSensorChanged(event:SensorEvent){
        val count=event.values[0].toLong();val date=LocalDate.now().toString();val prefs=tracking.prefs
        val previous=prefs.getLong("counter",count);val sameDay=prefs.getString("counter_date","")==date
        // A reset/reboot or a midnight-spanning batch cannot be assigned safely: start a new baseline.
        val delta=stepCounterDelta(previous,count,sameDay)
        prefs.edit().putLong("counter",count).putString("counter_date",date).putLong("sensor_$date",prefs.getLong("sensor_$date",0)+delta).apply()
    }
    override fun onAccuracyChanged(sensor:Sensor?,accuracy:Int){}
    override fun onDestroy(){sensors.unregisterListener(this);super.onDestroy()}
}
