package com.inf8405.delivr.core.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.inf8405.delivr.activities.Map;

/**
 * Cette classe permet de tourner la carte google maps
 * 
 * @author Dany
 */
public class Tilter {
	private Map map;
	private SensorManager sm;
	private SensorEventListener listener;
	
	/**
	 * Constructeur
	 * 
	 * @param act L'activite
	 */
	public Tilter(Activity act){
		try {
			map = (Map) act;
			
			// Source: StackOverflow
	        sm = (SensorManager) act.getSystemService(Context.SENSOR_SERVICE);        
	
	        // Les matrices utilisees pour les calculs
	        final float[] magnetic = new float[3];
	        final float[] acceleration = new float[3];
	        final float[] orientation = new float[3];
	        final float[] rotation = new float[9];
	
	        listener = new SensorEventListener() {
	            public void onAccuracyChanged(Sensor sensor, int accuracy) {
	            }
	
	            public void onSensorChanged(SensorEvent event) {
	                switch (event.sensor.getType()) {
	                    case Sensor.TYPE_ACCELEROMETER:
	                        System.arraycopy(event.values, 0, acceleration, 0, 3);
	                        break;
	
	                    case Sensor.TYPE_MAGNETIC_FIELD:
	                        System.arraycopy(event.values, 0, magnetic, 0, 3);
	                        break;
	                }
	                
	                SensorManager.getRotationMatrix(rotation, null, acceleration, magnetic);
	                SensorManager.getOrientation(rotation, orientation);
	                map.rotate(orientation);
	            };
	        };
	        
	        start();
		}catch(ClassCastException e){
			return;
		}
	}
	
	/**
	 * Cette methode demarre les evenements
	 */
	public void start(){
		if(listener == null)
			return;
		
		sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
	}
    
	/**
	 * Cette methode arrete les evennements
	 */
	public void stop(){
		if(listener == null)
			return;
		
		sm.unregisterListener(listener);
	}
}
