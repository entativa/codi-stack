package io.codibase.server.buildspecmodel.inputspec.groupchoiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.codibase.server.model.Group;
import io.codibase.server.annotation.Editable;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<Group> getChoices(boolean allPossible);
	
}
