package com.inf8405.delivr2.core.sensors;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;

/**
 * Cette classe represente l'ensemble des capteurs
 * 
 * @author Dany
 */
public class Sensors {
	// Le capteur GPS
	public Gps gps;
	// Le capteur de localisation
	public Geolocation geolocation;
	// L'objet permettant de recuper l'addresse a partir des coordonnees
	private Geocoder geoCoder;

	/**
	 * Constructeur par parametre
	 * 
	 * @param act L'activite
	 */
	public Sensors(Activity act) {
		gps = new Gps(act);
		geolocation = new Geolocation(act);
		geoCoder = new Geocoder(act);
	}

	/**
	 * Methode qui permet d'obtenir une addresse en fonction des coordonnees
	 * 
	 * @param latitude La latitude
	 * @param longitude La longitude
	 * @return L'addresse correspondante
	 */
	public String getAddressFromLocation(double latitude, double longitude) {
		try {
			List<Address> matches = geoCoder.getFromLocation(latitude, longitude, 1);
			if (matches.isEmpty())
				return "";
			else {
				String address = matches.get(0).getAddressLine(0);
				return address != null ? address : "";
			}
		}
		catch (IOException e) {
			return "";
		}
	}
}
