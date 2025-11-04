package io.codibase.server.service;

import io.codibase.server.model.IssueVote;

public interface IssueVoteService extends EntityService<IssueVote> {

    void create(IssueVote vote);
	
}
