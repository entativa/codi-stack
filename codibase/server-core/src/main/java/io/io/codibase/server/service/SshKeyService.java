package io.codibase.server.service;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.SshKey;
import io.codibase.server.model.User;

public interface SshKeyService extends EntityService<SshKey> {

    @Nullable
    SshKey findByFingerprint(String fingerprint);
    
    void syncSshKeys(User user, Collection<String> sshKeys);

    void create(SshKey sshKey);
	
}
