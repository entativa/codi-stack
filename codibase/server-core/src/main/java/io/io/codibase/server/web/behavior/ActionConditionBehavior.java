package io.codibase.server.web.behavior;

import static io.codibase.commons.codeassist.AntlrUtils.getLexerRule;
import static io.codibase.server.buildspec.job.action.condition.ActionCondition.checkField;
import static io.codibase.server.buildspec.job.action.condition.ActionConditionLexer.Is;
import static io.codibase.server.buildspec.job.action.condition.ActionConditionLexer.OnBranch;
import static io.codibase.server.buildspec.job.action.condition.ActionConditionLexer.ruleNames;
import static io.codibase.server.model.Build.NAME_BRANCH;
import static io.codibase.server.model.Build.NAME_LOG;
import static io.codibase.server.model.Build.NAME_PROJECT;
import static io.codibase.server.model.Build.NAME_PULL_REQUEST;
import static io.codibase.server.model.Build.NAME_TAG;
import static io.codibase.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.codibase.commons.codeassist.AntlrUtils;
import io.codibase.commons.codeassist.FenceAware;
import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.codibase.commons.codeassist.parser.Element;
import io.codibase.commons.codeassist.parser.ParseExpect;
import io.codibase.commons.codeassist.parser.TerminalExpect;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.buildspec.job.Job;
import io.codibase.server.buildspec.job.JobAware;
import io.codibase.server.buildspec.job.action.condition.ActionCondition;
import io.codibase.server.buildspec.job.action.condition.ActionConditionParser;
import io.codibase.server.buildspec.param.spec.ParamSpec;
import io.codibase.server.model.Build;
import io.codibase.server.model.Project;
import io.codibase.server.search.entity.project.ProjectQuery;
import io.codibase.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.codibase.server.web.util.SuggestionUtils;

public class ActionConditionBehavior extends ANTLRAssistBehavior {

	public ActionConditionBehavior() {
		super(ActionConditionParser.class, "condition", false);
	}

	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Quoted")) {
				return new FenceAware(codeAssist.getGrammar(), '"', '"') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						if ("criteriaField".equals(spec.getLabel())) {
							Map<String, String> fields = new LinkedHashMap<>();
							fields.put(NAME_PROJECT, _T("project of the running job"));
							fields.put(NAME_BRANCH, _T("branch the job is running against"));
							fields.put(NAME_TAG, _T("tag the job is running against"));
							fields.put(NAME_PULL_REQUEST, null);
							fields.put(NAME_LOG, null);
							JobAware jobAware = getComponent().findParent(JobAware.class);
							Job job = jobAware.getJob();
							for (var param: job.getParamSpecs())
								fields.put(param.getName(), null);
							return SuggestionUtils.suggest(fields, matchWith);
						} else if ("criteriaValue".equals(spec.getLabel())) {
							List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
							Preconditions.checkState(operatorElements.size() == 1);
							String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
							int operator = getLexerRule(ruleNames, operatorName);							
							if (operator == OnBranch) {
								return SuggestionUtils.suggestBranches(Project.get(), matchWith);
							} else if (operator == Is) {
								List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
								Preconditions.checkState(fieldElements.size() == 1);
								String fieldName = ActionCondition.getValue(fieldElements.get(0).getMatchedText());
								if (fieldName.equals(NAME_PROJECT)) {
									return SuggestionUtils.suggestProjectPaths(matchWith);
								} else if (fieldName.equals(NAME_BRANCH)) {
									return SuggestionUtils.suggestBranches(Project.get(), matchWith);	
								} else if (fieldName.equals(NAME_TAG)) {
									return SuggestionUtils.suggestTags(Project.get(), matchWith);
								} else {
									JobAware jobAware = getComponent().findParent(JobAware.class);
									Job job = jobAware.getJob();
									ParamSpec paramSpec = job.getParamSpecMap().get(fieldName);
									if (paramSpec != null)
										return SuggestionUtils.suggest(paramSpec.getPossibleValues(), matchWith);
								}
							}
						} 
						return null;
					}
					
					@Override
					protected String getFencingDescription() {
						return _T("value should be quoted");
					}
					
				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (suggestedLiteral.equals(AntlrUtils.getLexerRuleName(ruleNames, OnBranch)))
			return Optional.of(_T("branch the build commit is merged into"));
		parseExpect = parseExpect.findExpectByLabel("operator");
		if (parseExpect != null) {
			List<Element> fieldElements = parseExpect.getState().findMatchedElementsByLabel("criteriaField", false);
			if (!fieldElements.isEmpty()) {
				JobAware jobAware = getComponent().findParent(JobAware.class);
				Job job = jobAware.getJob();
				String fieldName = ActionCondition.getValue(fieldElements.iterator().next().getMatchedText());
				try {
					checkField(job, fieldName, getLexerRule(ruleNames, suggestedLiteral));
				} catch (ExplicitException e) {
					return null;
				}
			}
		}
		return super.describe(parseExpect, suggestedLiteral);
	}
	
	@Override
	protected List<String> getHints(TerminalExpect terminalExpect) {
		List<String> hints = new ArrayList<>();
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if ("criteriaValue".equals(spec.getLabel())) {
				List<Element> fieldElements = terminalExpect.getState().findMatchedElementsByLabel("criteriaField", true);
				if (!fieldElements.isEmpty()) {
					String fieldName = ProjectQuery.getValue(fieldElements.get(0).getMatchedText());
					if (fieldName.equals(NAME_PROJECT) || fieldName.equals(NAME_BRANCH) || fieldName.equals(NAME_TAG))
						hints.add(_T("Use '**', '*' or '?' for <a href='https://docs.codibase.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>"));
					else if (fieldName.equals(Build.NAME_LOG))
						hints.add(_T("Use '*' for wildcard match"));
				} else {
					List<Element> operatorElements = terminalExpect.getState().findMatchedElementsByLabel("operator", true);
					Preconditions.checkState(operatorElements.size() == 1);
					String operatorName = StringUtils.normalizeSpace(operatorElements.get(0).getMatchedText());
					int operator = getLexerRule(ruleNames, operatorName);
					if (operator == OnBranch)
						hints.add(_T("Use '**', '*' or '?' for <a href='https://docs.codibase.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>"));
				}
			}
		} 
		return hints;
	}
	
}
