package com.inf8405.delivr2.activities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.inf8405.delivr2.R;
import com.inf8405.delivr2.core.BitmapMaker;
import com.inf8405.delivr2.core.Client;
import com.inf8405.delivr2.core.DirectionsService;
import com.inf8405.delivr2.core.Server;
import com.inf8405.delivr2.core.sensors.Sensors;

/**
 * Cette classe gere la carte Google Maps
 * 
 * @author Dany
 */
public class Map extends Activity implements View.OnClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener, OnInfoWindowClickListener, LocationListener {
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

	// Les capteurs
	public static Sensors sensors;

	// Le bouton GPS
	private ToggleButton gps;

	// Le bouton GEO
	private ToggleButton geo;
	
	//le bouton itineraire
	private ToggleButton direction;

	// Le compteur de mise a jour
	private Timer update = new Timer();

	// Le wakeLock permet de garder l'appareil allume
	private WakeLock wL;

	// La liste des clients par ID
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Client> clientsI = new HashMap<Integer, Client>();

	// La liste des clients par marker
	private HashMap<Marker, Client> clientsM = new HashMap<Marker, Client>();
	
	// le polyline qui contient la direction a suivre
	private Polyline polyline;
	
	//liste des bitmap crees
	private List<BitmapDescriptor> bitmaps = new ArrayList<BitmapDescriptor>();
	
	//la liste des groundOverlay
	private List<Marker> waypointMarkers = new ArrayList<Marker>();
	
	//l'id des waypoints selon leur ordre de parcours
	private List<Integer> waypointOrdered = new ArrayList<Integer>();
	
	//la liste des waypoints
	private List<LatLng> waypoints = new ArrayList<LatLng>();
	
	/**
	 * Methode qui fait les initialisations
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		sensors = new Sensors(this);

		// Bouton gps
		gps = (ToggleButton) findViewById(R.id.gps);
		gps.setOnClickListener(this);

		// Bouton geo
		geo = (ToggleButton) findViewById(R.id.geolocation);
		geo.setOnClickListener(this);
		
		//bouton direction
		direction = (ToggleButton) findViewById(R.id.direction);
		direction.setOnClickListener(this);

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setOnMapClickListener(this);

		// On ne veut pas que Google offre la position courante parce qu'on veut le faire manuellement
		map.setMyLocationEnabled(false);

		// Position par defaut
		marker = map.addMarker(new MarkerOptions().position(DEFAULT_LOCATION).draggable(true).title("Ma position").snippet(DEFAULT_ADDRESS).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
		map.setOnMapClickListener(this);
		map.setOnInfoWindowClickListener(this);
		map.setOnMarkerDragListener(this);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));		
		
		update.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Get clients
				JSONArray list = Server.getUnservedClients(Map.this);

				if (list == null)
					return;

				for (int i = 0; i < list.length(); ++i) {
					try {
						JSONObject c = list.getJSONObject(i);

						// Algo de garbage collection
						for (Client x : clientsI.values()) {
							x.gc = true;
						}

						Integer marker_id = c.getInt("marker_id");
						if (clientsI.containsKey(marker_id)) {
							// Mark old
							clientsI.get(marker_id).gc = false;
						}
						else {
							// Add new
							final Client client = new Client();
							client.gc = false;
							client.marker_id = marker_id;
							clientsI.put(marker_id, client);

							client.name = c.getString("name");
							client.phone = c.getString("phone");
							client.battery = c.getDouble("battery");
							client.longitude = c.getDouble("longitude");
							client.latitude = c.getDouble("latitude");
							client.order = c.getString("order");
							client.image = getDrawableFromUrl(Server.host + c.getString("image"));

							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									client.marker = map.addMarker(new MarkerOptions().title(client.name).snippet(sensors.getAddressFromLocation(client.latitude, client.longitude)).position(new LatLng(client.latitude, client.longitude)).draggable(false));
									clientsM.put(client.marker, client);
								}
							});
						}

						clientsI.values().removeAll(Collections.singleton(Client.GC));
					}
					catch (JSONException e) {
						continue;
					}
					catch (MalformedURLException e) {
						continue;
					}
					catch (IOException e) {
						continue;
					}
				}
			}
		}, 0, UPDATE_INTERAVAL);

		// On obtient le lock pour prevenir le sleep pendant le jeu
		PowerManager pM = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wL = pM.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "tag");
		super.onCreate(savedInstanceState);
		wL.acquire();
	}

	/**
	 * Cette methode est appelle lorsque le jeu se met en pause
	 */
	@Override
	protected void onPause() {
		// Il ne faut pas oublier de relacher le lock
		wL.release();

		super.onPause();
	}

	/**
	 * Cette methode est appelle lorsque l'application revient du mode pause
	 */
	@Override
	protected void onResume() {
		// Il faut qu'on obtient de nouveau le wake lock
		wL.acquire();

		super.onResume();
	}

