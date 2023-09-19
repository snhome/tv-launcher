package nl.ndat.tvlauncher

import android.R
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage


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
	private lateinit var mqttClient: MqttAndroidClient;

	fun startBlackScreen() {
		Log.d("MyService", "startBlackScreen()")
		// start on top of all activities
		val intent = Intent(this@MyForegroundService, BlankBlockingActivity::class.java)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		startActivity(intent)
	}

	fun noYoutu() {
		startBlackScreen()
	}

	fun connect(context: Context) {
		val CHANNEL_ID = "MyForegroundServiceChannel"
		val username = "tvlauncher"
		val password = "GXlr9Z5OAw5Q11rwicahH89B9R35aw8A"
		val uri = "tcp://192.168.1.25:1883"
		val topic = "ha/tvbox"
		val clientId = "mqtt-explorer-ffc1707f"
		mqttClient = MqttAndroidClient(context, uri, clientId).apply {
			// create forground notification
			val foregroundNotification = NotificationCompat.Builder(context, CHANNEL_ID)
				.setContentTitle("My Foreground Service")
				.setContentText("My Foreground Service is running")
				.setSmallIcon(R.drawable.ic_dialog_email)
				.build()

			setForegroundService(foregroundNotification)
		}

		mqttClient.setCallback(object : MqttCallbackExtended {
			override fun connectComplete(reconnect: Boolean, serverURI: String?) {
				Log.d(MyForegroundService.TAG, "connectComplete " + reconnect)

				if (!reconnect) {
					// Because Clean Session is true, we need to re-subscribe
					subscribeToTopic(topic)
				} else {
					mqttClient.publish(topic, MqttMessage("tvbox online".toByteArray()))
					// Successfully connected to MQTT broker
				}
			}

			override fun connectionLost(cause: Throwable?) {
				Log.d(MyForegroundService.TAG, "connectionLost")
			}

			@Throws(Exception::class)
			override fun messageArrived(topic: String, message: MqttMessage) {
				val msg = message.toString()

				if (msg == "black") {
					noYoutu()
				}
				Log.d(MyForegroundService.TAG, "messageArrived: $topic - ${msg}")
			}

			override fun deliveryComplete(token: IMqttDeliveryToken) {
				Log.d(MyForegroundService.TAG, "deliveryComplete")
			}
		})


		val mqttConnectOptions = MqttConnectOptions()
		mqttConnectOptions.isAutomaticReconnect = true
		mqttConnectOptions.isCleanSession = false
		mqttConnectOptions.userName = username
		mqttConnectOptions.password = password.toCharArray()

		try {
			mqttClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
				override fun onSuccess(asyncActionToken: IMqttToken) {
					// Successfully connected to MQTT broker
					Log.d(MyForegroundService.TAG, "onSuccess")
				}

				override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
					// Failed to connect to MQTT broker
					Log.d(MyForegroundService.TAG, "onFailure")
				}
			})
		} catch (ex: MqttException) {
			ex.printStackTrace()
		}


	}

	fun subscribeToTopic(topic: String) {
		val qos = 1

		try {
			mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
				override fun onSuccess(asyncActionToken: IMqttToken) {
					// Successfully subscribed to topic
					Log.d(MyForegroundService.TAG, "Successfully subscribed to topic ${topic}")
				}

				override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
					// Failed to subscribe to topic
					Log.d(MyForegroundService.TAG, "Failed to subscribe to topic ${topic}")
				}
			})
		} catch (ex: MqttException) {
			ex.printStackTrace()
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


//		val executor = Executors.newSingleThreadExecutor()
//		executor.execute {
//			while (true) {
//				Thread.sleep(5000)
//				startBlackScreen()
//			}
//		}

		connect(this)


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

