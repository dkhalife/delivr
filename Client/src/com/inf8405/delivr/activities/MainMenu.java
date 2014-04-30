package com.inf8405.delivr.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.inf8405.delivr.R;
import com.inf8405.delivr.core.LocalStorage;
import com.inf8405.delivr.core.Order;

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
		Button about = (Button) findViewById(R.id.about);
		Button quit = (Button) findViewById(R.id.quit);

		showMap.setOnClickListener(this);
		about.setOnClickListener(this);
		quit.setOnClickListener(this);

		Order o = (Order) LocalStorage.readObjectFromFile(this, "order.dat");
		if (o != null) {
			Order.setInstance(o);
		}
	}

	/**
	 * Cette methode recupere les evenements de click
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.showMap:
				startActivity(new Intent("com.inf8405.delivr.activities.MAP"));
			break;

			case R.id.about:
				startActivity(new Intent("com.inf8405.delivr.activities.ABOUT"));
			break;

			case R.id.quit:
				finish();
			break;
		}
	}
}
