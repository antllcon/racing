import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mobility.race.R

class SoundManager(private val context: Context) {
    private val soundPool: SoundPool
    private var startSoundId: Int = 0
    private var clickSoundId: Int = 0

    private val exoPlayer: ExoPlayer

    private val surfaceSounds = mutableMapOf<String, Int>()
    private var currentSurfaceSoundId: Int? = null
    private var currentSurfaceStreamId: Int? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        startSoundId = soundPool.load(context, R.raw.start, 1)
        clickSoundId = soundPool.load(context, R.raw.click, 1)
        surfaceSounds["ROAD"] = soundPool.load(context, R.raw.road, 1)
        surfaceSounds["GRASS"] = soundPool.load(context, R.raw.grass, 1)
        surfaceSounds["WATER"] = soundPool.load(context, R.raw.water, 1)

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(C.USAGE_GAME)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SONIFICATION)
                    .build(),
                true
            )
            .build()
    }


    fun playStartSound() {
        soundPool.play(startSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playBackgroundMusic() {
        val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.phonk}")
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }
    fun playClickSound() {
        soundPool.play(clickSoundId, 1f, 1f, 0, 0, 1f)
    }
    fun playSurfaceSound(surfaceType: String, volume: Float = 1f) {
        currentSurfaceStreamId?.let { soundPool.stop(it) }

        val soundId = surfaceSounds[surfaceType] ?: surfaceSounds["ROAD"]!!

        currentSurfaceStreamId = soundPool.play(soundId, volume, volume, 0, -1, 1f)
        currentSurfaceSoundId = soundId
    }

    fun updateSurfaceSoundVolume(volume: Float) {
        currentSurfaceStreamId?.let {
            soundPool.setVolume(it, volume, volume)
        }
    }

    fun stopSurfaceSound() {
        currentSurfaceStreamId?.let {
            soundPool.stop(it)
            currentSurfaceStreamId = null
            currentSurfaceSoundId = null
        }
    }
    fun playMenuMusic() {
        val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.backround_menu}")
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun stopBackgroundMusic() {
        exoPlayer.stop()
    }

    fun pauseBackgroundMusic() {
        exoPlayer.pause()
    }

    fun resumeBackgroundMusic() {
        exoPlayer.play()
    }
    fun setMusicVolume(volume: Float) {
        exoPlayer.volume = volume
    }

    fun release() {
        soundPool.release()
        exoPlayer.release()
    }
}