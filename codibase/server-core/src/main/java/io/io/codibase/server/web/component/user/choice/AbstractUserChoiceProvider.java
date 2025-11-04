package io.codibase.server.web.component.user.choice;

import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.codibase.server.CodiBase;
import io.codibase.server.service.UserService;
import io.codibase.server.model.User;
import io.codibase.server.web.avatar.AvatarService;
import io.codibase.server.web.component.select2.ChoiceProvider;

public abstract class AbstractUserChoiceProvider extends ChoiceProvider<User> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void toJson(User choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(HtmlEscape.escapeHtml5(choice.getDisplayName().trim()));
		String avatarUrl = CodiBase.getInstance(AvatarService.class).getUserAvatarUrl(choice.getId());
		writer.key("avatar").value(avatarUrl);
	}

	@Override
	public Collection<User> toChoices(Collection<String> ids) {
		List<User> users = Lists.newArrayList();
		UserService userService = CodiBase.getInstance(UserService.class);
		for (String each : ids) {
			User user = userService.load(Long.valueOf(each)); 
			Hibernate.initialize(user);
			users.add(user);
		}

		return users;
	}

}