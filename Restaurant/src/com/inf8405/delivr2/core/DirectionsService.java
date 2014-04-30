package com.inf8405.delivr2.core;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.android.gms.maps.model.LatLng;

/**
 * Cette classe permet de gerer le service google directions
 * et permet de d'obtenir des informations sur les itineraires
 * calculés
 * @author jean-christian
 *
 */
public class DirectionsService {
	// la clé de server utilisee pour contacter le service google directions
	private final String API_KEY = "AIzaSyDUSqFhHctmjq-daILa63TiBU1Hju26EL0";
	
	/**
	 * Obtenir l'url de la requete
	 * @param origin la coordonnee de depart
	 * @param destination la coordonnee d'arrivee
	 * @param waypoints les points de transition
	 * @return la chaine représentant la requete a adresser pour obtenir l'itineraire
	 */
	public String getUrl(LatLng origin, LatLng destination, List<LatLng> waypoints) {
		String waypointsString = "";
		
		if (waypoints != null) {
			try {
				String bar = URLEncoder.encode("|", "UTF-8");
				for (LatLng pos : waypoints) {
					if (pos.latitude != destination.latitude && pos.longitude != destination.longitude) {
						waypointsString += bar + pos.latitude + "," + pos.longitude;
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		String url = "https://maps.googleapis.com/maps/api/directions/xml?"
				+ "origin=" + origin.latitude + "," + origin.longitude
				+ "&destination=" + destination.latitude + "," + destination.longitude;
				if (!waypointsString.isEmpty()) {
					url += "&waypoints=optimize:true"
					+ waypointsString;
				}
				url += "&sensor=true"
				+ "&key=" + API_KEY;
		return url;
	}
	
	/**
	 * Cette fonction effectue une requete au service google direction
	 * @param origin la coordonnee de depart
	 * @param destination la coordonnee d'arrivee
	 * @param waypoints les points de transition
	 * @return un XML contenant les informations sur l'itineraire
	 */
	public InputSource getDirectionResponse (LatLng origin, LatLng destination, List<LatLng> waypoints) {
		String url = getUrl(origin, destination, waypoints);
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(url);
			HttpResponse response = httpClient.execute(httpPost, localContext);
			InputStream in = response.getEntity().getContent();
			InputSource inputXml = new InputSource(in);
			return inputXml;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Ceyye fonction permet d'obtenir la valeur d'un champ dans le fichier XML
	 * @param fieldPath le champ dont on veut la valeur
	 * @param inputXml le XML contenant les informations sur l'itineraire
	 * @return
	 */
	public List<String> getXMLField(String fieldPath, InputSource inputXml) {
		
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    List<String> contents = new ArrayList<String>();
	    
		try {
			NodeList nodes = (NodeList) xpath.evaluate("/" + fieldPath, inputXml, XPathConstants.NODESET);
			
			for (int i = 0, n = nodes.getLength(); i < n; i++) {
				contents.add(nodes.item(i).getTextContent().replace("\n", "").replace(" ", ""));
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return contents;
	}
	
	/**
	 * Cette fonction permet d'obtenir l'ordre des points de transition
	 * @param inputXml le XML contenant les informations sur l'itineraire
	 * @return la liste contenant l'ordre des points de transition
	 */
	public List<Integer> getDirectionsWaypointsOrder(InputSource inputXml) {
		List<Integer> waypoints = new ArrayList<Integer>();
		List<String> contents = getXMLField("DirectionsResponse//waypoint_index", inputXml);
		for (String waypoint : contents) {
			waypoints.add(Integer.parseInt(waypoint));
		}
		return waypoints;
	}
	
	/**
	 * Cette fonction permet d'obtenir la duree du trajet
	 * @param inputXml le XML contenant les informations sur l'itineraire
	 * @return la duree du trajet
	 */
	public double getDirectionsDuration (InputSource inputXml) {
		double duration = 0.0;
		List<String> contents = getXMLField("DirectionsResponse/route/leg/duration/value", inputXml);
		for (int i=0; i<contents.size(); i++) {
			duration += Double.parseDouble(contents.get(i));
		}
		return duration;
	}
	
	/**
	 * Cette fonction permet d'obtenir l'itineraire
	 * @param inputXml le XML contenant les informations sur l'itineraire
	 * @return la liste des points de l'itineraire
	 */
	public List<LatLng> getDirectionsPoints (InputSource inputXml) {
		List<LatLng> points = new ArrayList<LatLng>();
		List<String> contents = getXMLField("DirectionsResponse//overview_polyline", inputXml);
		for (String encoded : contents) {
			List<LatLng> decodedPoints = decodePoints(encoded);
			points.addAll(decodedPoints);
		}
		return points;
	}
	
	/**
	 * Cette fonction permet de decoder la valeur du champs 'overview_polyline'
	 * de la reponse du service d'itineraire de google
	 * @param encoded la chaine a decoder
	 * @return la liste des points décodés
	 */
	public List<LatLng> decodePoints (String encoded) {
		// Cette algorithme implemente le decodage tel que specifie dans la documentation de Google Maps 
		List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
	}
}
