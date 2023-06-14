package nl.ndat.tvlauncher

import android.R
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.tvprovider.media.tv.TvContractCompat
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.launch
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.repository.ChannelRepository
import nl.ndat.tvlauncher.data.repository.InputRepository
import nl.ndat.tvlauncher.ui.AppBase
import nl.ndat.tvlauncher.util.DefaultLauncherHelper
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.koin.android.ext.android.inject

class LauncherActivity : ComponentActivity() {
	private val defaultLauncherHelper: DefaultLauncherHelper by inject()
	private val appRepository: AppRepository by inject()
	private val inputRepository: InputRepository by inject()
	private val channelRepository: ChannelRepository by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppBase()
		}

		validateDefaultLauncher()

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.RESUMED) {
				appRepository.refreshAllApplications()
				inputRepository.refreshAllInputs()
				channelRepository.refreshAllChannels()
			}
		}

		if (checkCallingOrSelfPermission(TvContractCompat.PERMISSION_READ_TV_LISTINGS) != PackageManager.PERMISSION_GRANTED)
			ActivityCompat.requestPermissions(this, arrayOf(TvContractCompat.PERMISSION_READ_TV_LISTINGS), 0)

		val serviceIntent = Intent(this, MyForegroundService::class.java)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(serviceIntent)
		} else {
			startService(serviceIntent)
		}

		connect(this)
	}


	private lateinit var mqttClient: MqttAndroidClient;


	fun subscribeToTopic() {
		val topic = "example/topic"
		val qos = 1

		try {
			mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
				override fun onSuccess(asyncActionToken: IMqttToken) {
					// Successfully subscribed to topic
					Log.d(MyForegroundService.TAG, "Successfully subscribed to topic")
				}

				override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
					// Failed to subscribe to topic
					Log.d(MyForegroundService.TAG, "Failed to subscribe to topic")
				}
			})
		} catch (ex: MqttException) {
			ex.printStackTrace()
		}
	}
	fun connect(context: Context) {
		val CHANNEL_ID = "MyForegroundServiceChannel"

		val username = "user"
		val password = "password"
		val uri = "tcp://192.168.1.145:1883"
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
				Log.d(MyForegroundService.TAG, "connectComplete")
				if (reconnect) {
					// Because Clean Session is true, we need to re-subscribe
					subscribeToTopic()

				} else {
					mqttClient.publish("example/topic", MqttMessage("Hello world".toByteArray()))
					// Successfully connected to MQTT broker
				}
			}

			override fun connectionLost(cause: Throwable?) {
				Log.d(MyForegroundService.TAG, "connectionLost")
			}

			@Throws(Exception::class)
			override fun messageArrived(topic: String, message: MqttMessage) {
				Log.d(MyForegroundService.TAG, "messageArrived: $topic - ${message.toString()}")
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

	private fun validateDefaultLauncher() {
		if (!defaultLauncherHelper.isDefaultLauncher() && defaultLauncherHelper.canRequestDefaultLauncher()) {
			val intent = defaultLauncherHelper.requestDefaultLauncherIntent()
			@Suppress("DEPRECATION")
			if (intent != null) startActivityForResult(intent, 0)
		}
	}

}
