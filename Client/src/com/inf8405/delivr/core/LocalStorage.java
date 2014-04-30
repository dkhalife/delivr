package com.inf8405.delivr.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.content.Context;

/**
 * LocalStorage
 * 
 * @see http
 * ://stackoverflow.com/questions/5816695/android-sharedpreferences-with
 * -serializable-object/5816861#5816861
 */
public class LocalStorage {
	/**
	 * Methode pour ecrire un fichier
	 * 
	 * @param context Le contexte Android
	 * @param object L'objet a entregistrer
	 * @param filename Le fichier dans lequel enregistrer l'objet
	 */
	public static void writeObjectToFile(Context context, Object object, String filename) {
		ObjectOutputStream objectOut = null;
		try {
			FileOutputStream fileOut = context.openFileOutput(filename, Activity.MODE_PRIVATE);
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(object);
			fileOut.getFD().sync();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (objectOut != null) {
				try {
					objectOut.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Methode pour lire un objet d'un fichier
	 * 
	 * @param context Le contexte Android
	 * @param filename Le fichier a lire
	 * @return L'objet contenu dans le fichier
	 */
	public static Object readObjectFromFile(Context context, String filename) {
		ObjectInputStream objectIn = null;
		Object object = null;
		try {
			FileInputStream fileIn = context.getApplicationContext().openFileInput(filename);
			objectIn = new ObjectInputStream(fileIn);
			object = objectIn.readObject();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (objectIn != null) {
				try {
					objectIn.close();
				} catch (IOException e) {
				}
			}
		}

		return object;
	}
}
