package io.codibase.server.web.behavior;

import io.codibase.commons.codeassist.FenceAware;
import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.codibase.commons.codeassist.parser.TerminalExpect;
import io.codibase.server.util.usermatch.UserMatchParser;
import io.codibase.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.codibase.server.web.util.SuggestionUtils;

import java.util.List;

import static io.codibase.server.util.usermatch.UserMatchLexer.GROUP;
import static io.codibase.server.util.usermatch.UserMatchLexer.USER;
import static io.codibase.server.web.translation.Translation._T;

public class UserMatchBehavior extends ANTLRAssistBehavior {
	
	public UserMatchBehavior() {
		super(UserMatchParser.class, "userMatch", false);
	}

	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), '(', ')') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						int tokenType = terminalExpect.getState().getLastMatchedToken().getType();
						switch (tokenType) {
							case USER:
								return SuggestionUtils.suggestUsers(matchWith);
							case GROUP:
								return SuggestionUtils.suggestGroups(matchWith); 
							default: 
								return null;
						} 
					}

					@Override
					protected String getFencingDescription() {
						return _T("value needs to be enclosed in parenthesis");
					}
					
				}.suggest(terminalExpect);
			}
		} 
		return null;
	}

}
