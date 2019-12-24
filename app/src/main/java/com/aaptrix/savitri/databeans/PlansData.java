package com.aaptrix.savitri.databeans;

import java.io.Serializable;

public class PlansData implements Serializable {
	
	private String id, name, alertByApp, alertByEmail, alertBySms, complianceLimit, dataDownload, storageCycle, planCost, userLimit;
	
	public String getUserLimit() {
		return userLimit;
	}
	
	public void setUserLimit(String userLimit) {
		this.userLimit = userLimit;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAlertByApp() {
		return alertByApp;
	}
	
	public void setAlertByApp(String alertByApp) {
		this.alertByApp = alertByApp;
	}
	
	public String getAlertByEmail() {
		return alertByEmail;
	}
	
	public void setAlertByEmail(String alertByEmail) {
		this.alertByEmail = alertByEmail;
	}
	
	public String getAlertBySms() {
		return alertBySms;
	}
	
	public void setAlertBySms(String alertBySms) {
		this.alertBySms = alertBySms;
	}
	
	public String getComplianceLimit() {
		return complianceLimit;
	}
	
	public void setComplianceLimit(String complianceLimit) {
		this.complianceLimit = complianceLimit;
	}
	
	public String getDataDownload() {
		return dataDownload;
	}
	
	public void setDataDownload(String dataDownload) {
		this.dataDownload = dataDownload;
	}
	
	public String getStorageCycle() {
		return storageCycle;
	}
	
	public void setStorageCycle(String storageCycle) {
		this.storageCycle = storageCycle;
	}
	
	public String getPlanCost() {
		return planCost;
	}
	
	public void setPlanCost(String planCost) {
		this.planCost = planCost;
	}
}
