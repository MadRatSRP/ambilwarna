package yuku.ambilwarna.demo

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import yuku.ambilwarna.FGHColorPickerDialog
import yuku.ambilwarna.FGHColorPickerDialog.OnnDialogButtonClickedListener
import yuku.ambilwarna.demo.databinding.ActivityMainBinding

class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding
    private var currentColor: Long = 0xffff0000
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        
        displayColor()
        
        binding.showDialogButton.setOnClickListener {
            openDialog()
        }
    }
    
    private fun openDialog() {
        val dialog = FGHColorPickerDialog(
            this,
            currentColor.toInt(),
            object : OnnDialogButtonClickedListener {
                override fun onOk(
                    dialog: FGHColorPickerDialog?,
                    color: Int
                ) {
                    Toast.makeText(applicationContext, "ok", Toast.LENGTH_SHORT).show()
                    this@MainActivity.currentColor = color.toLong()
                    displayColor()
                }
                
                override fun onCancel(dialog: FGHColorPickerDialog?) {
                    Toast.makeText(
                        applicationContext,
                        "cancel",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        dialog.show()
    }
    
    fun displayColor() {
        binding.currentColorHex.text = String.format(
            "Current color: 0x%08x",
            currentColor
        )
    }
}