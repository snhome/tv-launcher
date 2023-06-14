package nl.ndat.tvlauncher

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog

// this is a activity to block the launcher user interaction
class BlankBlockingActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
		super.onCreate(savedInstanceState, persistentState)

		// set the content view to a video layout
		setContentView(R.layout.video_layout)

		val builder = AlertDialog.Builder(this)
		builder.setTitle("Title")
		builder.setMessage("Message")
		builder.setPositiveButton("OK") { dialog, which ->
			// Do something when OK is clicked
		}
		builder.setNegativeButton("Cancel") { dialog, which ->
			// Do something when Cancel is clicked
		}
		val dialog = builder.create()
		dialog.show()

		// play a video
		// https://stackoverflow.com/questions/3028306/how-to-play-video-from-assets-folder-into-videoview


	}

}
