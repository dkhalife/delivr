package com.inf8405.delivr2.core;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Cette classe permet d'effectuer des requetes au serveur
 * 
 * @author Dany
 */
public class Server {
	// L'url du serveur
	public static final String host = "http://delivr.dkhalife.com/";

	// Le client HTTP
	private static HttpClient client = new DefaultHttpClient();

	/**
	 * Cette methode cree et envoie les requetes
	 * 
	 * @param path Le chemin dans l'URL
	 * @param params Les parametres a envoyer
	 * @return La reponse du serveur
	 */
	private static JSONObject post(String path, ArrayList<BasicNameValuePair> params) {
		try {
			if (params == null) {
				params = new ArrayList<BasicNameValuePair>();
			}

			// On ajoute le mot de passe
			params.add(new BasicNameValuePair("key", "iamadmin"));

			HttpPost req = new HttpPost(host + path);

			if (params != null) {
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				for (NameValuePair param : params) {
					String value = param.getValue();
					if (value == null)
						value = "";

					builder.addTextBody(param.getName(), value);
				}
				req.setEntity(builder.build());
			}

			HttpResponse response = client.execute(req);

			HttpEntity e = response.getEntity();

			if (e != null) {
				return new JSONObject(EntityUtils.toString(e));
			}

			return null;
		}
		catch (IOException e) {
			return null;
		}
		catch (JSONException e1) {
			return null;
		}
	}

	/**
	 * Cette methode s'occupe d'afficher les erreurs
	 * 
	 * @param activity L'activite ou afficher l'erreur
	 * @param error L'erreur
	 */
	private static void showError(final Activity activity, final String error) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, "Erreur: " + error, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Cette methode permet de mettre a jour ma position
	 * 
	 * @param activity L'activite
	 * @param position La nouvelle position
	 */
	public static void moveTruck(Activity activity, LatLng position) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("longitude", position.longitude + ""));
		params.add(new BasicNameValuePair("latitude", position.latitude + ""));

		JSONObject result = post("truck/move", params);

		if (result == null) {
			return;
		}

		try {
			if (!result.getBoolean("success")) {
				showError(activity, result.getString("message"));
				return;
			}
		}
		catch (Exception e) {
			return;
		}
	}

	/**
	 * Cette methode permet d'obtenir la liste des clients et leurs commandes
	 * 
	 * @param activity L'activite
	 * @return La liste des clients et leur commandes
	 */
	public static JSONArray getUnservedClients(Activity activity) {
		JSONObject result = post("map", null);

		if (result == null) {
			return null;
		}

		try {
			if (!result.getBoolean("success")) {
				showError(activity, result.getString("message"));
				return null;
			}

			return result.getJSONArray("markers");
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Cette methode permet de marquer un client comme etant servi
	 * 
	 * @param marker_id L'identifiant du marqueur a visiter
	 * @return True si la requette s'est bien passee
	 */
	public static void serveClient(Activity activity, int marker_id) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("marker_id", marker_id + ""));
		JSONObject result = post("map/visit", params);

		if (result == null) {
			return;
		}

		try {
			if (!result.getBoolean("success")) {
				showError(activity, result.getString("message"));
				return;
			}
		}
		catch (Exception e) {
			return;
		}
	}
}
