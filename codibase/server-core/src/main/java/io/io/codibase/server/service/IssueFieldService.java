package io.codibase.server.service;

import java.util.Collection;

import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueField;

public interface IssueFieldService extends EntityService<IssueField> {
	
	void saveFields(Issue issue);
	
	void onRenameUser(String oldName, String newName);

    void create(IssueField entity);

    void onRenameGroup(String oldName, String newName);
			
	void populateFields(Collection<Issue> issues);
	
}
