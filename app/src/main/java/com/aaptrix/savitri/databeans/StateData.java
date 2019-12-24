package com.aaptrix.savitri.databeans;

import java.io.Serializable;

public class StateData implements Serializable {
	
	String name, id;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
