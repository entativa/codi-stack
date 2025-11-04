package io.codibase.server.buildspec.job.gitcredential;

import io.codibase.k8shelper.CloneInfo;
import io.codibase.k8shelper.DefaultCloneInfo;
import io.codibase.server.CodiBase;
import io.codibase.server.web.UrlService;
import io.codibase.server.model.Build;
import io.codibase.server.annotation.Editable;

@Editable(name="Default", order=100)
public class DefaultCredential implements GitCredential {

	private static final long serialVersionUID = 1L;

	@Override
	public CloneInfo newCloneInfo(Build build, String jobToken) {
		return new DefaultCloneInfo(CodiBase.getInstance(UrlService.class).cloneUrlFor(build.getProject(), false), jobToken);
	}

}
