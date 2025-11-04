package io.codibase.server.exception.handler;

import io.codibase.server.exception.HttpResponse;
import io.codibase.server.exception.HttpResponseBody;
import io.codibase.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class UnauthorizedExceptionHandler extends AbstractExceptionHandler<UnauthorizedException> {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public HttpResponse getResponse(UnauthorizedException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not authorized";
		
		if (SecurityUtils.isAnonymous()) {
			MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
			headers.putSingle("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"CodiBase\"");
			return new HttpResponse(HttpServletResponse.SC_UNAUTHORIZED, new HttpResponseBody(false, errorMessage), headers);
		} else {
			return new HttpResponse(HttpServletResponse.SC_FORBIDDEN, errorMessage);
		}
    }
    
}
