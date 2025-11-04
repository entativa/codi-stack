package io.codibase.server.event.project.issue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.codibase.server.CodiBase;
import io.codibase.server.buildspecmodel.inputspec.Input;
import io.codibase.server.service.GroupService;
import io.codibase.server.service.UserService;
import io.codibase.server.model.Group;
import io.codibase.server.model.Issue;
import io.codibase.server.model.User;
import io.codibase.server.model.support.issue.field.spec.FieldSpec;
import io.codibase.server.util.CommitAware;
import io.codibase.server.util.ProjectScopedCommit;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;

public class IssueOpened extends IssueEvent implements CommitAware {

	private static final long serialVersionUID = 1L;

	private final Collection<String> notifiedEmailAddresses;
	
	public IssueOpened(Issue issue, Collection<String> notifiedEmailAddresses) {
		super(issue.getSubmitter(), issue.getSubmitDate(), issue);
		this.notifiedEmailAddresses = notifiedEmailAddresses;
	}

	@Override
	protected CommentText newCommentText() {
		return getIssue().getDescription()!=null? new MarkdownText(getProject(), getIssue().getDescription()): null;
	}

	@Override
	public boolean affectsListing() {
		return true;
	}

	public Collection<String> getNotifiedEmailAddresses() {
		return notifiedEmailAddresses;
	}
	
	@Override
	public Map<String, Collection<User>> getNewUsers() {
		Map<String, Collection<User>> newUsers = new HashMap<>();
		UserService userService = CodiBase.getInstance(UserService.class);
		for (Input field: getIssue().getFieldInputs().values()) {
			if (field.getType().equals(FieldSpec.USER)) {
				Set<User> usersOfField = field.getValues()
						.stream()
						.map(it->userService.findByName(it))
						.filter(it->it!=null)
						.collect(Collectors.toSet());
				if (!usersOfField.isEmpty())
					newUsers.put(field.getName(), usersOfField);
			} 
		}
		return newUsers;
	}

	@Override
	public Map<String, Group> getNewGroups() {
		Map<String, Group> newGroups = new HashMap<>();
		GroupService groupService = CodiBase.getInstance(GroupService.class);
		for (Input field: getIssue().getFieldInputs().values()) {
			if (field.getType().equals(FieldSpec.GROUP)) {
				if (!field.getValues().isEmpty()) {
					Group newGroup = groupService.find(field.getValues().iterator().next());
					if (newGroup != null)
						newGroups.put(field.getName(), newGroup);
				}
			} 
		}
		return newGroups;
	}

	@Override
	public String getActivity() {
		return "opened";
	}

	@Override
	public ProjectScopedCommit getCommit() {
		var project = getIssue().getProject();
		if (project.getDefaultBranch() != null)
			return new ProjectScopedCommit(project, project.getObjectId(project.getDefaultBranch(), true));
		else
			return null;
	}

}