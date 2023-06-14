package nl.ndat.tvlauncher

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.concurrent.Executors

class MyForegroundService : Service() {
	private val CHANNEL_ID = "MyForegroundServiceChannel"
	companion object {
		const val TAG = "AndroidMqttClient"
	}


	override fun onCreate() {
		super.onCreate()

		// log the service creation
		Log.d("MyService", "onCreate()")


		if (Build.VERSION.SDK_INT >= 26) {
			val channel = NotificationChannel(
				CHANNEL_ID,
				"Channel human readable title",
				NotificationManager.IMPORTANCE_DEFAULT
			)
			(getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
			val notification = NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentTitle("")
				.setContentText("").build()
			startForeground(1, notification)
		}




	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//		val input = intent?.getStringExtra("inputExtra")
//
//		val notificationIntent = Intent(this, LauncherActivity::class.java)
//		val pendingIntent = PendingIntent.getActivity(
//			this,
//			0, notificationIntent, 0
//		)
//
//		val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
//			.setContentTitle("My Foreground Service")
//			.setContentText(input)
//			.setSmallIcon(R.drawable.ic_dialog_email)
//			.setContentIntent(pendingIntent)
//			.build()
//
//		startForeground(1, notification)
		val executor = Executors.newSingleThreadExecutor()
		executor.execute {
			while (true) {
				Thread.sleep(5000)
				val intent = Intent(this@MyForegroundService, BlankBlockingActivity::class.java)
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(intent)
			}
		}


		return START_NOT_STICKY
	}

	fun startListenMQ() {
		// listen a message broker, the protocol is mqtt, hosted in rabbitmq
		// if message is "block" start BlankBlockingActivity
		// if message is "unblock" stop BlankBlockingActivity



	}



	override fun onDestroy() {
		super.onDestroy()
	}

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}
}

