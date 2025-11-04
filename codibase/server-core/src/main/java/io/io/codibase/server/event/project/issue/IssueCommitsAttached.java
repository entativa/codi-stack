package io.codibase.server.event.project.issue;

import java.util.Date;

import io.codibase.server.CodiBase;
import io.codibase.server.service.UserService;
import io.codibase.server.model.Issue;

public class IssueCommitsAttached extends IssueEvent {

	private static final long serialVersionUID = 1L;
	
	public IssueCommitsAttached(Issue issue) {
		super(CodiBase.getInstance(UserService.class).getSystem(), new Date(), issue);
	}

	@Override
	public boolean affectsListing() {
		return false;
	}
	
	@Override
	public String getActivity() {
		return "commits attached";
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
}
