package com.aaptrix.savitri.databeans;

import java.io.Serializable;

public class TasksData implements Serializable {
	
	private String taskId, taskName, taskDesc, taskDueDate, assignedTo, assignedBy, severity, assignedByName, status;
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getAssignedByName() {
		return assignedByName;
	}
	
	public void setAssignedByName(String assignedByName) {
		this.assignedByName = assignedByName;
	}
	
	public String getSeverity() {
		return severity;
	}
	
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	
	public String getAssignedTo() {
		return assignedTo;
	}
	
	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}
	
	public String getAssignedBy() {
		return assignedBy;
	}
	
	public void setAssignedBy(String assignedBy) {
		this.assignedBy = assignedBy;
	}
	
	public String getTaskId() {
		return taskId;
	}
	
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	public String getTaskName() {
		return taskName;
	}
	
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	
	public String getTaskDesc() {
		return taskDesc;
	}
	
	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
	}
	
	public String getTaskDueDate() {
		return taskDueDate;
	}
	
	public void setTaskDueDate(String taskDueDate) {
		this.taskDueDate = taskDueDate;
	}
}
