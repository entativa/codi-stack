package io.codibase.server.model.support.administration.mailservice;

import com.sun.mail.util.MailSSLSocketFactory;
import io.codibase.server.annotation.Editable;
import io.codibase.server.security.TrustCertsSSLSocketFactory;

import java.security.GeneralSecurityException;
import java.util.Properties;

@Editable
public abstract class SmtpWithSsl implements SmtpSslSetting {
	
	private static final long serialVersionUID = 1L;
	
	private boolean trustAll;

	@Editable(order=1000, name="Bypass Certificate Check", description = "In case SMTP host certificate is self-signed or its CA root is not accepted, " +
			"you may tell CodiBase to bypass certificate check. <b class='text-danger'>WARNING: </b> In " +
			"an untrusted network, this may lead to man-in-the-middle attack, and you should <a href='https://docs.codibase.io/administration-guide/trust-self-signed-certificates#trust-self-signed-certificates-on-server' target='_blank'>import the " +
			"certificate into CodiBase</a> instead")
	public boolean isTrustAll() {
		return trustAll;
	}

	public void setTrustAll(boolean trustAll) {
		this.trustAll = trustAll;
	}

	@Override
	public void configure(Properties properties) {
		properties.setProperty("mail.smtp.ssl.socketFactory.class",
				TrustCertsSSLSocketFactory.class.getName());
		properties.setProperty("mail.smtp.ssl.socketFactory.fallback", "false");
		if (isTrustAll()) {
			try {
				var sf = new MailSSLSocketFactory();
				sf.setTrustAllHosts(true);
				properties.put("mail.smtp.ssl.socketFactory", sf);
			} catch (GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
		}
		properties.setProperty("mail.smtps.localhost", "localhost.localdomain");
	}
}
