package io.codibase.server.model.support.administration;

import java.io.Serializable;

public class BrandingSetting implements Serializable {

	private static final long serialVersionUID = 1L;
			
	private String name = "CodiBase";
		
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
		
}