	/**
	 * Cette methode est executee quand on quitte l'activite
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		sensors.gps.stop(this);
		sensors.geolocation.stop(this);
		update.cancel();
	}

	/**
	 * Methode qui s'occupe des evenements de click
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.gps:
				if (gps.isChecked()) {
					disableDirection();
					geo.setChecked(false);
					marker.setDraggable(false);
					sensors.gps.getLocation(this);

					Toast.makeText(this, "Votre position sera mise a jour regulierement a l'aide du GPS", Toast.LENGTH_SHORT).show();
				}
				else {
					sensors.gps.stop(this);

					if (!geo.isChecked()) {
						marker.setDraggable(true);

						Toast.makeText(this, "Votre position ne sera plus mise a jour automatiquement", Toast.LENGTH_SHORT).show();
					}
				}

			break;

			case R.id.geolocation:
				if (geo.isChecked()) {
					disableDirection();
					gps.setChecked(false);
					marker.setDraggable(false);
					sensors.geolocation.getLocation(this);

					Toast.makeText(this, "Votre position sera mise a jour regulierement a l'aide de la Geolocalisation", Toast.LENGTH_SHORT).show();
				}
				else {
					sensors.geolocation.stop(this);

					if (!gps.isChecked()) {
						marker.setDraggable(true);

						Toast.makeText(this, "Votre position ne sera plus mise a jour automatiquement", Toast.LENGTH_SHORT).show();
					}
				}
			break;
			
			case R.id.direction:
				if (direction.isChecked()) {
					direction.setEnabled(false);
					final LatLng truckPosition = marker.getPosition();
					new Thread(new Runnable() {
						List<LatLng> points = new ArrayList<LatLng>();
						@Override
						public void run() {
							 points = getDirections(truckPosition);
							 runOnUiThread(new Runnable(){
								@Override
								public void run() {									
									//on rend invisible les markers des clients
									for (Entry<Marker, Client> entry : clientsM.entrySet()) {
										entry.getKey().setVisible(false);
									}
									
									//on rajoute les markers pour indiquer l'ordre de livraison
									int index;
									for (index=0; index<waypointOrdered.size(); index++) {
										Marker waypointMarker = map.addMarker(new MarkerOptions().position(waypoints.get(waypointOrdered.get(index))).draggable(false).icon(bitmaps.get(index)));
										waypointMarkers.add(waypointMarker);
									}
									
									
									PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.BLUE);
									for(int i = 0 ; i < points.size() ; i++) {          
										rectLine.add(points.get(i));
									}
									if(polyline!= null && !polyline.getPoints().isEmpty()) {
										polyline.remove();
									}
									polyline = map.addPolyline(rectLine);
									direction.setEnabled(true);
								}
							});
						}
					}).start();
				} else {
					disableDirection();
				}
				break;
		}
	}

	/**
	 * Methode qui gerer l'evenement de debut de bouger le marker
	 */
	@Override
	public void onMarkerDragStart(Marker marker) {
		marker.hideInfoWindow();
		disableDirection();
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
		final LatLng position = marker.getPosition();
		marker.setSnippet(sensors.getAddressFromLocation(position.latitude, position.longitude));
		marker.showInfoWindow();

		new Thread(new Runnable() {
			@Override
			public void run() {
				Server.moveTruck(Map.this, position);
			}
		}).start();
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
		return Drawable.createFromStream(((java.io.InputStream) new java.net.URL(url).getContent()), null);
	}

	/**
	 * Cette methode gere les clicks sur la carte
	 */
	@Override
	public void onMapClick(LatLng position) {
		if (geo.isChecked() || gps.isChecked())
			return;

		marker.hideInfoWindow();
		marker.setPosition(position);
		marker.setSnippet(sensors.getAddressFromLocation(position.latitude, position.longitude));
		marker.showInfoWindow();
		
		disableDirection();
	}

	/**
	 * Cette methode permet de recevoir les notifications de GPS
	 * 
	 * @param location La position
	 */
	@Override
	public void onLocationChanged(Location location) {
		final LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());

