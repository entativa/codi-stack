package io.codibase.server.search.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.Path;
import javax.validation.ValidationException;

import com.google.common.base.Splitter;

import io.codibase.commons.codeassist.FenceAware;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.commons.utils.StringUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.IssueService;
import io.codibase.server.service.IterationService;
import io.codibase.server.service.LabelSpecService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.service.PullRequestService;
import io.codibase.server.service.SettingService;
import io.codibase.server.service.UserService;
import io.codibase.server.entityreference.BuildReference;
import io.codibase.server.entityreference.IssueReference;
import io.codibase.server.entityreference.PullRequestReference;
import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.Build;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Iteration;
import io.codibase.server.model.LabelSpec;
import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;
import io.codibase.server.util.DateUtils;
import io.codibase.server.util.ProjectScopedCommit;
import io.codibase.server.util.ProjectScopedRevision;
import io.codibase.server.util.criteria.Criteria;

public abstract class EntityQuery<T extends AbstractEntity> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Pattern INSIDE_QUOTE = Pattern.compile("\"([^\"\\\\]|\\\\.)*");

	private Criteria<T> criteria;

	private List<EntitySort> sorts;

	private List<EntitySort> baseSorts;

	public EntityQuery(@Nullable Criteria<T> criteria, List<EntitySort> sorts) {
		this(criteria, sorts, new ArrayList<>());
	}

	public EntityQuery(@Nullable Criteria<T> criteria, List<EntitySort> sorts, List<EntitySort> baseSorts) {
		this.criteria = criteria;
		this.sorts = sorts;
		this.baseSorts = baseSorts;
	}

	@Nullable
	public Criteria<T> getCriteria() {
		return criteria;
	}

	public void setCriteria(@Nullable Criteria<T> criteria) {
		this.criteria = criteria;
	}

	public List<EntitySort> getSorts() {
		return sorts;
	}

	public void setSorts(List<EntitySort> sorts) {
		this.sorts = sorts;
	}

	public List<EntitySort> getBaseSorts() {
		return baseSorts;
	}

	public void setBaseSorts(List<EntitySort> baseSorts) {
		this.baseSorts = baseSorts;
	}
	
	public static String getValue(String token) {
		return StringUtils.unescape(FenceAware.unfence(token));
	}
	
	public static int getIntValue(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new ExplicitException("Invalid number: " + value);
		}
	}

	public static float getFloatValue(String value) {
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			throw new ExplicitException("Invalid decimal: " + value);
		}
	}
	
	public static LabelSpec getLabelSpec(String labelName) {
		var labelSpec = CodiBase.getInstance(LabelSpecService.class).find(labelName);
		if (labelSpec != null) 
			return labelSpec;
		else
			throw new ExplicitException("Undefined label: " + labelName);
	}
	
	public static int getWorkingPeriodValue(String value) {
		try {
			var timeTrackingSetting = CodiBase.getInstance(SettingService.class).getIssueSetting().getTimeTrackingSetting();
			return timeTrackingSetting.parseWorkingPeriod(value);
		} catch (ValidationException e) {
			throw new ExplicitException("Invalid working period: " + value);
		}
	}
	
	public static long getLongValue(String value) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new ExplicitException("Invalid number: " + value);
		}
	}
	
	public static User getUser(String loginName) {
		User user = CodiBase.getInstance(UserService.class).findByName(loginName);
		if (user == null)
			throw new ExplicitException("Unable to find user with login: " + loginName);
		return user;
	}
	
	public static Project getProject(String projectPath) {
		Project project = CodiBase.getInstance(ProjectService.class).findByPath(projectPath);
		if (project == null)
			throw new ExplicitException("Unable to find project '" + projectPath + "'");
		return project;
	}
	
	public static boolean getBooleanValue(String value) {
		if (value.equals("true"))
			return true;
		else if (value.equals("false"))
			return false;
		else
			throw new ExplicitException("Invalid boolean: " + value);
	}
	
	public static Date getDateValue(String value) {
		Date dateValue = DateUtils.parseRelaxed(value);
		if (dateValue == null)
			throw new ExplicitException("Unrecognized date: " + value);
		return dateValue;
	}

	public static ProjectScopedCommit getCommitId(@Nullable Project project, String value) {
		if (project != null && !value.contains(":"))
			value = project.getPath() + ":" + value;
		ProjectScopedCommit commitId = ProjectScopedCommit.from(value);
		if (commitId != null && commitId.getCommitId() != null)
			return commitId;
		else
			throw new ExplicitException("Unable to find revision: " + value);
	}

	public static ProjectScopedRevision getRevision(@Nullable Project project, String value) {
		if (project != null && !value.contains(":"))
			value = project.getPath() + ":" + value;
		ProjectScopedRevision revision = ProjectScopedRevision.from(value);
		if (revision != null)
			return revision;
		else
			throw new ExplicitException("Unable to find revision: " + value);
	}
	
	public static Issue getIssue(@Nullable Project project, String value) {
		var reference = IssueReference.of(value, project);
		var issue = CodiBase.getInstance(IssueService.class).find(reference.getProject(), reference.getNumber());
		if (issue != null)
			return issue;
		else
			throw new ExplicitException("Unable to find issue: " + value);
	}
	
	public static PullRequest getPullRequest(@Nullable Project project, String value) {
		var reference = PullRequestReference.of(value, project);
		var pullRequest = CodiBase.getInstance(PullRequestService.class).find(reference.getProject(), reference.getNumber());
		if (pullRequest != null)
			return pullRequest;
		else
			throw new ExplicitException("Unable to find pull request: " + value);
	}
	
	public static Build getBuild(@Nullable Project project, String value) {
		var reference = BuildReference.of(value, project);
		var build = CodiBase.getInstance(BuildService.class).find(reference.getProject(), reference.getNumber());
		if (build != null)
			return build;
		else
			throw new ExplicitException("Unable to find build: " + value);
	}
	
	public static Iteration getIteration(@Nullable Project project, String value) {
		if (project != null && !value.contains(":")) 
			value = project.getPath() + ":" + value;
		Iteration iteration = CodiBase.getInstance(IterationService.class).findInHierarchy(value);
		if (iteration != null)
			return iteration;
		else
			throw new ExplicitException("Unable to find iteration: " + value);
	}
	
	public boolean matches(T entity) {
		return getCriteria() == null || getCriteria().matches(entity);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (getCriteria() != null) 
			builder.append(getCriteria().toString()).append(" ");
		if (!getSorts().isEmpty()) {
			builder.append("order by ");
			builder.append(getSorts().stream().map(it->it.toString()).collect(Collectors.joining(", ")));
		}
		String toStringValue = builder.toString().trim();
		if (toStringValue.length() == 0)
			toStringValue = null;
		return toStringValue;
	}
	
	public static <T> Path<T> getPath(Path<?> root, String pathName) {
		int index = pathName.indexOf('.');
		if (index != -1) {
			Path<T> path = root.get(pathName.substring(0, index));
			for (String field: Splitter.on(".").split(pathName.substring(index+1))) 
				path = path.get(field);
			return path;
		} else {
			return root.get(pathName);
		}
	}
	
	public EntityQuery<T> onMoveProject(String oldPath, String newPath) {
		if (getCriteria() != null)
			getCriteria().onMoveProject(oldPath, newPath);
		return this;
	}
	
	public boolean isUsingProject(String projectPath) {
		if (getCriteria() != null)
			return getCriteria().isUsingProject(projectPath);
		else
			return false;
	}
	
	public static boolean isInsideQuote(String value) {
		return INSIDE_QUOTE.matcher(value.trim()).matches();
	}
	
}
