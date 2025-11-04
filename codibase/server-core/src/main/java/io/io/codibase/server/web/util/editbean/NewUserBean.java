package io.codibase.server.web.util.editbean;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import io.codibase.server.annotation.DependsOn;
import io.codibase.server.annotation.Editable;
import io.codibase.server.model.User;

@Editable
public class NewUserBean extends User {

	private static final long serialVersionUID = 1L;

	public static final String PROP_EMAIL_ADDRESS = "emailAddress";
	
	private String emailAddress;

	@Editable(order=1000)
	@DependsOn(property="serviceAccount", value="false")
	@NotEmpty
	@Email
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
}
