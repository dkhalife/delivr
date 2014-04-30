package com.inf8405.delivr.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inf8405.delivr.R;

/**
 * Cette classe permet de construire un item dans une liste
 * 
 * @author Dany
 */
public class ItemAdapter extends BaseAdapter {
	// L'activite qui a cree cette classe
	private Activity activity;

	// La liste des données
	private ArrayList<HashMap<String, Object>> data;

	// L'objet qui instancie les views
	private static LayoutInflater inflater = null;

	// La cache pour les images
	private static HashMap<String, Drawable> cache = new HashMap<String, Drawable>();

	/**
	 * Constructeur par parametres
	 * 
	 * @param activity L'activite
	 * @param data Les donnees
	 */
	public ItemAdapter(Activity activity, ArrayList<HashMap<String, Object>> data) {
		this.activity = activity;
		this.data = data;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Methode d'acces au nombre total d'items
	 */
	@Override
	public int getCount() {
		return data.size();
	}

	/**
	 * Methode d'acces aux donnees d'un item en particulier
	 */
	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	/**
	 * Methode d'acces a l'identifiant d'un item en particulier
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Methode qui permet d'obtenir un item de la liste
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final HashMap<String, Object> item = data.get(position);
		View v = convertView != null ? convertView : inflater.inflate(R.layout.item, null);

		TextView name = (TextView) v.findViewById(R.id.name);
		TextView price = (TextView) v.findViewById(R.id.price);
		final ImageView img = (ImageView) v.findViewById(R.id.img);

		name.setText(item.get("name").toString());
		price.setText(Double.parseDouble(item.get("price").toString()) + "$");

		new Thread(new Runnable() {
			public void run() {
				final Drawable d;

				try {
					d = getDrawableFromUrl(item.get("img").toString());

					activity.runOnUiThread(new Runnable() {
						public void run() {
							img.setBackground(d);
						}
					});
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
		return v;
	}

	/**
	 * Cette methode permet d'avoir une image a partir d'un URL
	 * 
	 * @param url Le URL
	 * @return Un objet Drawable pour l'image sous format Android
	 * @throws java.net.MalformedURLException
	 * @throws java.io.IOException
	 */
	private static Drawable getDrawableFromUrl(String url) throws java.net.MalformedURLException, java.io.IOException {
		if (cache.containsKey(url))
			return cache.get(url);

		Drawable img = Drawable.createFromStream(((java.io.InputStream) new java.net.URL(url).getContent()), null);
		cache.put(url, img);

		return img;
	}
}