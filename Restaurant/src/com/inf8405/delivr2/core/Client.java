package com.inf8405.delivr2.core;

import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.Marker;

/**
 * Cette classe represente un client
 * 
 * @author Dany
 */
public class Client {
	// L'identifiant du marqueur
	public int marker_id;
	// Le nom du client
	public String name;
	// Le numero de telephonne
	public String phone;
	// Le niveau de batterie
	public double battery;
	// Son longitude
	public double longitude;
	// Son latitude
	public double latitude;
	// Sa commande
	public String order;
	// Son image
	public Drawable image;
	// Son marker
	public Marker marker;
	// Variable utilisee pour le garbage collection
	public boolean gc = true;
	// Un client vide (utilise pour le garbage collection)
	public static Client GC = new Client();
}
