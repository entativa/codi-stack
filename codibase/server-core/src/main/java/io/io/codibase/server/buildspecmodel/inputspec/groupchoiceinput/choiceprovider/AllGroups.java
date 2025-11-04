package io.codibase.server.buildspecmodel.inputspec.groupchoiceinput.choiceprovider;

import java.util.Comparator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;
import io.codibase.server.CodiBase;
import io.codibase.server.service.GroupService;
import io.codibase.server.model.Group;
import io.codibase.server.annotation.Editable;

@Editable(order=100, name="All groups")
public class AllGroups implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<Group> getChoices(boolean allPossible) {
		List<Group> groups = CodiBase.getInstance(GroupService.class).query();
		Collections.sort(groups, new Comparator<Group>() {

			@Override
			public int compare(Group o1, Group o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		return groups;
	}

}
