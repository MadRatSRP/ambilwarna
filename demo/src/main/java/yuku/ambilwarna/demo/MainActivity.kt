package yuku.ambilwarna.demo

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import yuku.ambilwarna.FGHColorPickerDialog
import yuku.ambilwarna.demo.databinding.ActivityMainBinding

class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding
    private var currentColor: Long = 0xffff0000
    private var dialog: AlertDialog? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        
        val dialogView = FGHColorPickerDialog(
            this@MainActivity,
            currentColor.toInt()
        )
        
        val dialogBuilder = AlertDialog.Builder(this).apply {
            setView(dialogView)
            setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                handleOkDialogButtonClick(
                    dialogView.color
                )
            }
            setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                handleCancelDialogButtonClick()
            }
        }
        
        dialog = dialogBuilder.create()
        
        displayColor()
        
        binding.showDialogButton.setOnClickListener {
            dialog?.show()
        }
    }
    
    private fun handleOkDialogButtonClick(
        selectedColor: Int
    ) {
        Toast.makeText(
            this,
            "ok",
            Toast.LENGTH_SHORT
        ).show()
        this.currentColor = selectedColor.toLong()
        displayColor()
        dialog?.dismiss()
    }
    
    private fun handleCancelDialogButtonClick() {
        Toast.makeText(
            applicationContext,
            "cancel",
            Toast.LENGTH_SHORT
        ).show()
        dialog?.dismiss()
    }
    
    private fun displayColor() {
        val stringHexColor = String.format(
            "%06X", 0xFFFFFF and currentColor.toInt()
        )
        binding.currentColorHex.text = String.format(
            "Current color: #%s",
            stringHexColor
        )
    }
}