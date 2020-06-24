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
public class RepairConclusion {
	
	@Id
	String applyNo;
	
	String conclusionDesc, attachUrl, quotationPdfUrl;
	
	Double totalAmount, discount, discountAmount, payableAmount;

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
	 * @return the conclusionDesc
	 */
	public String getConclusionDesc() {
		return conclusionDesc;
	}

	/**
	 * @param conclusionDesc the conclusionDesc to set
	 */
	public void setConclusionDesc(String conclusionDesc) {
		this.conclusionDesc = conclusionDesc;
	}

	/**
	 * @return the attachUrl
	 */
	public String getAttachUrl() {
		return attachUrl;
	}

	/**
	 * @param attachUrl the attachUrl to set
	 */
	public void setAttachUrl(String attachUrl) {
		this.attachUrl = attachUrl;
	}

	/**
	 * @return the quotationPdfUrl
	 */
	public String getQuotationPdfUrl() {
		return quotationPdfUrl;
	}

	/**
	 * @param quotationPdfUrl the quotationPdfUrl to set
	 */
	public void setQuotationPdfUrl(String quotationPdfUrl) {
		this.quotationPdfUrl = quotationPdfUrl;
	}

	/**
	 * @return the totalAmount
	 */
	public Double getTotalAmount() {
		return totalAmount;
	}

	/**
	 * @param totalAmount the totalAmount to set
	 */
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	/**
	 * @return the discount
	 */
	public Double getDiscount() {
		return discount;
	}

	/**
	 * @param discount the discount to set
	 */
	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	/**
	 * @return the discountAmount
	 */
	public Double getDiscountAmount() {
		return discountAmount;
	}

	/**
	 * @param discountAmount the discountAmount to set
	 */
	public void setDiscountAmount(Double discountAmount) {
		this.discountAmount = discountAmount;
	}

	/**
	 * @return the payableAmount
	 */
	public Double getPayableAmount() {
		return payableAmount;
	}

	/**
	 * @param payableAmount the payableAmount to set
	 */
	public void setPayableAmount(Double payableAmount) {
		this.payableAmount = payableAmount;
	}
}
