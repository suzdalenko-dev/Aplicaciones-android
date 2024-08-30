package radioalarm.suzdalenko.util
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import radioalarm.suzdalenko.util.App.Companion.exoPlayer

object Player {
    fun play(url: String, context: Context) {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        exoPlayer = ExoPlayer.Builder(context).build().also { player ->
            Log.d("Player", "Initializing new ExoPlayer instance.")
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.playWhenReady = true
        }
    }
}