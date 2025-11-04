package io.codibase.server.buildspecmodel.inputspec.choiceinput.choiceprovider;

import java.io.Serializable;
import java.util.Map;

import io.codibase.server.util.usage.Usage;
import io.codibase.server.annotation.Editable;

@Editable
public abstract class ChoiceProvider implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract Map<String, String> getChoices(boolean allPossible);

	public void onMoveProject(String oldPath, String newPath) {
	}
	
	public Usage onDeleteProject(String projectPath) {
		return new Usage();
	}
	
}
