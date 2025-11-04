package io.codibase.server.buildspecmodel.inputspec.userchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.codibase.server.util.GroovyUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.service.UserService;
import io.codibase.server.model.User;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.ScriptChoice;

@Editable(order=300, name="Evaluate script to get choices")
public class ScriptingChoices implements ChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ScriptingChoices.class);

	private String scriptName;

	@Editable(description="Groovy script to be evaluated. The return value should be a list of user login names to "
			+ "be used as choices. Check <a href='https://docs.codibase.io/appendix/scripting' target='_blank'>scripting help</a> for details")
	@ScriptChoice
	@OmitName
	@NotEmpty
	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getChoices(boolean allPossible) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("allPossible", allPossible);
		try {
			return ((List<String>) GroovyUtils.evalScriptByName(scriptName, variables))
					.stream()
					.map(it->CodiBase.getInstance(UserService.class).findByName(it))
					.filter(it->it!=null)
					.collect(Collectors.toList());
		} catch (RuntimeException e) {
			if (allPossible) {
				logger.error("Error getting all possible choices", e);
				return new ArrayList<>();
			} else {
				throw e;
			}
		}
	}

}
