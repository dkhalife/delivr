package com.inf8405.delivr.core.sensors;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * Cette classe represente le capteur de la batterie
 * 
 * @author Dany
 */
public class Battery {
	// Le intent qui nous permet d'obtenir les infos sur la batterie
	Activity activity;

	/**
	 * Constructeur par parametres
	 * 
	 * @param context Le contexte de l'application
	 */
	public Battery(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Methode d'acces au pourcentage restant de la batterie
	 * 
	 * @return Le pourcentage de charge de la batterie
	 */
	public float getPercentage() {
		Intent i = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		float scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		return level / scale;
	}
}
