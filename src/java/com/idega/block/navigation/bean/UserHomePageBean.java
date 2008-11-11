package com.idega.block.navigation.bean;

public class UserHomePageBean {

	private String id;
	private String name;
	private String uri;
	
	public UserHomePageBean() {}
	
	public UserHomePageBean(String id, String name, String uri) {
		this();
		
		this.id = id;
		this.name = name;
		this.uri = uri;
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
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
