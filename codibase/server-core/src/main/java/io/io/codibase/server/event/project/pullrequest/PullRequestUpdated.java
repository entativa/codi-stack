package io.codibase.server.event.project.pullrequest;

import java.util.Collection;
import java.util.stream.Collectors;

import io.codibase.server.CodiBase;
import io.codibase.server.service.EmailAddressService;
import io.codibase.server.service.PullRequestUpdateService;
import io.codibase.server.model.PullRequestUpdate;
import io.codibase.server.model.User;

public class PullRequestUpdated extends PullRequestEvent {

	private static final long serialVersionUID = 1L;

	private final Long updateId;
	
	private transient Collection<User> committers;
	
	public PullRequestUpdated(PullRequestUpdate update) {
		super(null, update.getDate(), update.getRequest());
		updateId = update.getId();
	}

	public PullRequestUpdate getUpdate() {
		return CodiBase.getInstance(PullRequestUpdateService.class).load(updateId);
	}

	@Override
	public String getActivity() {
		return "added commits";
	}

	public Collection<User> getCommitters() {
		if (committers == null) {
			committers = getUpdate().getCommits()
				.stream()
				.map(it->CodiBase.getInstance(EmailAddressService.class).findByPersonIdent(it.getCommitterIdent()))
				.filter(it -> it!=null && it.isVerified())
				.map(it->it.getOwner())
				.collect(Collectors.toSet());
		}
		return committers;
	}

}
