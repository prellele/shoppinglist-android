package de.shoppinglist.android.bean;

import java.sql.Timestamp;

public class Shoppinglist extends BusinessBean {

	private Timestamp finishedTime;

	private Timestamp createdTime;

	public Timestamp getFinishedTime() {
		return finishedTime;
	}

	public void setFinishedTime(Timestamp finishedTime) {
		this.finishedTime = finishedTime;
	}

	public Timestamp getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Timestamp createdTime) {
		this.createdTime = createdTime;
	}
}
