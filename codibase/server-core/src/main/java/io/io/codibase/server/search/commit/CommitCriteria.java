package io.codibase.server.search.commit;

import java.io.Serializable;

import io.codibase.commons.codeassist.AntlrUtils;
import io.codibase.commons.utils.StringUtils;
import io.codibase.server.event.project.RefUpdated;
import io.codibase.server.git.command.RevListOptions;
import io.codibase.server.model.Project;

public abstract class CommitCriteria implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract void fill(Project project, RevListOptions options);
	
	public abstract boolean matches(RefUpdated event);
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(CommitQueryLexer.ruleNames, rule).replace(' ', '-');
	}
	
	public static String parens(String value) {
		return "(" + StringUtils.escape(value, "()") + ")";
	}
	
}
