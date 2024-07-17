package com.example.project_equal.ui.activity

import android.content.Intent
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R

class SplashActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var textureView: TextureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        val surface = Surface(surfaceTexture)
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@SplashActivity, Uri.parse("android.resource://" + packageName + "/" + R.raw.splash))
            setSurface(surface)
            setOnPreparedListener { mp ->
                adjustAspectRatio(mp.videoWidth, mp.videoHeight, width, height)
                mp.start()
            }
            setOnCompletionListener {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)

                finish()
            }
            prepareAsync()
        }
    }

    private fun adjustAspectRatio(videoWidth: Int, videoHeight: Int, viewWidth: Int, viewHeight: Int) {
        val aspectRatio = videoWidth.toFloat() / videoHeight.toFloat()
        val viewAspectRatio = viewWidth.toFloat() / viewHeight.toFloat()

        val scaleX: Float
        val scaleY: Float

        if (aspectRatio > viewAspectRatio) {
            scaleX = viewHeight * aspectRatio / viewWidth
            scaleY = 1f
        } else {
            scaleX = 1f
            scaleY = viewWidth / (viewHeight * aspectRatio)
        }

        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
        textureView.setTransform(matrix)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // No-op
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        mediaPlayer.release()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // No-op
    }
}
