package com.aimir.mm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RechargeHistoryEnt", propOrder = {
    "transaction_id",
    "purchase_type",
    "paid_arrers",
    "pre_arrers_amount",
    "arrers_amount",
    "pre_balance_amount",
    "balance_amount",
    "purchase_amount",
    "purchase_date",
    "token_number",
    "cancel_date",
    "cancel_token_number",
    "cancel_reason"
})

public class RechargeHistoryEnt {
	@XmlElement(name = "transaction_id")
	protected String transaction_id;
	
	@XmlElement(name = "purchase_type")
	protected String purchase_type;
	
	@XmlElement(name = "paid_arrers")
	protected Double paid_arrers;

	@XmlElement(name = "pre_arrers_amount")
	protected Double pre_arrers_amount;
	
	@XmlElement(name = "arrers_amount")
	protected Double arrers_amount;

	@XmlElement(name = "pre_balance_amount")
	protected Double pre_balance_amount;
	
	@XmlElement(name = "balance_amount")
	protected Double balance_amount;
	
	@XmlElement(name = "purchase_amount")
	protected Double purchase_amount;

	@XmlElement(name = "purchase_date")
	protected String purchase_date;

	@XmlElement(name = "token_number")
	protected String token_number;

	@XmlElement(name = "cancel_date")
	protected String cancel_date;
	
	@XmlElement(name = "cancel_token_number")
	protected String cancel_token_number;

	@XmlElement(name = "cancel_reason")
	protected String cancel_reason;

	
	public String getTransactionId() {
		return transaction_id;
	}

	public void setTransactionId(String transaction_id) {
		this.transaction_id = transaction_id;
	}

	public String getPurchaseType() {
		return purchase_type;
	}

	public void setPurchaseType(String purchase_type) {
		this.purchase_type = purchase_type;
	}

	public Double getPaidArrers() {
		return paid_arrers;
	}

	public void setPaidArrers(Double paid_arrers) {
		this.paid_arrers = paid_arrers;
	}

	public Double getPreArrersAmount() {
		return pre_arrers_amount;
	}

	public void setPreArrersAmount(Double pre_arrers_amount) {
		this.pre_arrers_amount = pre_arrers_amount;
	}

	public Double getArrersAmount() {
		return arrers_amount;
	}

	public void setArrersAmount(Double arrers_amount) {
		this.arrers_amount = arrers_amount;
	}

	public Double getPreBalanceAmount() {
		return pre_balance_amount;
	}

	public void setPreBalanceAmount(Double pre_balance_amount) {
		this.pre_balance_amount = pre_balance_amount;
	}
	
	public Double getBalanceAmount() {
		return balance_amount;
	}

	public void setBalanceAmount(Double balance_amount) {
		this.balance_amount = balance_amount;
	}
	
	public Double getPurchaseAmount() {
		return purchase_amount;
	}

	public void setPurchaseAmount(Double purchase_amount) {
		this.purchase_amount = purchase_amount;
	}

	public String getPurchaseDate() {
		return purchase_date;
	}

	public void setPurchaseDate(String purchase_date) {
		this.purchase_date = purchase_date;
	}

	public String getTokenNumber() {
		return token_number;
	}

	public void setTokenNumber(String token_number) {
		this.token_number = token_number;
	}

	public String getCancelDate() {
		return cancel_date;
	}

	public void setCancelDate(String cancel_date) {
		this.cancel_date = cancel_date;
	}
	
	public String getCancelTokenNumber() {
		return cancel_token_number;
	}

	public void setCancelTokenNumber(String cancel_token_number) {
		this.cancel_token_number = cancel_token_number;
	}

	public String getCancelReason() {
		return cancel_reason;
	}

	public void setCancelReason(String cancel_reason) {
		this.cancel_reason = cancel_reason;
	}
    
}
