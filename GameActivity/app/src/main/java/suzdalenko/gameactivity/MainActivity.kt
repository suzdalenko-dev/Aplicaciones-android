package suzdalenko.gameactivity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import com.google.androidgamesdk.GameActivity

class MainActivity : GameActivity() {
    companion object {
        init {
            System.loadLibrary("gameactivity")
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUi()
        }
    }

    private fun hideSystemUi() {
        window.setDecorFitsSystemWindows(false)
        val windowInsetsController = window.insetsController
        if (windowInsetsController != null) {
            windowInsetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsets.Type.systemBars())
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(DrawingView(this))
    }
}

class DrawingView(context: Context) : View(context) {
    private val paint = Paint().apply {
        color = Color.RED
        textSize = 60f
        isAntiAlias = true
    }
    private var xPos = 0f
    private var yPos = 100f
    private val updateRate = 16L // 16ms for roughly 60fps

    init {
        // Start a loop to update the position
        post{
            updatePosition.run()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw a circle
        canvas.drawCircle(xPos, yPos, 50f, paint)
        // Draw some text
        canvas.drawText("Hello World!", 100f, 200f, paint)
    }

    private val updatePosition = object : Runnable {
        override fun run() {
            // Update the x position
            xPos += 5
            if (xPos > width) {
                xPos = 0f
            }
            // Request to redraw the view
            invalidate()
            // Schedule the next update
            postDelayed(this, updateRate)
        }
    }
}