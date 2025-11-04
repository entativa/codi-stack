package io.codibase.server.buildspecmodel.inputspec.userchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Sets;

import io.codibase.server.CodiBase;
import io.codibase.server.service.UserService;
import io.codibase.server.model.User;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.facade.UserCache;
import io.codibase.server.annotation.Editable;
import io.codibase.server.web.page.project.issues.detail.IssueDetailPage;
import io.codibase.server.web.util.WicketUtils;

@Editable(order=130, name="All users")
public class AllUsers implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<User> getChoices(boolean allPossible) {
		UserCache cache = CodiBase.getInstance(UserService.class).cloneCache();
		
		if (WicketUtils.getPage() instanceof IssueDetailPage) {
			IssueDetailPage issueDetailPage = (IssueDetailPage) WicketUtils.getPage();
			List<User> users = new ArrayList<>(cache.getUsers());
			users.sort(cache.comparingDisplayName(issueDetailPage.getIssue().getParticipants()));
			return users;
		} else if (SecurityUtils.getAuthUser() != null) {
			List<User> users = new ArrayList<>(cache.getUsers());
			users.sort(cache.comparingDisplayName(Sets.newHashSet(SecurityUtils.getAuthUser())));
			return users;
		} else {
			List<User> users = new ArrayList<>(cache.getUsers());
			users.sort(cache.comparingDisplayName(Sets.newHashSet()));
			return users;
		}
	}

}
