package nl.ndat.tvlauncher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import nl.ndat.tvlauncher.data.SharedDatabase
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.repository.ChannelRepository
import nl.ndat.tvlauncher.data.repository.InputRepository
import nl.ndat.tvlauncher.data.repository.PreferenceRepository
import nl.ndat.tvlauncher.data.resolver.AppResolver
import nl.ndat.tvlauncher.data.resolver.ChannelResolver
import nl.ndat.tvlauncher.data.resolver.InputResolver
import nl.ndat.tvlauncher.util.DefaultLauncherHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import timber.log.Timber


private val launcherModule = module {
	single { DefaultLauncherHelper(get()) }

	single { AppRepository(get(), get(), get(), get()) }
	single { AppResolver() }

	single { ChannelRepository(get(), get(), get(), get(), get()) }
	single { ChannelResolver() }

	single { InputRepository(get(), get(), get(), get()) }
	single { InputResolver() }

	single { PreferenceRepository() }
}

private val databaseModule = module {
	// Create database(s)
	single { SharedDatabase.build(get()) }

	// Add DAOs for easy access
	single { get<SharedDatabase>().appDao() }
	single { get<SharedDatabase>().channelDao() }
	single { get<SharedDatabase>().channelProgramDao() }
	single { get<SharedDatabase>().inputDao() }
}

class BootReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
			val serviceIntent = Intent(context, MyForegroundService::class.java)
			ContextCompat.startForegroundService(context, serviceIntent)
		}
	}
}

class LauncherApplication : Application(), ImageLoaderFactory {
	override fun onCreate() {
		super.onCreate()

		Timber.plant(Timber.DebugTree())

		startKoin {
			androidLogger(level = if (BuildConfig.DEBUG) Level.DEBUG else Level.INFO)
			androidContext(this@LauncherApplication)

			modules(launcherModule, databaseModule)
		}

		//start BootReceiver
		val serviceIntent = Intent(this, MyForegroundService::class.java)
		ContextCompat.startForegroundService(this, serviceIntent)



	}

	override fun newImageLoader()= ImageLoader.Builder(this)
		.logger(DebugLogger())
		.build()
}
