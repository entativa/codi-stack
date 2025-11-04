package io.codibase.server.web.page.project.issues.iteration;

import io.codibase.server.model.Iteration;
import io.codibase.server.model.Project;
import io.codibase.server.search.entity.issue.IssueQuery;
import io.codibase.server.search.entity.issue.IterationCriteria;
import io.codibase.server.search.entity.issue.StateCriteria;
import io.codibase.server.web.component.issue.list.IssueListPanel;
import io.codibase.server.web.component.issue.statestats.StateStatsBar;
import io.codibase.server.web.component.link.ViewStateAwarePageLink;
import io.codibase.server.web.util.paginghistory.PagingHistorySupport;
import io.codibase.server.web.util.paginghistory.ParamPagingHistorySupport;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import static io.codibase.server.search.entity.issue.IssueQueryLexer.Is;

public class IterationIssuesPage extends IterationDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_PAGE = "page";
	
	private String query;
	
	private IssueListPanel issueList;
	
	public IterationIssuesPage(PageParameters params) {
		super(params);
		
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new StateStatsBar("issueStats", new LoadableDetachableModel<Map<String, Integer>>() {

			@Override
			protected Map<String, Integer> load() {
				return getIteration().getStateStats(getProject());
			}
			
		}) {

			@Override
			protected Link<Void> newStateLink(String componentId, String state) {
				String query = new IssueQuery(new StateCriteria(state, Is)).toString();
				PageParameters params = paramsOf(getProject(), getIteration(), query);
				return new ViewStateAwarePageLink<Void>(componentId, IterationIssuesPage.class, params);
			}
			
		});
		
		add(issueList = new IssueListPanel("issues", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
				PageParameters params = getPageParameters();
				params.set(PARAM_QUERY, query);
				params.remove(PARAM_PAGE);
				CharSequence url = RequestCycle.get().urlFor(IterationIssuesPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {
			
			@Override
			protected IssueQuery getBaseQuery() {
				return new IssueQuery(new IterationCriteria(getIteration().getName(), Is), new ArrayList<>());
			}
	
			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new ParamPagingHistorySupport() {
					
					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getProject(), getIteration(), query);
						params.add(PARAM_PAGE, currentPage+1);
						return params;
					}
					
					@Override
					public int getCurrentPage() {
						return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
					}
					
				};
			}
	
			@Override
			protected Project getProject() {
				return IterationIssuesPage.this.getProject();
			}
			
		});
		
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		add(issueList);
	}
	
	public static PageParameters paramsOf(Project project, Iteration iteration, @Nullable String query) {
		PageParameters params = paramsOf(project, iteration);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
