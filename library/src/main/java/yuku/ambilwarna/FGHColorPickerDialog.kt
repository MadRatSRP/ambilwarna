package yuku.ambilwarna

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.RelativeLayout
import yuku.ambilwarna.databinding.AmbilwarnaDialogBinding
import kotlin.math.floor

class FGHColorPickerDialog(
    context: Context?,
    currentColor: Int,
    private val listener: OnnDialogButtonClickedListener?
) {
    interface OnnDialogButtonClickedListener {
        fun onCancel(dialog: FGHColorPickerDialog?)
        
        fun onOk(dialog: FGHColorPickerDialog?, color: Int)
    }
    
    private val dialog: AlertDialog
    private val currentColorHSV = FloatArray(3)
    private val binding: AmbilwarnaDialogBinding
    
    private val color: Int
        get() {
            val argb = Color.HSVToColor(currentColorHSV)
            return alpha shl 24 or (argb and 0x00ffffff)
        }
    private var colorHue: Float
        get() = currentColorHSV[0]
        private set(hue) {
            currentColorHSV[0] = hue
        }
    private var colorSaturation: Float
        get() = currentColorHSV[1]
        private set(saturation) {
            currentColorHSV[1] = saturation
        }
    private var colorValue: Float
        get() = currentColorHSV[2]
        private set(value) {
            currentColorHSV[2] = value
        }
    private var alpha: Int
    
    init {
        Color.colorToHSV(
            currentColor,
            currentColorHSV
        )
        alpha = Color.alpha(currentColor)
        binding = AmbilwarnaDialogBinding.inflate(
            LayoutInflater.from(context)
        )
        with(binding) {
            saturationAndValueSelectionView.setHue(colorHue)
            viewOldColor.setBackgroundColor(currentColor)
            viewNewColor.setBackgroundColor(currentColor)
            saturationAndValueSelectionView.setOnTouchListener { view: View?, motionEvent: MotionEvent ->
                onSaturationAndValueSelectionViewTouched(
                    view,
                    motionEvent
                )
            }
            hueSelectionView.setOnTouchListener { view: View?, motionEvent: MotionEvent ->
                onHueSelectionViewTouched(
                    view,
                    motionEvent
                )
            }
            // if back button is used, call back our listener.
            dialog = AlertDialog.Builder(context).apply {
                setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    if (this@FGHColorPickerDialog.listener != null) {
                        this@FGHColorPickerDialog.listener.onOk(
                            this@FGHColorPickerDialog,
                            color
                        )
                    }
                }
                setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                    if (this@FGHColorPickerDialog.listener != null) {
                        this@FGHColorPickerDialog.listener.onCancel(this@FGHColorPickerDialog)
                    }
                }
                setOnCancelListener {
                    if (this@FGHColorPickerDialog.listener != null) {
                        this@FGHColorPickerDialog.listener.onCancel(this@FGHColorPickerDialog)
                    }
                }
            }.create()
            // kill all padding from the dialog window
            dialog.setView(
                binding.root,
                0,
                0,
                0,
                0
            )
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
    
    private fun onSaturationAndValueSelectionViewTouched(
        view: View?,
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
                viewNewColor.setBackgroundColor(color)
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
            layoutParams.leftMargin = (saturationAndValueSelectionView.left + x - floor((saturationAndValueSelectorView.measuredWidth / 2).toDouble())
                - viewContainer.paddingLeft).toInt()
            layoutParams.topMargin = (saturationAndValueSelectionView.top + y - floor((saturationAndValueSelectorView.measuredHeight / 2).toDouble())
                - viewContainer.paddingTop).toInt()
            saturationAndValueSelectorView.layoutParams = layoutParams
        }
    }
    
    private fun onHueSelectionViewTouched(
        view: View?,
        motionEvent: MotionEvent
    ): Boolean {
        with(binding) {
            if (motionEvent.action == MotionEvent.ACTION_MOVE ||
                motionEvent.action == MotionEvent.ACTION_DOWN ||
                motionEvent.action == MotionEvent.ACTION_UP
            ) {
                var y = motionEvent.y
                if (y < 0f) {
                    y = 0f
                }
                if (y > hueSelectionView.measuredHeight) {
                    y = hueSelectionView.measuredHeight - 0.001f // to avoid jumping the cursor from bottom to top.
                }
                var hue = 360f - 360f / hueSelectionView.measuredHeight * y
                if (hue == 360f) {
                    hue = 0f
                }
                colorHue = hue
                // update view
                saturationAndValueSelectionView.setHue(hue)
                moveHueSelector()
                viewNewColor.setBackgroundColor(color)
                return true
            }
            return false
        }
    }
    
    private fun moveHueSelector() {
        with(binding) {
            var y = hueSelectionView.measuredHeight - colorHue * hueSelectionView.measuredHeight / 360f
            if (y == hueSelectionView.measuredHeight.toFloat()) {
                y = 0f
            }
            val layoutParams = hueSelectorView.layoutParams as RelativeLayout.LayoutParams
            layoutParams.leftMargin = (hueSelectionView.left - floor((hueSelectorView.measuredWidth / 2).toDouble())
                    - viewContainer.paddingLeft).toInt()
            layoutParams.topMargin = (hueSelectionView.top + y - floor((hueSelectorView.measuredHeight / 2).toDouble())
                - viewContainer.paddingTop).toInt()
            hueSelectorView.layoutParams = layoutParams
        }
    }
    
    fun show() {
        dialog.show()
    }
}