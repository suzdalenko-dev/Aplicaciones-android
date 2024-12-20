package suzdalenko.photolapse.util

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import suzdalenko.photolapse.R
import suzdalenko.photolapse.util.MyApp.Companion.prefs

object PlaySound {
    fun playSoundGetFoto(context: Context) {
        if (prefs.getString("sound", "x") == "sound"){
            try {
                val mediaPlayer = MediaPlayer.create(context, R.raw.camera_shutter)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.release()
                }
            } catch (_: Exception){

            }
        }
    }
    fun errorSoundGetFoto(context: Context) {
        if (prefs.getString("sound", "x") == "sound"){
            try {
                val mediaPlayer = MediaPlayer.create(context, R.raw.error_photo)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.release()
                }
            } catch (_: Exception){

            }
        }
    }
    fun uploadFileSound(context: Context) {
        if (prefs.getString("sound", "x") == "sound") {
            try {
                val mediaPlayer = MediaPlayer.create(context, R.raw.upload_positive)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.release()
                }
            } catch (_: Exception) {

            }
        }
    }
    fun errorFileSound(context: Context) {
        if (prefs.getString("sound", "x") == "sound") {
            try {
                val mediaPlayer = MediaPlayer.create(context, R.raw.error_upload)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.release()
                }
            } catch (_: Exception) {

            }
        }
    }

}