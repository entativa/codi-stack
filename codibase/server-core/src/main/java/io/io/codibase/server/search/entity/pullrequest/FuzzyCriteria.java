package io.codibase.server.search.entity.pullrequest;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;

import io.codibase.commons.utils.StringUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.model.PullRequest;
import io.codibase.server.search.entitytext.PullRequestTextService;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class FuzzyCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private static final int MAX_TEXT_QUERY_COUNT = 1000;

	private final String value;

	private transient List<Long> requestIds;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		if (requestIds == null) {
			if (value.length() == 0)
				return builder.conjunction();
			var project = projectScope!=null? projectScope.getProject(): null;
			requestIds = CodiBase.getInstance(PullRequestTextService.class).query(project, value, MAX_TEXT_QUERY_COUNT);
		}
		if (requestIds.isEmpty())
			return builder.disjunction();
		else
			return builder.in(from.get(PullRequest.PROP_ID)).value(requestIds);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (value.length() == 0)
			return true;
		else
			return CodiBase.getInstance(PullRequestTextService.class).matches(request, value);
	}

	@Override
	public List<Order> getPreferOrders(CriteriaBuilder builder, From<PullRequest, PullRequest> from) {
		if (requestIds != null && !requestIds.isEmpty()) {
			var orders = new ArrayList<Order>();
			var orderCase = builder.selectCase();
			for (int i = 0; i < requestIds.size(); i++) 
				orderCase.when(builder.equal(from.get(PullRequest.PROP_ID), requestIds.get(i)), i);
			orders.add(builder.asc(orderCase.otherwise(requestIds.size())));
			return orders;
		} else {
			return new ArrayList<>();
		}
	}	

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
