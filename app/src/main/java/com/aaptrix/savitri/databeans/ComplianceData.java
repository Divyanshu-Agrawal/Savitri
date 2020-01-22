package com.aaptrix.savitri.databeans;

import java.io.Serializable;

public class ComplianceData implements Serializable {

	private String id, name, refNo, issueAuth, otherAuth,
			validfrom, validTo, certificate, notes, addedDate, assignedTo, renewCount, markReview, status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMarkReview() {
		return markReview;
	}

	public void setMarkReview(String markReview) {
		this.markReview = markReview;
	}

	public String getRenewCount() {
		return renewCount;
	}
	
	public void setRenewCount(String renewCount) {
		this.renewCount = renewCount;
	}
	
	public String getAssignedTo() {
		return assignedTo;
	}
	
	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
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
	
	public String getRefNo() {
		return refNo;
	}
	
	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	
	public String getIssueAuth() {
		return issueAuth;
	}
	
	public void setIssueAuth(String issueAuth) {
		this.issueAuth = issueAuth;
	}
	
	public String getOtherAuth() {
		return otherAuth;
	}
	
	public void setOtherAuth(String otherAuth) {
		this.otherAuth = otherAuth;
	}
	
	public String getValidfrom() {
		return validfrom;
	}
	
	public void setValidfrom(String validfrom) {
		this.validfrom = validfrom;
	}
	
	public String getValidTo() {
		return validTo;
	}
	
	public void setValidTo(String validTo) {
		this.validTo = validTo;
	}
	
	public String getCertificate() {
		return certificate;
	}
	
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getAddedDate() {
		return addedDate;
	}
	
	public void setAddedDate(String addedDate) {
		this.addedDate = addedDate;
	}
}
