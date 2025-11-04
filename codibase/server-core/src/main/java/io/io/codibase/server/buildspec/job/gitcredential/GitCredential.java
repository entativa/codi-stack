package io.codibase.server.buildspec.job.gitcredential;

import com.google.common.collect.Lists;
import io.codibase.k8shelper.CloneInfo;
import io.codibase.server.CodiBase;
import io.codibase.server.ServerConfig;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.ImplementationProvider;
import io.codibase.server.model.Build;

import java.io.Serializable;
import java.util.Collection;

@Editable
@ImplementationProvider("getImplementations")
public interface GitCredential extends Serializable {
	
	CloneInfo newCloneInfo(Build build, String jobToken);
	
	@SuppressWarnings("unused")
	private static Collection<Class<? extends GitCredential>> getImplementations() {
		var implementations = Lists.newArrayList(DefaultCredential.class, HttpCredential.class);
		if (CodiBase.getInstance(ServerConfig.class).getSshPort() != 0)
			implementations.add(SshCredential.class);
		return implementations;
	}
	
}
