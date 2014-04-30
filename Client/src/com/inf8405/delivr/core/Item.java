package com.inf8405.delivr.core;

/**
 * Cette classe represente un item qu'on peut acheter
 * 
 * @author Dany
 */
public class Item {
	// Le nom de l'item
	private String name;
	// Le prix de l'item
	private double price;
	// Le lien vers l'image de l'item
	private String img;

	/**
	 * Constructeur par parametres
	 * 
	 * @param name Le nom de l'item
	 * @param price Le prix de l'item
	 * @param img L'url de l'image de l'item
	 */
	public Item(String name, double price, String img) {
		this.name = name;
		this.price = price;
		this.img = img;
	}

	/**
	 * Methode d'acces au nom
	 * 
	 * @return Le nom de l'item
	 */
	public String getName() {
		return name;
	}

	/**
	 * Methode d'acces au prix
	 * 
	 * @return Le prix de l'item
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Methode d'acces a l'image
	 * 
	 * @return L'url de l'image de l'item
	 */
	public String getImg() {
		return img;
	}
}
