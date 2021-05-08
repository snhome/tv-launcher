package nl.ndat.tvlauncher.data

import android.content.Intent
import android.graphics.drawable.Drawable

data class AppInfo(
	val label: String,
	val banner: Drawable,
	val intent: Intent?,
)
