package io.codibase.server.security;

import io.codibase.server.CodiBase;
import nl.altindag.ssl.SSLFactory;

import javax.net.ssl.SSLSocketFactory;

public abstract class TrustCertsSSLSocketFactory extends SSLSocketFactory {
	
	public static SSLSocketFactory getDefault() {
		return CodiBase.getInstance(SSLFactory.class).getSslSocketFactory();
	}
	
}
