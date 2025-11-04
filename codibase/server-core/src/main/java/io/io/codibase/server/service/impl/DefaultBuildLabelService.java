package io.codibase.server.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.common.base.Preconditions;

import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.Build;
import io.codibase.server.model.BuildLabel;
import io.codibase.server.model.LabelSpec;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.service.BuildLabelService;

@Singleton
public class DefaultBuildLabelService extends BaseEntityLabelService<BuildLabel> implements BuildLabelService {

	@Override
	protected BuildLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new BuildLabel();
		label.setBuild((Build) entity);
		label.setSpec(spec);
		return label;
	}

	@Override
	public void create(BuildLabel buildLabel) {
		Preconditions.checkState(buildLabel.isNew());
		dao.persist(buildLabel);
	}

	@Sessional
	@Override
	public void populateLabels(List<Build> builds) {
		var builder = getSession().getCriteriaBuilder();
		CriteriaQuery<BuildLabel> labelQuery = builder.createQuery(BuildLabel.class);
		Root<BuildLabel> labelRoot = labelQuery.from(BuildLabel.class);
		labelQuery.select(labelRoot);
		labelQuery.where(labelRoot.get(BuildLabel.PROP_BUILD).in(builds));

		for (var build: builds)
			build.setLabels(new ArrayList<>());

		for (var label: getSession().createQuery(labelQuery).getResultList())
			label.getBuild().getLabels().add(label);
	}

}