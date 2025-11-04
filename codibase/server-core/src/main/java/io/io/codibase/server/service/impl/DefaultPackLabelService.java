package io.codibase.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.common.base.Preconditions;

import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.LabelSpec;
import io.codibase.server.model.Pack;
import io.codibase.server.model.PackLabel;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.service.PackLabelService;

@Singleton
public class DefaultPackLabelService extends BaseEntityLabelService<PackLabel> implements PackLabelService {

	@Override
	protected PackLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new PackLabel();
		label.setPack((Pack) entity);
		label.setSpec(spec);
		return label;
	}

	@Override
	public void create(PackLabel packLabel) {
		Preconditions.checkState(packLabel.isNew());
		dao.persist(packLabel);
	}

	@Sessional
	@Override
	public void populateLabels(Collection<Pack> packs) {
		var builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PackLabel> labelQuery = builder.createQuery(PackLabel.class);
		Root<PackLabel> labelRoot = labelQuery.from(PackLabel.class);
		labelQuery.select(labelRoot);
		labelQuery.where(labelRoot.get(PackLabel.PROP_PACK).in(packs));

		for (var pack: packs)
			pack.setLabels(new ArrayList<>());

		for (var label: getSession().createQuery(labelQuery).getResultList())
			label.getPack().getLabels().add(label);
	}

}