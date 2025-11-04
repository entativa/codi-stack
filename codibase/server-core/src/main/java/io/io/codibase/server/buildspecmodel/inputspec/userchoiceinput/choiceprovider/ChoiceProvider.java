package io.codibase.server.buildspecmodel.inputspec.userchoiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.codibase.server.model.User;
import io.codibase.server.annotation.Editable;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<User> getChoices(boolean allPossible);
	
}
