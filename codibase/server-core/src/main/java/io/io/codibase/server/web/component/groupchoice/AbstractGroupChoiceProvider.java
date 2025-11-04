package io.codibase.server.web.component.groupchoice;

import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.codibase.server.CodiBase;
import io.codibase.server.service.GroupService;
import io.codibase.server.model.Group;
import io.codibase.server.web.component.select2.ChoiceProvider;

public abstract class AbstractGroupChoiceProvider extends ChoiceProvider<Group> {

	private static final long serialVersionUID = 1L;

	@Override
	public void toJson(Group choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(HtmlEscape.escapeHtml5(choice.getName()));
	}

	@Override
	public Collection<Group> toChoices(Collection<String> ids) {
		List<Group> groups = Lists.newArrayList();
		GroupService groupService = CodiBase.getInstance(GroupService.class);
		for (String each : ids) {
			Group group = groupService.load(Long.valueOf(each));
			Hibernate.initialize(group);
			groups.add(group);
		}

		return groups;
	}

}