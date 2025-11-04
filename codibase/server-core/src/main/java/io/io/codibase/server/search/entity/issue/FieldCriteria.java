package io.codibase.server.search.entity.issue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.google.common.base.Preconditions;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueField;
import io.codibase.server.model.support.administration.GlobalIssueSetting;
import io.codibase.server.model.support.issue.field.spec.FieldSpec;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;
import io.codibase.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;

public abstract class FieldCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private String fieldName;
	
	public FieldCriteria(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public final Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueField> fieldQuery = query.subquery(IssueField.class);
		Root<IssueField> fieldRoot = fieldQuery.from(IssueField.class);
		fieldQuery.select(fieldRoot);

		Predicate issuePredicate = builder.equal(fieldRoot.get(IssueField.PROP_ISSUE), from);
		Predicate namePredicate = builder.equal(fieldRoot.get(IssueField.PROP_NAME), getFieldName());
		Predicate valuePredicate = getValuePredicate(from, fieldRoot, builder);
		if (valuePredicate != null) {
			return builder.exists(fieldQuery.where(issuePredicate, namePredicate, valuePredicate));
		} else {
			return builder.not(builder.exists(fieldQuery.where(
					issuePredicate,
					namePredicate,
					builder.isNotNull(fieldRoot.get(IssueField.PROP_VALUE)))));
		}
	}

	/**
	 * @return predicate of field value. <tt>null</tt> to indicate that this field is empty   
	 */
	@Nullable
	protected abstract Predicate getValuePredicate(From<Issue, Issue> issue, From<IssueField, IssueField> field, CriteriaBuilder builder);
	
	@Override
	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		GlobalIssueSetting issueSetting = CodiBase.getInstance(SettingService.class).getIssueSetting();
		if (!Issue.QUERY_FIELDS.contains(fieldName) 
				&& issueSetting.getFieldSpec(fieldName) == null) {
			undefinedFields.add(fieldName);
		}
		return undefinedFields;
	}
	
	@Override
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				if (entry.getKey().equals(fieldName))
					fieldName = entry.getValue().getNewField();
			} else if (entry.getKey().equals(fieldName)) {
				return false;
			}
		}
		return true;
	}

	public FieldSpec getFieldSpec() {
		SettingService settingService = CodiBase.getInstance(SettingService.class);
		return Preconditions.checkNotNull(settingService.getIssueSetting().getFieldSpec(fieldName));
	}
	
}
