package com.inf8405.delivr2.core.sensors;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

/**
 * Cette classe represente les capteur de position selon le reseau
 * 
 * @author Dany
 */
public class Geolocation {
	// Le gestionnaire de localisation a partir duquel on obtient les
	// coordonnees
	private LocationManager lm;

	/**
	 * Constructeur par parametres
	 * 
	 * @param act L'activite
	 */
	public Geolocation(Activity act) {
		lm = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * Methode d'acces a la position selon les capteurs WiFi/GSM/3G/LTE..
	 * 
	 * @return La position
	 */
	public void getLocation(LocationListener activity) {
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, activity);
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
