package io.codibase.server.service;

import io.codibase.server.model.IssueDescriptionRevision;

public interface IssueDescriptionRevisionService extends EntityService<IssueDescriptionRevision> {

    void create(IssueDescriptionRevision revision);
		
}
