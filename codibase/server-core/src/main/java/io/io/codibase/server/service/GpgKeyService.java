package io.codibase.server.service;

import org.jspecify.annotations.Nullable;

import io.codibase.server.git.signatureverification.gpg.GpgSigningKey;
import io.codibase.server.model.GpgKey;

public interface GpgKeyService extends EntityService<GpgKey> {

    @Nullable
	GpgSigningKey findSigningKey(long keyId);

    void create(GpgKey gpgKey);
}
