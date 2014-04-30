package com.inf8405.delivr.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Cette classe represente une commande
 * 
 * @author Dany
 */
public class Order {
	// L'instance unique
	private static Order instance;
	// Les coordonnees de position
	private LatLng location;
	// L'identifiant de la commande
	private Integer order_id;

	// La liste des items
	private ArrayList<Item> items = new ArrayList<Item>();

	/**
	 * Constructeur prive
	 */
	private Order() {
	}

	/**
	 * Methode d'acces a l'instance unique
	 * 
	 * @return L'instance unique
	 */
	public static Order getInstance() {
		if (instance == null) {
			instance = new Order();
		}

		return instance;
	}

	/**
	 * Methode qui permet de charger l'instance unique
	 * 
	 * @param o L'instance unique
	 */
	public static void setInstance(Order o) {
		instance = o;
	}

	/**
	 * Methode d'acces a la liste des items
	 * 
	 * @return La liste des items
	 */
	public ArrayList<Item> getItems() {
		return items;
	}

	/**
	 * Methode de modification des items
	 * 
	 * @param items Les items a mettre
	 */
	public void setItems(ArrayList<Item> items) {
		this.items = items;
	}

	/**
	 * Methode d'ajout d'un item
	 * 
	 * @param i L'item a ajouter
	 */
	public void addItem(Item i) {
		items.add(i);
	}

	/**
	 * Methode de suppression d'un item
	 * 
	 * @param i L'item a supprimer
	 */
	public void remove(int i) {
		items.remove(i);
	}

	/**
	 * Methode de calcul du total
	 * 
	 * @return Le total de la commande
	 */
	public double total() {
		double total = 0;

		for (Item i : items) {
			total += i.getPrice();
		}

		return total;
	}

	/**
	 * Methode de modification de l'adresse de livraison
	 * 
	 * @param location La position sur la carte
	 */
	public void setLocation(LatLng location) {
		this.location = location;
	}

	/**
	 * Methode d'acces a la position de la commande
	 * 
	 * @return La position de la commande
	 */
	public LatLng getLocation() {
		return location;
	}

	/**
	 * Cette methode permet de savoir s'il y a une commande deja
	 * 
	 * @return True s'il y a une commande
	 */
	public boolean isSet() {
		return order_id != null;
	}

	/**
	 * Methode d'acces a l'identifiant de la commande
	 * 
	 * @return L'identifiant de la commande
	 */
	public Integer getOrderId() {
		return order_id;
	}

	/**
	 * Methode qui permet de vider la commande
	 */
	public void clear(Activity activity) {
		order_id = null;
		items.clear();
		LocalStorage.writeObjectToFile(activity, this, "order.dat");
	}

	/**
	 * Cette methode permet d'acheminer la commande
	 * 
	 * @param activity L'activite
	 * @param name Le nom du client
	 * @param phone Le numero de telephone du client
	 * @param battery Le niveau de batterie de l'appareil
	 * @param photo La photo prise
	 */
	public void process(final Activity activity, String name, String phone, float battery, final Bitmap photo) {
		final ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("phone", phone));
		params.add(new BasicNameValuePair("battery", battery + ""));
		params.add(new BasicNameValuePair("longitude", location.longitude + ""));
		params.add(new BasicNameValuePair("latitude", location.latitude + ""));

		double total = 0;
		StringBuilder order = new StringBuilder();
		for (Item i : items) {
			order.append(i.getName() + "\n");
			total += i.getPrice();
		}
		order.append("Total: " + total);
		params.add(new BasicNameValuePair("order", order.toString()));

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					File file = new File(activity.getCacheDir(), "photo.png");
					file.createNewFile();
					FileOutputStream fos = new FileOutputStream(file);
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					photo.compress(CompressFormat.PNG, 0, os);
					fos.write(os.toByteArray());
					fos.flush();
					fos.close();

					order_id = Server.placeOrder(activity, params, file);

					if (order_id != -1) {
						LocalStorage.writeObjectToFile(activity, this, "order.dat");
						activity.finish();
					}
				}
				catch (IOException e) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(activity, "Erreur durant le traitement de l'image", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
	}
}
