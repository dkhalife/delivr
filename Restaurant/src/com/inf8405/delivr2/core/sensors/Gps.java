package com.inf8405.delivr2.core.sensors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;

/**
 * Cette classe represente le capteur de GPS
 * 
 * @author Dany
 */
public class Gps {
	// Le gestionnaire de localisation a partir duquel on obtient les
	// coordonnees
	private LocationManager lm;

	/**
	 * Constructeur par parametres
	 * 
	 * @param act L'activite
	 */
	public Gps(Activity act) {
		lm = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * Methode d'acces a la position GPS
	 * 
	 * @return La position GPS
	 */
	public void getLocation(Activity activity) {
		// Verifier si le GPS est allume
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			activity.startActivity(intent);
			return;
		}

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, (LocationListener) activity);
	}

	/**
	 * Cette methode permet de se desabonner des mises a jour
	 * 
	 * @param activity
	 */
	public void stop(LocationListener activity) {
		lm.removeUpdates(activity);
	}
}
