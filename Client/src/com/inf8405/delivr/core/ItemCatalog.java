package com.inf8405.delivr.core;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

/**
 * Cette classe represente la liste des items servis par le restaurant mobile
 * 
 * @author Dany
 */
public class ItemCatalog {
	// L'instance unique de la classe
	private static ItemCatalog instance = null;
	// La liste des items (pour le caching)
	private ArrayList<Item> items;

	/**
	 * Constructeur
	 */
	private ItemCatalog() {
	}

	/**
	 * Cette methode permet d'obtenir l'unique instance
	 * 
	 * @return l'instance
	 */
	public static ItemCatalog getInstance() {
		if (instance == null) {
			instance = new ItemCatalog();
		}
		return instance;
	}

	/**
	 * Cette methode permet d'obtenir la liste des items
	 * 
	 * @return
	 */
	public ArrayList<Item> getItems(Activity activity) {
		if (this.items != null)
			return this.items;

		ArrayList<Item> items = new ArrayList<Item>();

		try {
			JSONArray response = Server.getItemsList(activity);

			if (response == null) {
				return items;
			}

			for (int i = 0; i < response.length(); ++i) {
				JSONObject item = response.getJSONObject(i);
				items.add(new Item(item.getString("name"), item.getDouble("price"), item.getString("img")));
			}
		}
		catch (JSONException e) {
			return items;
		}

		this.items = items;
		return items;
	}
}
