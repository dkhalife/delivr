package com.inf8405.delivr.activities;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.inf8405.delivr.R;
import com.inf8405.delivr.core.Order;
import com.inf8405.delivr.core.Server;
import com.inf8405.delivr.core.sensors.Sensors;

/**
 * Cette classe gere la carte Google Maps
 * 
 * @author Dany
 */
public class Map extends Activity implements View.OnClickListener, OnMarkerDragListener, OnMapClickListener, LocationListener {
	// Le facteur de Zoom
	private final float DEFAULT_ZOOM = 15;
	// Les coordonnees par defaut
	private final LatLng DEFAULT_LOCATION = new LatLng(45.504912, -73.613764);
	// L'adresse par defaut
	private final String DEFAULT_ADDRESS = "Ecole Polytechnique de Montreal";
	// Le delai pour les mises a jour
	private final int UPDATE_INTERAVAL = 2000;

	// La carte google maps
	private GoogleMap map;
	// Le marker sur la carte
	private Marker marker;

	// La position du truck
	private Marker truck;

	// La derniere addresse
	private Location location;

	// Les capteurs
	public static Sensors sensors;

	// Le compteur de mise a jour
	private Timer update = new Timer();

	// Le bouton GPS
	private Button gps;

	// Le bouton geolocation
	private Button geolocation;

	// Le bouton order
	private Button order;

	// Le timeout
	private TimerTask cancelLocation;

	/**
	 * Methode qui fait les initialisations
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		sensors = new Sensors(this);

		// La position de depart
		location = new Location("Default");
		location.setLatitude(DEFAULT_LOCATION.latitude);
		location.setLongitude(DEFAULT_LOCATION.longitude);
		location.setTime(new Date().getTime());

		// Bouton de commande
		order = (Button) findViewById(R.id.order);
		order.setOnClickListener(this);

		// Bouton gps
		gps = (Button) findViewById(R.id.gps);
		gps.setOnClickListener(this);

		// Bouton geo
		geolocation = (Button) findViewById(R.id.geolocation);
		geolocation.setOnClickListener(this);

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

		// On ne veut pas que Google offre la position courante parce qu'on veut le faire manuellement
		map.setMyLocationEnabled(false);

		marker = map.addMarker(new MarkerOptions().position(DEFAULT_LOCATION).draggable(true).title("Ma position").snippet(DEFAULT_ADDRESS));
		map.setOnMarkerDragListener(this);
		map.setOnMapClickListener(this);

		truck = map.addMarker(new MarkerOptions().draggable(false).position(new LatLng(0, 0)).title("Voiture Delivr").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)).infoWindowAnchor(0, 0));

		final Order o = Order.getInstance();
		update.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				final LatLng position = Server.getTruckLocation(Map.this);

				if (position != null) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							truck.setPosition(position);
						}
					});
				}

				if (o.isSet()) {
					final Boolean delivered = Server.trackOrder(Map.this, o.getOrderId());

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (delivered == null) {
								Order.getInstance().clear(Map.this);
								order.setText(R.string.orderHere);
								marker.setDraggable(true);

								gps.setEnabled(true);
								geolocation.setEnabled(true);
							}
							else if (delivered == true) {
								// Le son a jouer pour la notification
								Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

								Notification alert = new Notification.Builder(Map.this).setContentTitle("Commande Delivr").setContentText("Votre commande a ete livree!").setSmallIcon(R.drawable.ic_launcher).setSound(soundUri).build();
								NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

								// Ceci est pour cacher la notification une fois selectionnee
								alert.flags |= Notification.FLAG_AUTO_CANCEL;

								notificationManager.notify(0, alert);

								o.clear(Map.this);
								order.setText(R.string.orderHere);
								marker.setDraggable(true);
								gps.setEnabled(true);
								geolocation.setEnabled(true);
							}
						}
					});
				}
			}
		}, 0, UPDATE_INTERAVAL);

		if (o.isSet()) {
			order.setText(R.string.myOrder);
			marker.setPosition(o.getLocation());
			marker.setDraggable(false);
			gps.setEnabled(false);
			geolocation.setEnabled(false);
		}

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), DEFAULT_ZOOM));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		sensors.shaker.stop();
		sensors.tilter.stop();
		sensors.gps.stop(this);
		sensors.geolocation.stop(this);
		update.cancel();
	}

	/**
	 * Methode qui gerer l'evenement de debut de bouger le marker
	 */
	@Override
	public void onMarkerDragStart(Marker marker) {
		marker.hideInfoWindow();
	}

	/**
	 * Methode qui gerer l'evenement de bouger le marker
	 */
	@Override
	public void onMarkerDrag(Marker marker) {
	}

