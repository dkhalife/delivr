package com.inf8405.delivr2.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.inf8405.delivr2.R;

/**
 * Cette classe represente le menu principal
 * 
 * @author Dany
 */
public class MainMenu extends Activity implements View.OnClickListener {
	/**
	 * Cette methode initialise le tout
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);

		Button showMap = (Button) findViewById(R.id.showMap);
		Button quit = (Button) findViewById(R.id.quit);

		showMap.setOnClickListener(this);
		quit.setOnClickListener(this);
	}

	/**
	 * Cette methode recupere les evenements de click
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.showMap:
				startActivity(new Intent("com.inf8405.delivr2.activities.MAP"));
			break;

			case R.id.quit:
				finish();
			break;
		}
	}
}
