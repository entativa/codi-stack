package io.codibase.server.model.support.administration.mailservice;

import io.codibase.server.annotation.Editable;

import java.io.Serializable;
import java.util.Properties;

@Editable
public interface SmtpSslSetting extends Serializable {

	void configure(Properties properties);
	
}