		if (!newPosition.equals(marker.getPosition())) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Server.moveTruck(Map.this, newPosition);
				}
			}).start();

			map.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, DEFAULT_ZOOM));
			marker.setPosition(newPosition);
			marker.hideInfoWindow();
			marker.setSnippet(sensors.getAddressFromLocation(newPosition.latitude, newPosition.longitude));
			marker.showInfoWindow();
		}
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
	 * Cette methode gere les evenements de click sur les markeurs
	 * 
	 * @param m Le marker clique
	 */
	@Override
	public void onInfoWindowClick(final Marker m) {
		final Client c = clientsM.get(m);

		// On veut pas traiter le marker du driver
		if (c == null)
			return;

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.client);
		dialog.setTitle("Informations client:");

		ImageView image = (ImageView) dialog.findViewById(R.id.image);

		TextView name = (TextView) dialog.findViewById(R.id.name);
		TextView phone = (TextView) dialog.findViewById(R.id.phone);
		TextView longitude = (TextView) dialog.findViewById(R.id.longitude);
		TextView latitude = (TextView) dialog.findViewById(R.id.latitude);
		TextView battery = (TextView) dialog.findViewById(R.id.battery);
		TextView order = (TextView) dialog.findViewById(R.id.order);

		Button serve = (Button) dialog.findViewById(R.id.serve);
		Button close = (Button) dialog.findViewById(R.id.close);

		if (c.image != null) {
			image.setBackground(c.image);
		}
		else {
			image.setBackground(getResources().getDrawable(R.drawable.person));
		}

		name.setText(c.name.substring(0, c.name.indexOf('@')));
		phone.setText(c.phone);
		longitude.setText(c.longitude + "");
		latitude.setText(c.latitude + "");
		battery.setText(100 * c.battery + "%");
		order.setText(c.order);

		View.OnClickListener l = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.serve:
						new Thread(new Runnable() {
							@Override
							public void run() {
								Server.serveClient(Map.this, c.marker_id);
							}
						}).start();

						m.remove();
					break;
				}

				dialog.dismiss();
			}
		};

		serve.setOnClickListener(l);
		close.setOnClickListener(l);
		dialog.show();
	}
	
	/**
	 * Cette fonction desactive et efface les options de l'itineraire
	 */
	public void disableDirection() {
		direction.setChecked(false);
		//on efface la trajectoire
		if(polyline!= null && !polyline.getPoints().isEmpty()) {
			polyline.remove();
		}
		//on retire les marker qui specifie l'ordre de livraison
		for (int i=0; i<waypointMarkers.size(); i++) {
			waypointMarkers.get(i).remove();
		}
		//on rend visible les marker de chaque client
		for (Entry<Marker, Client> entry : clientsM.entrySet()) {
			entry.getKey().setVisible(true);
		}
		//on efface les donnée d'itinineraire
		waypoints.clear();
		bitmaps.clear();
		waypointMarkers.clear();
	}
	
	/**
	 * vérifie si deux coordonnée pointe a la meme adresse
	 * @param op1 la premiere coordonnee
	 * @param op2 la seconde coordonnee
	 * @return true, si il s'agit de la meme adresse, false sinon
	 */
	public static boolean areTheSameAdress(LatLng op1, LatLng op2) {
		return (op1.latitude == op2.latitude && op1.longitude == op2.longitude);
	}
	
	/**
	 * Cette fonction calcul l'itineraire du camion
	 * @param truckPosition la position actuelle du camion
	 * @return la trajectoire du camion sous forme de points a suivre
	 */
	public List<LatLng> getDirections(LatLng truckPosition) {
		List<LatLng> points = new ArrayList<LatLng>();
		DirectionsService dr = new DirectionsService();
		double smallestDuration = Double.MAX_VALUE;
		// obtenir les waypoints
		for ( Entry<Integer, Client>  entry : clientsI.entrySet()) {
			LatLng clientAdress = new LatLng(entry.getValue().latitude, entry.getValue().longitude);
			boolean exist = false;
			//verifier que l'adresse n'est pas déjà repertoriée avant de l'ajouter
			for (int i=0; i<waypoints.size(); i++) {
				exist = exist | areTheSameAdress(waypoints.get(i), clientAdress);
			}
			if (!exist) {
				waypoints.add(clientAdress);
			}
		}
		
		//On recherche l'itineraire le moins long (peu de temps)
		LatLng origin = truckPosition;
		LatLng lastToBeDelivered = truckPosition;
		for ( Entry<Integer, Client>  entry : clientsI.entrySet()) {
			LatLng destination = new LatLng(entry.getValue().latitude, entry.getValue().longitude);
			InputSource inputXml = dr.getDirectionResponse(origin, destination, waypoints);
			double duration = dr.getDirectionsDuration(inputXml);
			if (duration < smallestDuration) {
				smallestDuration = duration;
				lastToBeDelivered = destination;
			}
		}
		
		//on enregistre l'ordre de parcours des livraisons
		waypoints.remove(lastToBeDelivered);
		InputSource inputXml = dr.getDirectionResponse(origin, lastToBeDelivered, waypoints);
		waypointOrdered = dr.getDirectionsWaypointsOrder(inputXml);
		waypoints.add(lastToBeDelivered);
		waypointOrdered.add(waypoints.size()-1);
		int index;
		for (index=0; index<waypointOrdered.size(); index++) {
			BitmapMaker bm = new BitmapMaker(getApplicationContext());
			bitmaps.add(BitmapDescriptorFactory.fromBitmap(bm.createBitmap(index+1)));
		}
		
		// on extrait les points du parcours et on trace des polyline sur le map
		inputXml = dr.getDirectionResponse(origin, lastToBeDelivered, waypoints);
		points = dr.getDirectionsPoints(inputXml);
		return points;
	}
}