	/**
	 * Methode qui s'occupe de changer l'addresse lorsque le marker arrete de bouger
	 */
	@Override
	public void onMarkerDragEnd(Marker marker) {
		LatLng position = marker.getPosition();
		marker.setSnippet(sensors.getAddressFromLocation(position.latitude, position.longitude));
		marker.showInfoWindow();
	}

	/**
	 * Cette methode demarre la commande
	 */
	private boolean orderStarted = false;
	public void startOrder(){
		if(orderStarted)
			return;
		
		// Une seule fois
		Order.getInstance().setLocation(marker.getPosition());
		startActivity(new Intent("com.inf8405.delivr.activities.ORDER_DETAILS"));
		orderStarted = true;
	}
	
	/**
	 * Cette methode est appelee lorsqu'on revient a l'activite
	 */
	@Override
	public void onResume(){
		super.onResume();
		orderStarted = false;
	}
	
	/**
	 * Methode qui s'occupe des evenements de click
	 */
	@Override
	public void onClick(View v) {
		cancelLocation = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						gps.setEnabled(true);
						geolocation.setEnabled(true);
						order.setEnabled(true);
						sensors.gps.stop(Map.this);
						sensors.geolocation.stop(Map.this);

						Toast.makeText(Map.this, "Impossible de trouver votre position", Toast.LENGTH_SHORT).show();
					}
				});
			}
		};

		switch (v.getId()) {
			case R.id.gps:
				// On active les notifications
				sensors.gps.getLocation(this);
				gps.setEnabled(false);
				geolocation.setEnabled(false);
				order.setEnabled(false);

				// Un timeout pour ne pas rester longtemps
				update.schedule(cancelLocation, 5000);
				marker.hideInfoWindow();

				Toast.makeText(this, "Recherche de votre position avec le GPS", Toast.LENGTH_SHORT).show();
			break;

			case R.id.geolocation:
				// On active les notifications
				sensors.geolocation.getLocation(this);
				gps.setEnabled(false);
				geolocation.setEnabled(false);
				order.setEnabled(false);

				// Un timeout pour ne pas rester longtemps
				update.schedule(cancelLocation, 5000);

				Toast.makeText(this, "Recherche de votre position avec la geolocalisation", Toast.LENGTH_SHORT).show();
			break;

			case R.id.order:
				startOrder();
			break;
		}
	}

	/**
	 * Cette methode gere les clicks sur la carte
	 */
	@Override
	public void onMapClick(LatLng position) {
		if (Order.getInstance().isSet())
			return;

		marker.hideInfoWindow();
		marker.setPosition(position);
		marker.setSnippet(sensors.getAddressFromLocation(position.latitude, position.longitude));
		marker.showInfoWindow();
	}

	/**
	 * Cette methode permet de recevoir les notifications de GPS
	 * 
	 * @param location La position
	 */
	@Override
	public void onLocationChanged(Location location) {
		// On change les infos sur la carte
		LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
		marker.setPosition(latlng);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));
		marker.hideInfoWindow();
		marker.setSnippet(sensors.getAddressFromLocation(location.getLatitude(), location.getLongitude()));
		marker.showInfoWindow();

		// On veut les notifications une fois
		sensors.gps.stop(this);
		sensors.geolocation.stop(this);

		gps.setEnabled(true);
		geolocation.setEnabled(true);
		order.setEnabled(true);

		cancelLocation.cancel();
	}

	/**
	 * Cette methode est appelee lorsque le GPS est desactivite
	 * 
	 * @parma Le provider qui a change
	 */
	@Override
	public void onProviderDisabled(String provider) {
		sensors.gps.stop(this);
	}

	/**
	 * Cette methode est appelee lorsque le GPS est active
	 * 
	 * @param Le provider qui a change
	 */
	@Override
	public void onProviderEnabled(String provider) {
	}

	/**
	 * Cette methode est appelee lorsque le statut d'un provider change
	 * 
	 * @param provider Le provider qui a change
	 * @param status Le status de ce dernier
	 * @param extras Les donees
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * Cette methode permet de tourner la carte
	 * @param orientation La matrice des angles de rotation
	 */
	public void rotate(float[] orientation) {
		final float tilt_delta = 2f;
		float tilt = Math.max(0, Math.min((float) Math.toDegrees(orientation[1]) % 360, 90));
		CameraPosition pos = map.getCameraPosition();
		if (Math.abs(pos.tilt - tilt) > tilt_delta)
			map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().tilt(tilt).bearing(pos.bearing).target(pos.target).zoom(pos.zoom).build()));
	}
}
