package nl.ndat.tvlauncher

import android.R
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.util.concurrent.Executors

class MyForegroundService : Service() {
	private val CHANNEL_ID = "MyForegroundServiceChannel"

	override fun onCreate() {
		super.onCreate()


		val executor = Executors.newSingleThreadExecutor()
		executor.execute {
			while (true) {
				Thread.sleep(10000)
				val intent = Intent(this@MyForegroundService, BlankBlockingActivity::class.java)
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(intent)
			}
		}

	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		val input = intent?.getStringExtra("inputExtra")

		val notificationIntent = Intent(this, LauncherActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(
			this,
			0, notificationIntent, 0
		)

		val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
			.setContentTitle("My Foreground Service")
			.setContentText(input)
			.setSmallIcon(R.drawable.ic_dialog_email)
			.setContentIntent(pendingIntent)
			.build()

		startForeground(1, notification)
		return START_NOT_STICKY
	}

	override fun onDestroy() {
		super.onDestroy()
	}

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}
}

