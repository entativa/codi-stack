package io.codibase.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.common.base.Preconditions;

import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.LabelSpec;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestLabel;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.service.PullRequestLabelService;

@Singleton
public class DefaultPullRequestLabelService extends BaseEntityLabelService<PullRequestLabel>
		implements PullRequestLabelService {

	@Override
	protected PullRequestLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new PullRequestLabel();
		label.setRequest((PullRequest) entity);
		label.setSpec(spec);
		return label;
	}

	@Override
	public void create(PullRequestLabel pullRequestLabel) {
		Preconditions.checkState(pullRequestLabel.isNew());
		dao.persist(pullRequestLabel);
	}

	@Sessional
	@Override
	public void populateLabels(Collection<PullRequest> pullRequests) {
		var builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PullRequestLabel> labelQuery = builder.createQuery(PullRequestLabel.class);
		Root<PullRequestLabel> labelRoot = labelQuery.from(PullRequestLabel.class);
		labelQuery.select(labelRoot);
		labelQuery.where(labelRoot.get(PullRequestLabel.PROP_REQUEST).in(pullRequests));

		for (var pullRequest: pullRequests)
			pullRequest.setLabels(new ArrayList<>());

		for (var label: getSession().createQuery(labelQuery).getResultList())
			label.getRequest().getLabels().add(label);
	}

}