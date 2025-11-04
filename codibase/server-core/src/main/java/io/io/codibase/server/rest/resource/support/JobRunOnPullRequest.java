package io.codibase.server.rest.resource.support;

import javax.validation.constraints.NotNull;

import io.codibase.server.model.Build;
import io.codibase.server.rest.annotation.Api;
import io.codibase.server.rest.annotation.EntityCreate;

@EntityCreate(Build.class)
public class JobRunOnPullRequest extends JobRun {
	
	private static final long serialVersionUID = 1L;

	@Api(order=100, description="CodiBase will build against merge preview commit of this pull request")
	private Long pullRequestId;
	
	@NotNull
	public Long getPullRequestId() {
		return pullRequestId;
	}

	public void setPullRequestId(Long pullRequestId) {
		this.pullRequestId = pullRequestId;
	}

}