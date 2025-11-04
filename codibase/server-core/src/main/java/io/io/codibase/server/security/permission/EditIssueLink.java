package io.codibase.server.security.permission;

import io.codibase.server.model.LinkSpec;
import io.codibase.server.util.facade.UserFacade;
import org.apache.shiro.authz.Permission;

import org.jspecify.annotations.Nullable;

public class EditIssueLink implements BasePermission {

	private final LinkSpec link;
	
	public EditIssueLink(@Nullable LinkSpec link) {
		this.link = link;
	}
	
	@Override
	public boolean implies(Permission p) {
		if (p instanceof EditIssueLink) {
			EditIssueLink editIssueLink = (EditIssueLink) p;
			return link == null || link.equals(editIssueLink.link);
		} else {
			return new AccessProject().implies(p);
		}
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return user != null;
	}
}
