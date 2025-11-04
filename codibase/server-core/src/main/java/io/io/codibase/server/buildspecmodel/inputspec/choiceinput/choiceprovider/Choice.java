package io.codibase.server.buildspecmodel.inputspec.choiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.server.annotation.Color;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Patterns;
import io.codibase.server.web.util.SuggestionUtils;

@Editable(name="Value")
public class Choice implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;
	
	private String color = "#0d87e9";
	
	private String applicableProjects;

	@Editable(order=100)
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=200)
	@NotEmpty
	@Color
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Editable(order=300, placeholder="All projects", description="Specify applicable projects separated by space. "
			+ "Use '**', '*' or '?' for <a href='https://docs.codibase.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty for all projects")
	@Patterns(suggester="suggestProjects", path=true)
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
	
}
