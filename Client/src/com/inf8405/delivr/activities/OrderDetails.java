package com.inf8405.delivr.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inf8405.delivr.R;
import com.inf8405.delivr.core.Item;
import com.inf8405.delivr.core.ItemAdapter;
import com.inf8405.delivr.core.ItemCatalog;
import com.inf8405.delivr.core.Order;
import com.inf8405.delivr.core.sensors.Sensors;

/**
 * Cette classe represente les details d'une commande
 * 
 * @author Dany
 */
public class OrderDetails extends Activity implements OnItemClickListener, View.OnClickListener {
	// La liste des items disponibles
	private ListView catalog;
	// Les items sur la commande
	private ListView order;
	// La liste des items commandes
	private ArrayList<HashMap<String, Object>> orderedItems = new ArrayList<HashMap<String, Object>>();
	// L'objet responsable d'afficher la liste des items commandes
	private ItemAdapter orderedItemsAdapter;
	// Le total de la transaction
	private double total = 0;
	// Le text affichant le total de la transaction
	private TextView orderTotal;
	// Le bouton d'acheminement de la commande
	private Button finishOrder;
	// Le bouton d'annulation de la commande
	private Button cancelOrder;
	// Constante permettant d'identifier la requete de capture d'image
	private final int CAMERA_REQUEST = 1;

	/**
	 * Methode d'initialisation
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_details);

		// Les deux listes
		catalog = (ListView) findViewById(R.id.catalog);
		order = (ListView) findViewById(R.id.selectedItems);
		orderTotal = (TextView) findViewById(R.id.orderTotal);
		finishOrder = (Button) findViewById(R.id.finishOrder);
		cancelOrder = (Button) findViewById(R.id.cancelOrder);

		reloadItems();

		new Thread(new Runnable() {
			@Override
			public void run() {
				final List<Item> items = ItemCatalog.getInstance().getItems(OrderDetails.this);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// On construit la liste des items du catalog
						ArrayList<HashMap<String, Object>> catalogItems = new ArrayList<HashMap<String, Object>>();

						for (Item item : items) {
							;
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("name", item.getName());
							map.put("price", String.valueOf(item.getPrice()));
							map.put("img", String.valueOf(item.getImg()));
							map.put("item", item);
							catalogItems.add(map);
						}

						// L'afficheur des items
						catalog.setAdapter(new ItemAdapter(OrderDetails.this, catalogItems));
					}
				});
			}
		}).start();

		orderedItemsAdapter = new ItemAdapter(this, orderedItems);
		order.setAdapter(orderedItemsAdapter);

		catalog.setOnItemClickListener(this);
		order.setOnItemClickListener(this);
		finishOrder.setOnClickListener(this);
		cancelOrder.setOnClickListener(this);
	}

	/**
	 * Methode qui initialiser
	 */
	private void reloadItems() {
		ArrayList<Item> items = Order.getInstance().getItems();
		double total = 0;

		for (Item item : items) {
			double price = item.getPrice();

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", item.getName());
			map.put("price", String.valueOf(price));
			map.put("img", String.valueOf(item.getImg()));
			map.put("item", item);
			orderedItems.add(map);

			total += price;
		}

		if (!Order.getInstance().isSet()) {
			if (items.size() > 0) {
				finishOrder.setVisibility(Button.VISIBLE);
			}
		}
		else {
			total = Math.round(total * 100) / 100.0;
			orderTotal.setText("Total: " + total + "$");

			cancelOrder.setVisibility(Button.VISIBLE);
		}
	}

	/**
	 * Methode de selection des items
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void onItemClick(AdapterView<?> a, View v, int position, long id) {
		// Pas plus qu'une commande en meme temps
		if (Order.getInstance().isSet()) {
			return;
		}

		HashMap<String, Object> item;

		switch (a.getId()) {
			case R.id.catalog:
				item = (HashMap<String, Object>) catalog.getItemAtPosition(position);

				// Add item to the list
				orderedItems.add(item);
				orderedItemsAdapter.notifyDataSetChanged();
				Order.getInstance().addItem((Item) item.get("item"));

				if (orderedItems.size() == 1) {
					finishOrder.setVisibility(Button.VISIBLE);
				}

				// Update total
				total += Double.parseDouble(item.get("price").toString());

				// Show notification
				Toast.makeText(this, item.get("name") + " a ete ajoute a la commande!", Toast.LENGTH_SHORT).show();
			break;

			case R.id.selectedItems:
				item = (HashMap<String, Object>) order.getItemAtPosition(position);

				// Delete item from list
				orderedItems.remove(position);
				orderedItemsAdapter.notifyDataSetChanged();
				Order.getInstance().remove(position);

				if (orderedItems.size() == 0) {
					finishOrder.setVisibility(Button.INVISIBLE);
				}

				// Update total
				total -= Double.parseDouble(item.get("price").toString());

				// Show notification
				Toast.makeText(this, item.get("name") + " a ete supprime de la commande!", Toast.LENGTH_SHORT).show();
			break;
		}

		total = Math.round(total * 100) / 100.0;
		orderTotal.setText("Total: " + total + "$");
	}

	/**
	 * Cette methode gere l'evenement de click de bouton
	 * 
	 * @param v Le bouton clique
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.finishOrder:
				startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
			break;

			case R.id.cancelOrder:
				Order.getInstance().clear(this);
				finish();
			break;
		}
	}

	/**
	 * Cette methode recoit les notification des que des requetes d'autres activites on fini
	 * 
	 * @param requestCode Le code de declanchement
	 * @param resultCode Le resultat de la requete
	 * @param data Les donnees
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			Sensors s = new Sensors(this);
			Order.getInstance().process((Activity) this, s.getName(), s.getPhoneNumber(this), s.battery.getPercentage(), (Bitmap) data.getExtras().get("data"));
		}
	}
}
