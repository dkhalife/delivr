package com.inf8405.delivr.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.inf8405.delivr.R;

/**
 * Cette classe represente la page a propos
 * 
 * @author Dany
 */
public class About extends Activity implements View.OnClickListener {
	/**
	 * Methode qui fait l'initialisation
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		Button back = (Button) findViewById(R.id.aboutBack);
		back.setOnClickListener(this);
	}

	/**
	 * Methode qui gere les evenements de click
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.aboutBack:
				finish();
			break;
		}
	}
}
