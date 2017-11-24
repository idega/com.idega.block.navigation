package com.idega.block.navigation.bean;

import java.io.Serializable;

public class UserHomePageBean implements Serializable {

	private static final long serialVersionUID = -5932605698840614929L;

	private String id;
	private String name;
	private String uri;
	private String role;

	public UserHomePageBean() {
		super();
	}

	public UserHomePageBean(String id, String name, String uri, String role) {
		this();

		this.id = id;
		this.name = name;
		this.uri = uri;
		this.role = role;
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
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "ID: " + getId() + ", name: " + getName() + ", uri: " + getUri() + ", role: " + getRole();
	}

}