package com.inf8405.delivr.core.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.inf8405.delivr.activities.Map;

/**
 * Cette classe represente le capteur de l'accelerometre
 * pour la detection lorsque l'appareil est secoué
 * 
 * @author jean-christian
 *
 */
public class Shaker implements SensorEventListener {
	// L'activite map
	Activity map;
	
	// Le gestionnaire de capteur
	SensorManager sensorManager;
	
	// La derniere mise a jour
	private long lastUpdate;

	// Les dernieres valeurs receuillies 
	private float lastX = 0, lastY = 0, lastZ = 0;
	
	// Le seuil pour detecter le mouvement
	private static final int SHAKE_THRESHOLD = 800;
	
	/**
	 * Constructeur
	 * 
	 * @param map l'activite map
	 */
	public Shaker(Activity map) {
		this.map = map;
		this.sensorManager = (SensorManager) map.getSystemService(Context.SENSOR_SERVICE);
		
		if(map instanceof Map)
			start();
	}
	
	/**
	 * Cette methode demarre les evennements
	 */
	public void start(){
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/**
	 * Cette methode arrete les evennements
	 */
	public void stop(){
		sensorManager.unregisterListener(this);
	}

	/**
	 * Cette methode receuille les evenements de changement dans les capteurs
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		// Source: StackOverflow
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			
		    // On gere les evenements au 100ms
		    if ((curTime - lastUpdate) > 100) {
		      long diffTime = (curTime - lastUpdate);
		      lastUpdate = curTime;

		      float x = event.values[0];
		      float y = event.values[1];
		      float z = event.values[2];

		      float speed = Math.abs(x+y+z-lastX - lastY -lastZ) / diffTime * 10000;
		      
		      if (speed > SHAKE_THRESHOLD) {
		    	  ((Map) map).startOrder();
		      }
		      
		      lastX = x;
		      lastY = y;
		      lastZ = z;
		    }
		}	
	}

	/**
	 * Cette methode gere les changements de precision
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {		
	}
}
