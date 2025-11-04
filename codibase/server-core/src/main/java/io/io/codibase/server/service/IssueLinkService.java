package io.codibase.server.service;

import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueLink;
import io.codibase.server.model.LinkSpec;

import java.util.Collection;

public interface IssueLinkService extends EntityService<IssueLink> {

	void syncLinks(LinkSpec spec, Issue issue, Collection<Issue> linkedIssues, boolean opposite);

    void create(IssueLink link);

    void populateLinks(Collection<Issue> issues);
	
	void loadDeepLinks(Issue issue);
	
}
