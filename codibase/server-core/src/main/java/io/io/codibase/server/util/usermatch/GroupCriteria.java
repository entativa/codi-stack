package io.codibase.server.util.usermatch;

import io.codibase.commons.utils.StringUtils;
import io.codibase.server.model.Group;
import io.codibase.server.model.User;

public class GroupCriteria implements UserMatchCriteria {

	private static final long serialVersionUID = 1L;

	private final Group group;
	
	public GroupCriteria(Group group) {
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public boolean matches(User user) {
		return group.getMembers().contains(user);
	}

	@Override
	public String toString() {
		return "group(" + StringUtils.escape(group.getName(), "()") + ")";
	}
	
}
