package pawel.hn.simplepainterapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private lateinit var drawPath: CustomPath
    private lateinit var canvasBitmap: Bitmap
    private lateinit var drawPaint: Paint
    private lateinit var canvasPaint: Paint
    private lateinit var canvas: Canvas
    private var brushSize: Float = 0F
    private var paintColor = Color.BLACK
    val drawPaths = ArrayList<CustomPath>()

    init {
        setUpPainter()
    }

    private fun setUpPainter() {
        drawPaint = Paint()
        drawPath = CustomPath(paintColor, brushSize)
        drawPaint.apply {
            color = paintColor
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        canvasPaint = Paint()
        brushSize = resources.getFloat(R.dimen.brush_size_small)
    }

    // called when
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap)
    }

    override fun onDraw(canvas: Canvas?) {
        Log.d("PHN", "onDraw called")
        canvas?.drawBitmap(canvasBitmap, 0F, 0F, canvasPaint)

        drawPaths.forEach {
            drawPaint.strokeWidth = it.brushThickness
            drawPaint.color = it.color
            canvas?.drawPath(it, drawPaint)
        }

        drawPaint.strokeWidth = drawPath.brushThickness
        drawPaint.color = drawPath.color
        canvas?.drawPath(drawPath, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.apply {
                    color = paintColor
                    brushThickness = brushSize
                    reset()
                    moveTo(touchX!!, touchY!!)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_UP -> {
                drawPaths.add(drawPath)
                drawPath = CustomPath(paintColor, brushSize)
            }
            else -> return false
        }

        performClick()
        invalidate()
        return true
    }

    fun setBrushSize(size: Float) {
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, size, resources.displayMetrics
        )
        drawPaint.strokeWidth = brushSize
    }

    fun setColor(color: String) {
        paintColor = Color.parseColor(color)
    }

    fun undo() {
        if (drawPaths.isNotEmpty()) {
            drawPaths.removeLast()
        }
        invalidate()
    }

    fun clearAll() {
        drawPaths.clear()
        invalidate()
    }


    //class for draw path
    inner class CustomPath(
        var color: Int,
        var brushThickness: Float
    ) : Path()
}