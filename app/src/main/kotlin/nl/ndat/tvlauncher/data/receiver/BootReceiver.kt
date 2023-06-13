package nl.ndat.tvlauncher.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import nl.ndat.tvlauncher.MyForegroundService
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		Timber.tag("BootReceiver").d("onReceive")
		if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
			val serviceIntent = Intent(context, MyForegroundService::class.java)
			ContextCompat.startForegroundService(context, serviceIntent)
		}
	}
}
