package de.shoppinglist.android.bean;

public class Store extends BusinessBean {

	private int alreadyCheckedProducts;

	private int countProducts;

	private String name;

	/**
	 * Returns the alreadyCheckedProducts.
	 * 
	 * @return Returns the alreadyCheckedProducts.
	 */
	public int getAlreadyCheckedProducts() {
		return this.alreadyCheckedProducts;
	}

	/**
	 * Returns the countProducts.
	 * 
	 * @return Returns the countProducts.
	 */
	public int getCountProducts() {
		return this.countProducts;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Sets the alreadyCheckedProducts field with given alreadyCheckedProducts.
	 * 
	 * @param alreadyCheckedProducts
	 *            The alreadyCheckedProducts to set.
	 */
	public void setAlreadyCheckedProducts(final int alreadyCheckedProducts) {
		this.alreadyCheckedProducts = alreadyCheckedProducts;
	}

	/**
	 * Sets the countProducts field with given countProducts.
	 * 
	 * @param countProducts
	 *            The countProducts to set.
	 */
	public void setCountProducts(final int countProducts) {
		this.countProducts = countProducts;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name + " ( " + this.alreadyCheckedProducts + " / " + this.countProducts + " )";
	}

}
