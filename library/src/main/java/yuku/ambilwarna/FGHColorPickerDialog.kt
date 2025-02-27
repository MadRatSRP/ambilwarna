package yuku.ambilwarna

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import yuku.ambilwarna.databinding.AmbilwarnaDialogBinding
import kotlin.math.floor

class FGHColorPickerDialog @JvmOverloads constructor(
    context: Context?,
    currentColor: Int,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): LinearLayout(
    context,
    attrs,
    defStyle
) {
    private val binding = AmbilwarnaDialogBinding.inflate(
        LayoutInflater.from(context)
    )
    private val hueColorHSV = FloatArray(3)
    private val saturationAndValueColorHSV = FloatArray(3)
    // Hue - [0], Saturation = [1], Value = [2]
    val color: Int
        get() {
            val argb = Color.HSVToColor(saturationAndValueColorHSV)
            return alpha shl 24 or (argb and 0x00ffffff)
        }
    private var colorHue: Float
        get() = saturationAndValueColorHSV[0]
        private set(hue) {
            hueColorHSV[0] = hue
            saturationAndValueColorHSV[0] = hue
        }
    private var colorSaturation: Float
        get() = saturationAndValueColorHSV[1]
        private set(saturation) {
            saturationAndValueColorHSV[1] = saturation
        }
    private var colorValue: Float
        get() = saturationAndValueColorHSV[2]
        private set(value) {
            saturationAndValueColorHSV[2] = value
        }
    private var alpha: Int
    
    init {
        addView(binding.root)
        arrayOf(
            saturationAndValueColorHSV,
            hueColorHSV
        ).forEach {
            Color.colorToHSV(
                currentColor,
                it
            )
        }
        alpha = Color.alpha(currentColor)
        with(binding) {
            saturationAndValueSelectionView.setHue(colorHue)
            restoreSavedColors(color)
            saturationAndValueSelectionView.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
                onSaturationAndValueSelectionViewTouched(
                    motionEvent
                )
            }
            hueSelectionView.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
                onHueSelectionViewTouched(
                    motionEvent
                )
            }
            // if back button is used, call back our listener.
            /*dialog = AlertDialog.Builder(context).apply {
                setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    listener?.onOk(
                        this@FGHColorPickerDialog,
                        color
                    )
                }
                setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                    listener?.onCancel(this@FGHColorPickerDialog)
                }
                setOnCancelListener {
                    listener?.onCancel(this@FGHColorPickerDialog)
                }
            }.create()*/
            // kill all padding from the dialog window
            /*dialog.setView(
                binding.root,
                0,
                0,
                0,
                0
            )*/
            // move cursor & target on first draw
            binding.root.viewTreeObserver.apply {
                addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        moveHueSelector()
                        moveSaturationAndValueSelector()
                        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }
    }
    
    private fun updateCurrentHexColor(color: Int) {
        val stringHexColor = String.format("#%06X", 0xFFFFFF and color)
        binding.currentColorHex.text = stringHexColor
    }
    
    private fun restoreSavedColors(color: Int) {
        with(binding) {
            updateCurrentHexColor(color)
            currentColorView.setBackgroundColor(color)
            saturationAndValueSelectorView.updateSelectorColor(color)
            hueSelectorView.updateSelectorColor(color)
        }
    }
    
    private fun updateSaturationAndValueColor(color: Int) {
        with(binding) {
            updateCurrentHexColor(color)
            currentColorView.setBackgroundColor(color)
            saturationAndValueSelectorView.updateSelectorColor(color)
        }
    }
    
    private fun updateHueColor(color: Int) {
        with(binding) {
            updateCurrentHexColor(color)
            currentColorView.setBackgroundColor(color)
            saturationAndValueSelectorView.updateSelectorColor(color)
            hueSelectorView.updateSelectorColor(color)
        }
    }
    
    private fun onSaturationAndValueSelectionViewTouched(
        motionEvent: MotionEvent
    ): Boolean {
        with(binding) {
            if (motionEvent.action == MotionEvent.ACTION_MOVE ||
                motionEvent.action == MotionEvent.ACTION_DOWN ||
                motionEvent.action == MotionEvent.ACTION_UP) {
                // touch event are in dp units.
                var x = motionEvent.x
                var y = motionEvent.y
                if (x < 0f) {
                    x = 0f
                }
                if (x > saturationAndValueSelectionView.measuredWidth) {
                    x = saturationAndValueSelectionView.measuredWidth.toFloat()
                }
                if (y < 0f) {
                    y = 0f
                }
                if (y > saturationAndValueSelectionView.measuredHeight) {
                    y = saturationAndValueSelectionView.measuredHeight.toFloat()
                }
                colorSaturation = 1f / saturationAndValueSelectionView.measuredWidth * x
                colorValue = 1f - 1f / saturationAndValueSelectionView.measuredHeight * y
                // update view
                moveSaturationAndValueSelector()
                updateSaturationAndValueColor(color)
                return true
            }
            return false
        }
    }
    
    private fun moveSaturationAndValueSelector() {
        with(binding) {
            val x = colorSaturation * saturationAndValueSelectionView.measuredWidth
            val y = (1f - colorValue) * saturationAndValueSelectionView.measuredHeight
            val layoutParams = saturationAndValueSelectorView.layoutParams as RelativeLayout.LayoutParams
            layoutParams.leftMargin = (saturationAndValueSelectionView.left + x - floor(
                (saturationAndValueSelectorView.measuredWidth / 2).toDouble())
                - viewContainer.paddingLeft).toInt()
            layoutParams.topMargin = (saturationAndValueSelectionView.top + y - floor(
                (saturationAndValueSelectorView.measuredHeight / 2).toDouble())
                - viewContainer.paddingTop).toInt()
            saturationAndValueSelectorView.layoutParams = layoutParams
        }
    }
    
    private fun onHueSelectionViewTouched(
        motionEvent: MotionEvent
    ): Boolean {
        with(binding) {
            if (motionEvent.action == MotionEvent.ACTION_MOVE ||
                motionEvent.action == MotionEvent.ACTION_DOWN ||
                motionEvent.action == MotionEvent.ACTION_UP
            ) {
                var x = motionEvent.x
                if (x < 0f) {
                    x = 0f
                }
                if (x > hueSelectionView.measuredWidth) {
                    // to avoid jumping the cursor from bottom to top.
                    x = hueSelectionView.measuredWidth - 0.001f
                }
                var hue = 360f - 360f / hueSelectionView.measuredWidth * x
                if (hue == 360f) {
                    hue = 0f
                }
                colorHue = hue
                // update view
                saturationAndValueSelectionView.setHue(hue)
                moveHueSelector()
                updateHueColor(color)
                return true
            }
            return false
        }
    }
    
    private fun moveHueSelector() {
        with(binding) {
            var x = hueSelectionView.measuredWidth - colorHue * hueSelectionView.measuredWidth / 360f
            if (x == hueSelectionView.measuredWidth.toFloat()) {
                x = 0f
            }
            val layoutParams = hueSelectorView.layoutParams as RelativeLayout.LayoutParams
            
            // 0.0, где 0 - x, 0 - y
            // y уменьшается, x остаётся на месте
    
            // 0.0, где 0 - x, 0 - y
            // y остаётся на месте, x увеличивается
            
            layoutParams.topMargin = ((hueSelectionView.top - floor(
                (hueSelectorView.measuredHeight / 2).toDouble())
                - viewContainer.paddingTop).toInt()) + 30
            layoutParams.leftMargin = (hueSelectionView.left + x - floor(
                (hueSelectorView.measuredWidth / 2).toDouble())
                - viewContainer.paddingLeft).toInt()
            hueSelectorView.layoutParams = layoutParams
        }
    }
    
    private fun ImageView.updateSelectorColor(color: Int) {
        drawable.mutate()
        drawable.colorFilter = PorterDuffColorFilter(
            color, PorterDuff.Mode.SRC_IN
        )
    }
}