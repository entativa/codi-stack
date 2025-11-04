package io.codibase.server.util.oauth;

import java.io.Serializable;

public interface RefreshTokenAccessor extends Serializable {

    String getRefreshToken();

    void setRefreshToken(String refreshToken);

}
