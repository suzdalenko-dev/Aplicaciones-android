package suzdalenko.photolapse.util

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import suzdalenko.photolapse.R

object PlaySound {
    fun playSoundGetFoto(context: Context) {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.camera_shutter)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        } catch (_: Exception){

        }
    }
    fun errorSoundGetFoto(context: Context) {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.error_photo)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        } catch (_: Exception){

        }
    }
    fun uploadFileSound(context: Context) {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.upload_positive)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        } catch (_: Exception){

        }
    }
    fun errorFileSound(context: Context) {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.error_upload)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        } catch (_: Exception){

        }
    }

}