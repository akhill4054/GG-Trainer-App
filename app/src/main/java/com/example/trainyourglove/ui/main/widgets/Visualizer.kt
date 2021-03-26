package com.example.trainyourglove.ui.main.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView
import com.example.trainyourglove.R
import kotlinx.coroutines.*

private const val CHANGE_VELOCITY =
    0.03F // Ranges from 0.0F (0% of |minValue - maxValue|) to 1.0F (100% of |minValue - maxValue|).
private const val BAR_COUNT = 25 // Must be an odd number.
private const val BAR_WIDTH =
    0.8F // Ranges from 0.0F (0% of max width) to 1.0F (100% of max width).

private const val TAG = "Visualizer"

class Visualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr) {

    private var barFillColor = Color.WHITE

    init {
        // To make the view transparent.
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)

        // Setting view attrs
        if (attrs != null) {
            val typedArray =
                getContext().obtainStyledAttributes(attrs, R.styleable.Visualizer, 0, 0)

            barFillColor = typedArray.getColor(R.styleable.Visualizer_bar_color, barFillColor)

            typedArray.recycle()
        }
    }

    private val mCoroutineScope = CoroutineScope(Dispatchers.Default)
    private var mRenderJob: Job? = null

    // m is the size of values to be generated.
    private var m = BAR_COUNT

    init {
        if (m % 2 == 0) {
            m -= 1
        }
    }

    private var mSetMinValue = 0F
    private var mSetMaxValue = 0F
    private var mMinMaxValueDiff = 0F

    private var barHeightChangeVelocity = 0F

    private var mProvidedValues = FloatArray(1) { 0F }
    private var mLastProvidedValues = FloatArray(1) { 0F }

    private val mValues = Array(m) { 0F } // Array of values from 0 to 1
    private val currentValues = Array(m) { 0F }

    private val valueIndices = Array(m) { 0 }

    init {
        for (i in 0 until m) valueIndices[i] = i

        // Shuffling the array using 'Fisher–Yates shuffle Algorithm'.
        for (i in valueIndices.size - 1 downTo 1) {
            val j = (0..i).random()
            valueIndices[i] = j
        }
    }

    private var mSurfaceWidth = 0F
    private var mSurfaceHeight = 0F

    private var midH = 0F
    private var barWidth = 0F
    private var maxBarWidth = 0F
    private var maxBarHeight = 0F
    private var lrBarMargin = 0F
    private var verticalMargin = 0F

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = barFillColor
    }

    /**
     * Size of passed values (ranging from [mSetMinValue] to [mSetMaxValue]) should be non-zero.
     * @param values
     * */
    fun updateValues(values: FloatArray) {
        mProvidedValues = values
    }

    private fun generateValues() {
        // Stable ref. of provided values
        val mLocalProvidedValues = mProvidedValues

        if (mLocalProvidedValues.size == mLastProvidedValues.size) {
            var isChanged = false

            for (i in mLocalProvidedValues.indices) {
                if (mLocalProvidedValues[i] != mLastProvidedValues[i]) {
                    isChanged = true
                    break
                }
            }
            if (isChanged) {
                // Update last values
                for (j in mLastProvidedValues.indices) {
                    mLastProvidedValues[j] = mProvidedValues[j]
                }
            } else {
                // Cancel update
                return
            }
        } else {
            mLastProvidedValues = mLocalProvidedValues.clone()
        }

        // Justify values
        for (i in mLocalProvidedValues.indices) {
            if (mLocalProvidedValues[i] < 0) {
                mLocalProvidedValues[i] = -mLocalProvidedValues[i]
            }
        }

        when {
            mLocalProvidedValues.size == m -> {
                for (i in mLocalProvidedValues.indices) mValues[i] = mLocalProvidedValues[i]
            }
            mValues.size > m -> {
                val groupSize = mLocalProvidedValues.size / m

                for (i in mLocalProvidedValues.indices) {
                    val start = i * groupSize
                    val end = if (i == m - 1) {
                        mLocalProvidedValues.size - 1
                    } else {
                        start + groupSize - 1
                    }

                    var avg = 0F
                    for (j in start..end) avg += mLocalProvidedValues[j]
                    avg /= groupSize

                    mValues[i] = avg
                }
            }
            else -> { // m > size(values)
                val groupSize = m / mValues.size

                // Shifting all the values to right by one position.
                for (i in (mValues.size - 1) downTo 1) {
                    mValues[i] = mValues[i - 1]
                }

                // Updating values at propagation start indices
                for (i in mLocalProvidedValues.indices) {
                    mValues[i * groupSize] = mLocalProvidedValues[i]
                }
            }
        }
    }

    fun start(minValue: Number, maxValue: Number) {
        mSetMinValue = minValue.toFloat()
        mSetMaxValue = maxValue.toFloat()

        mMinMaxValueDiff = mSetMaxValue - mSetMinValue

        barHeightChangeVelocity = mMinMaxValueDiff * CHANGE_VELOCITY

        mCoroutineScope.launch {
            mRenderJob?.cancelAndJoin()
            dispatchRenderJob()
        }
    }

    private fun dispatchRenderJob() {
        mRenderJob = mCoroutineScope.launch {
            while (isActive) {
                // If we can obtain a valid drawing surface...
                if (holder.surface.isValid) {
                    // Generate values using latest provided values
                    generateValues()

                    val canvas = holder.lockCanvas()
                    canvas.drawColor(0, PorterDuff.Mode.CLEAR)

                    // Updating values gradually to latest values.
                    for (i in mValues.indices) {
                        when {
                            (mValues[i] > currentValues[i]) -> {
                                val updatedValue = currentValues[i] + barHeightChangeVelocity

                                currentValues[i] = if (updatedValue > mValues[i]) {
                                    mValues[i]
                                } else {
                                    updatedValue
                                }
                            }
                            (mValues[i] < currentValues[i]) -> {
                                val updatedValue = currentValues[i] - barHeightChangeVelocity

                                currentValues[i] = if (updatedValue < mValues[i]) {
                                    mValues[i]
                                } else {
                                    updatedValue
                                }
                            }
                            else -> {
                                mValues[i] = 0F
                            }
                        }
                    }

//                    val bitmap = // (older impl)
//                        Bitmap.createBitmap(canvas.width / 2, midH.toInt(), Bitmap.Config.ARGB_8888)
                    val bitmap =
                        Bitmap.createBitmap(
                            mSurfaceWidth.toInt(),
                            (mSurfaceHeight / 2).toInt(),
                            Bitmap.Config.ARGB_8888
                        )
                    val bitmapCanvas = Canvas(bitmap)

//                    val maxBarWidth = canvas.width.toFloat() / (currentValues.size * 2 - 1) (older impl)

                    for (i in currentValues.indices) {
                        // Draw each or the bars on the top left portion here.
                        val value = currentValues[valueIndices[i]]

                        // Height of current bar
                        var h =
                            maxBarHeight * (value / mMinMaxValueDiff) // maxBarHeight * (value ranging from 0.0F to 1.0F)

                        if (h > maxBarHeight) h = maxBarHeight

                        // Cap/semi-circle of/on the top of bar.
                        val x = maxBarWidth * i + lrBarMargin

//                        bitmapCanvas.drawArc( (older impl)
//                            x,
//                            midH - h - barWidth / 2,
//                            x + barWidth,
//                            midH - h + barWidth / 2,
//                            -180F,
//                            180F,
//                            true,
//                            fillPaint
//                        )
                        bitmapCanvas.drawCircle(
                            x + barWidth / 2,
                            midH - h,
                            barWidth / 2,
                            fillPaint
                        )
                        bitmapCanvas.drawRect(x, midH - h, x + barWidth, midH, fillPaint)
                    }

                    // Top
                    canvas.drawBitmap(bitmap, 0F, 0F, null)

                    // Bottom
                    val flipMatrix = Matrix()

                    flipMatrix.postScale(
                        1F,
                        -1F,
                        bitmap.width.toFloat() / 2,
                        bitmap.height.toFloat() / 2
                    )

                    val flippedVertically =
                        Bitmap.createBitmap(
                            bitmap,
                            0,
                            0,
                            bitmap.width,
                            bitmap.height,
                            flipMatrix,
                            true
                        )

                    canvas.drawBitmap(
                        flippedVertically,
                        0F,
                        bitmap.height.toFloat(),
                        null
                    )

//                    // Top right (older impl)
//                    val flipMatrix = Matrix().apply {
//                        postScale(
//                            -1F,
//                            1F,
//                            bitmap.width.toFloat() / 2,
//                            bitmap.height.toFloat() / 2
//                        )
//                    }
//
//                    val flippedHorizontally =
//                        Bitmap.createBitmap(
//                            bitmap,
//                            0,
//                            0,
//                            bitmap.width,
//                            bitmap.height,
//                            flipMatrix,
//                            true
//                        )
//
//                    canvas.drawBitmap(flippedHorizontally, bitmap.width.toFloat(), 0F, null)
//
//                    // Bottom left
//                    flipMatrix.postScale(
//                        1F,
//                        -1F,
//                        bitmap.width.toFloat() / 2,
//                        bitmap.height.toFloat() / 2
//                    )
//
//                    val flippedVertically =
//                        Bitmap.createBitmap(
//                            flippedHorizontally,
//                            0,
//                            0,
//                            bitmap.width,
//                            bitmap.height,
//                            flipMatrix,
//                            true
//                        )
//
//                    canvas.drawBitmap(
//                        flippedVertically,
//                        0F,
//                        bitmap.height.toFloat(),
//                        null
//                    )
//
//                    // Bottom right
//                    flipMatrix.postScale(
//                        1F,
//                        -1F,
//                        bitmap.width.toFloat() / 2,
//                        bitmap.height.toFloat() / 2
//                    )
//
//                    val flippedHorizontally2 =
//                        Bitmap.createBitmap(
//                            flippedVertically,
//                            0,
//                            0,
//                            bitmap.width,
//                            bitmap.height,
//                            flipMatrix,
//                            true
//                        )
//
//                    canvas.drawBitmap(
//                        flippedHorizontally2,
//                        bitmap.width.toFloat(),
//                        bitmap.height.toFloat(),
//                        null
//                    )

                    holder.unlockCanvasAndPost(canvas)

                    delay(15L)
                }
            }
        }
    }

    fun pause() {
        mRenderJob?.cancel()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mSurfaceHeight = h.toFloat()
        mSurfaceWidth = w.toFloat()

        maxBarWidth = mSurfaceWidth / mValues.size
        barWidth = maxBarWidth * BAR_WIDTH
        lrBarMargin = maxBarWidth * (1 - BAR_WIDTH)

        verticalMargin =
            mSurfaceHeight * 0.15F // Ranges from 0.0F (0% of max height) to 1.0F (100% of max height).

        midH = mSurfaceHeight / 2
        maxBarHeight = midH - verticalMargin
    }
}