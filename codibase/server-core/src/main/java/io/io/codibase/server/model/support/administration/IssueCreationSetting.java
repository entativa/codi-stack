package io.codibase.server.model.support.administration;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.match.PathMatcher;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.FieldNamesProvider;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.Patterns;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.Project;
import io.codibase.server.model.support.issue.field.instance.FieldInstance;
import io.codibase.server.util.patternset.PatternSet;
import io.codibase.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.codibase.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.codibase.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.codibase.server.web.util.SuggestionUtils;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.*;

@Editable
public class IssueCreationSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String applicableProjects;
	
	private boolean confidential = true;
	
	private List<FieldInstance> issueFields = new ArrayList<>();
	
	@Editable(order=150, placeholder="Any project", description="Specify space-separated projects applicable for this entry. "
			+ "Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to "
			+ "match all projects")
	@Patterns(suggester="suggestProjects")
	public String getApplicableProjects() {
		return applicableProjects;
	}

	public void setApplicableProjects(String applicableProjects) {
		this.applicableProjects = applicableProjects;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}
	
	@Editable(order=200, description="Whether or not created issue should be confidential")
	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	@Editable(order=300)
	@FieldNamesProvider("getFieldNames")
	@OmitName
	@Valid
	public List<FieldInstance> getIssueFields() {
		return issueFields;
	}

	public void setIssueFields(List<FieldInstance> issueFields) {
		this.issueFields = issueFields;
	}
	
	@SuppressWarnings("unused")
	private static Collection<String> getFieldNames() {
		return CodiBase.getInstance(SettingService.class).getIssueSetting().getFieldNames();
	}
	
	public boolean isProjectAuthorized(Project project) {
		return applicableProjects == null 
				|| PatternSet.parse(applicableProjects).matches(new PathMatcher(), project.getPath());
	}
	
	public Set<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		for (FieldInstance instance: getIssueFields()) 
			undefinedFields.addAll(instance.getUndefinedFields());
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		for (FieldInstance instance: getIssueFields()) 
			undefinedFieldValues.addAll(instance.getUndefinedFieldValues());
		return undefinedFieldValues;
	}
	
	public void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Iterator<FieldInstance> it = getIssueFields().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFields(resolutions))
				it.remove();
		}
	}

	public void fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (Iterator<FieldInstance> it = getIssueFields().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFieldValues(resolutions))
				it.remove();
		}
	}
	
}