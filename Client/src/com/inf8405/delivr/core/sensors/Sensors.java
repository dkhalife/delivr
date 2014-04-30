package com.inf8405.delivr.core.sensors;

import java.io.IOException;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.telephony.TelephonyManager;

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
	// Le capteur de la batterie
	public Battery battery;
	// L'objet permettant de recuper l'addresse a partir des coordonnees
	private Geocoder geoCoder;
	// L'accelerometre
	public Shaker shaker;
	// Le tilter
	public Tilter tilter;
	// L'activite
	private Activity activity;

	/**
	 * Constructeur par parametre
	 * 
	 * @param act L'activite
	 */
	public Sensors(Activity act) {
		activity = act;
		gps = new Gps(act);
		geolocation = new Geolocation(act);
		battery = new Battery(act);
		geoCoder = new Geocoder(act);
		shaker = new Shaker(act);
		tilter = new Tilter(act);
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

	/**
	 * Cette methode permet d'obtenir le nom d'utilisateur sur le telephonne
	 * 
	 * @param context Le contexte
	 * @return Le nom d'usager du client
	 */
	public String getName() {
		AccountManager accountManager = AccountManager.get(activity);
		Account[] accounts = accountManager.getAccountsByType("com.google");

		return accounts.length > 0 ? accounts[0].name : "Anonyme";
	}

	/**
	 * Cette methode permet d'obtenir le numero du telephone
	 * 
	 * @param context Le contexte
	 * @return Le numero du telephone
	 */
	public String getPhoneNumber(Context context) {
		return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
	}
}
