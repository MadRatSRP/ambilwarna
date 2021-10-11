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
    
    protected fun moveCursor() {
        with(binding) {
            var y = viewHue.measuredHeight - colorHue * viewHue.measuredHeight / 360f
            if (y == viewHue.measuredHeight.toFloat()) {
                y = 0f
            }
            val layoutParams = viewCursor.layoutParams as RelativeLayout.LayoutParams
            layoutParams.leftMargin = (viewHue.left - floor((viewCursor.measuredWidth / 2).toDouble())
                    - viewContainer.paddingLeft).toInt()
            layoutParams.topMargin = (viewHue.top + y - floor((viewCursor.measuredHeight / 2).toDouble())
                - viewContainer.paddingTop).toInt()
            viewCursor.layoutParams = layoutParams
        }
    }
    
    protected fun moveTarget() {
        with(binding) {
            val x = colorSaturation * viewSatVal.measuredWidth
            val y = (1f - colorValue) * viewSatVal.measuredHeight
            val layoutParams = viewTarget.layoutParams as RelativeLayout.LayoutParams
            layoutParams.leftMargin = (viewSatVal.left + x - floor((viewTarget.measuredWidth / 2).toDouble())
                - viewContainer.paddingLeft).toInt()
            layoutParams.topMargin = (viewSatVal.top + y - floor((viewTarget.measuredHeight / 2).toDouble())
                - viewContainer.paddingTop).toInt()
            viewTarget.layoutParams = layoutParams
        }
    }
    
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
    private fun receiveAlpha(): Float {
        return alpha.toFloat()
    }
    private fun updateAlpha(alpha: Int) {
        this.alpha = alpha
    }
    
    fun show() {
        dialog.show()
    }
    
    init {
        Color.colorToHSV(currentColor, currentColorHSV)
        alpha = Color.alpha(currentColor)
        binding = AmbilwarnaDialogBinding.inflate(
            LayoutInflater.from(context)
        )
        with(binding) {
            viewSatVal.setHue(colorHue)
            viewOldColor.setBackgroundColor(currentColor)
            viewNewColor.setBackgroundColor(currentColor)
            viewHue.setOnTouchListener { view: View?, motionEvent: MotionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_MOVE ||
                    motionEvent.action == MotionEvent.ACTION_DOWN ||
                    motionEvent.action == MotionEvent.ACTION_UP
                ) {
                    var y = motionEvent.y
                    if (y < 0f) {
                        y = 0f
                    }
                    if (y > viewHue.measuredHeight) {
                        y = viewHue.measuredHeight - 0.001f // to avoid jumping the cursor from bottom to top.
                    }
                    var hue = 360f - 360f / viewHue.measuredHeight * y
                    if (hue == 360f) {
                        hue = 0f
                    }
                    colorHue = hue
                    // update view
                    viewSatVal.setHue(hue)
                    moveCursor()
                    viewNewColor.setBackgroundColor(color)
                    return@setOnTouchListener true
                }
                false
            }
            viewSatVal.setOnTouchListener { view: View?, motionEvent: MotionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_MOVE || motionEvent.action == MotionEvent.ACTION_DOWN || motionEvent.action == MotionEvent.ACTION_UP) {
                    // touch event are in dp units.
                    var x = motionEvent.x
                    var y = motionEvent.y
                    if (x < 0f) {
                        x = 0f
                    }
                    if (x > viewSatVal.measuredWidth) {
                        x = viewSatVal.measuredWidth.toFloat()
                    }
                    if (y < 0f) {
                        y = 0f
                    }
                    if (y > viewSatVal.measuredHeight){
                        y = viewSatVal.measuredHeight.toFloat()
                    }
                    colorSaturation = 1f / viewSatVal.measuredWidth * x
                    colorValue = 1f - 1f / viewSatVal.measuredHeight * y
                    // update view
                    moveTarget()
                    viewNewColor.setBackgroundColor(color)
                    return@setOnTouchListener true
                }
                false
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
            dialog.setView(binding.root, 0, 0, 0, 0)
    
            // move cursor & target on first draw
            binding.root.viewTreeObserver.apply {
                addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        moveCursor()
                        moveTarget()
                        binding.root.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                })
            }
        }
    }
}