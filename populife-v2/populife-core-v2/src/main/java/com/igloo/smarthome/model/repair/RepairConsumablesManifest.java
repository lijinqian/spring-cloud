/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.repair;

import javax.persistence.Id;

/**
 * 
 * @author Ares S
 * @date 2020年6月8日
 */
public class RepairConsumablesManifest {
	
	/**
	 * 
	 */
	public RepairConsumablesManifest() {
		super();
	}

	/**
	 * @param applyNo
	 */
	public RepairConsumablesManifest(String applyNo) {
		super();
		this.applyNo = applyNo;
	}

	@Id
	String id;
	
	String applyNo, partsName;
	
	Integer quantity;
	
	Double price, totalPrice;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the applyNo
	 */
	public String getApplyNo() {
		return applyNo;
	}

	/**
	 * @param applyNo the applyNo to set
	 */
	public void setApplyNo(String applyNo) {
		this.applyNo = applyNo;
	}

	/**
	 * @return the partsName
	 */
	public String getPartsName() {
		return partsName;
	}

	/**
	 * @param partsName the partsName to set
	 */
	public void setPartsName(String partsName) {
		this.partsName = partsName;
	}

	/**
	 * @return the quantity
	 */
	public Integer getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the price
	 */
	public Double getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(Double price) {
		this.price = price;
	}

	/**
	 * @return the totalPrice
	 */
	public Double getTotalPrice() {
		return totalPrice;
	}

	/**
	 * @param totalPrice the totalPrice to set
	 */
	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}
}
