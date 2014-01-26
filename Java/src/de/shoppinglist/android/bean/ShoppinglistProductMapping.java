package de.shoppinglist.android.bean;

public class ShoppinglistProductMapping extends BusinessBean {

	private Shoppinglist shoppinglist;

	private Store store;

	private Product product;

	private String quantity;

	private short checked;

	public Shoppinglist getShoppinglist() {
		return shoppinglist;
	}

	public void setShoppinglist(Shoppinglist shoppinglist) {
		this.shoppinglist = shoppinglist;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public short isChecked() {
		return checked;
	}

	public void setChecked(short checked) {
		this.checked = checked;
	}

	@Override
	public String toString() {
		return this.quantity + " " + this.product.getUnit().getName() + " "
				+ this.product.getName();
	}
}
