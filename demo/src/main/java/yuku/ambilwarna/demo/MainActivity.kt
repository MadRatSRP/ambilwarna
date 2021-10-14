package yuku.ambilwarna.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import yuku.ambilwarna.FGHColorPickerDialog;

public class AmbilWarnaDemoActivity extends Activity {
	TextView text1;
	int color = 0xffffff00;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final View button1 = findViewById(R.id.button1);
		final View button2 = findViewById(R.id.button2);
		final View button3 = findViewById(R.id.button3);
		text1 = (TextView) findViewById(R.id.text1);
		displayColor();

		button1.setOnClickListener(view -> openDialog(false));

		button2.setOnClickListener(view -> openDialog(true));

		button3.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), AmbilWarnaDemoPreferenceActivity.class)));
	}

	void openDialog(boolean supportsAlpha) {
		FGHColorPickerDialog dialog = new FGHColorPickerDialog(
			AmbilWarnaDemoActivity.this,
			color,
			new FGHColorPickerDialog.OnnDialogButtonClickedListener() {
			@Override
			public void onOk(FGHColorPickerDialog dialog, int color) {
				Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
				AmbilWarnaDemoActivity.this.color = color;
				displayColor();
			}

			@Override
			public void onCancel(FGHColorPickerDialog dialog) {
				Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();
			}
		});
		dialog.show();
	}

	void displayColor() {
		text1.setText(String.format("Current color: 0x%08x", color));
	}
}