package com.inf8405.delivr.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
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
	private static final String host = "http://delivr.dkhalife.com/";

	// Le client HTTP
	private static HttpClient client = new DefaultHttpClient();

	/**
	 * Cette methode cree et envoie les requetes
	 * 
	 * @param path Le chemin dans l'URL
	 * @param params Les parametres a envoyer
	 * @param file Le fichier a joindre
	 * @return La reponse du serveur
	 */
	private static JSONObject post(String path, ArrayList<BasicNameValuePair> params, File file) {
		try {
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

				if (file != null) {
					builder.addPart("image", new FileBody(file));
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
	 * Cette methode permet d'obtenir la position du truck
	 * 
	 * @return La position du truck
	 */
	public static LatLng getTruckLocation(Activity activity) {
		try {
			JSONObject result = post("truck", null, null);

			if (result == null) {
				return null;
			}

			if (!result.getBoolean("success")) {
				showError(activity, result.getString("message"));
				return null;
			}

			return new LatLng(result.getDouble("longitude"), result.getDouble("latitude"));
		}
		catch (JSONException e) {
			return null;
		}
	}

	/**
	 * Cette methode permet d'obtenir la liste d'items vendus
	 * 
	 * @return L'objet JSON retourne par le serveur
	 */
	public static JSONArray getItemsList(Activity activity) {
		try {
			JSONObject result = post("catalog", null, null);

			if (result == null) {
				return null;
			}

			if (!result.getBoolean("success")) {
				showError(activity, result.getString("message"));
				return null;
			}

			return result.getJSONArray("items");
		}
		catch (JSONException e) {
			return null;
		}
	}

	/**
	 * Cette methode permet d'effectuer une commande
	 * 
	 * @return Le numero de commande si tout a reussi, -1 sinon
	 */
	public static int placeOrder(Activity activity, ArrayList<BasicNameValuePair> params, File image) {
		try {
			JSONObject result = post("/map/add", params, image);

			if (result == null) {
				return -1;
			}

			if (!result.getBoolean("success")) {
				showError(activity, result.getString("message"));
				return -1;
			}

			return result.getInt("marker_id");
		}
		catch (JSONException e) {
			return -1;
		}
	}

	/**
	 * Cette methode permet de savoir l'etat d'une commande
	 * 
	 * @param order_id L'identifiant de la commande
	 * @return True si elle a ete livree
	 */
	public static Boolean trackOrder(Activity activity, Integer order_id) {
		try {
			ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("order_id", order_id + ""));

			JSONObject result = post("/map/track", params, null);

			if (result == null) {
				return false;
			}

			if (!result.getBoolean("success")) {
				showError(activity, result.getString("message"));
				return null;
			}

			return result.getBoolean("delivered");
		}
		catch (JSONException e) {
			return false;
		}
	}
}
