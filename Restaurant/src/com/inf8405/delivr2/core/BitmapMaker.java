package com.inf8405.delivr2.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Cette classe permet de construire l'image bitmap
 * qui va etre utilise comme icon pour le marker
 * qui indique l'ordre de livraison d'une commande
 * @author jean-christian
 *
 */
public class BitmapMaker {
	private final int textSize = 50;
	private final int tileSize = 100;
    private Paint mBorderPaint;
    
    /**
     * Constructeur
     * @param context
     */
    public BitmapMaker(Context context) {
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
    }
    
    /**
     * Cette fonction cree le bitmap
     * @param value la valeur a ecrire a l'interieur de l'image
     * @return l'image bitmap cree
     */
    public Bitmap createBitmap(int value) {
    	// On cree le bitmap
        Bitmap copy = Bitmap.createBitmap((int) (tileSize), (int) (100), android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(copy);
        
        // On ecrit le numero 
        String number = "[" + value + "]";

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(textSize);
        p.setColor(Color.RED);
        canvas.drawText(number, tileSize/2, tileSize/2, p);
        return copy;
    }
}
