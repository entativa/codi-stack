package io.codibase.server.web.component.issue.progress;

import com.google.common.collect.Sets;
import io.codibase.server.CodiBase;
import io.codibase.server.service.StopwatchService;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Stopwatch;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.behavior.ChangeObserver;
import io.codibase.server.web.behavior.CompletionRateBehavior;
import io.codibase.server.web.component.floating.FloatingPanel;
import io.codibase.server.web.component.link.DropdownLink;
import io.codibase.server.web.util.WicketUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Collection;

public abstract class IssueProgressPanel extends Panel {

	private final IModel<Stopwatch> stopwatchModel = new LoadableDetachableModel<>() {
		@Override
		protected Stopwatch load() {
			var user = SecurityUtils.getAuthUser();
			if (user != null)
				return getStopWatchManager().find(user, getIssue());
			else
				return null;
		}
	};
	
	public IssueProgressPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new DropdownLink("completionRate") {
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new TimingDetailPanel(id) {
					@Override
					protected Issue getIssue() {
						return IssueProgressPanel.this.getIssue();
					}

					@Override
					protected void onWorkStarted(AjaxRequestTarget target, Stopwatch stopwatch) {
						stopwatchModel.setObject(stopwatch);
						target.add(IssueProgressPanel.this);
					}

					@Override
					protected void closeDropdown() {
						dropdown.close();
					}

				};
			}
		}.add(new CompletionRateBehavior() {
			@Override
			protected long getTotal() {
				return getIssue().getTotalEstimatedTime();
			}

			@Override
			protected long getCompleted() {
				return getIssue().getTotalSpentTime();
			}
		}));
		
		add(new WebMarkupContainer("workingTime") {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				var script = String.format("codibase.server.issueProgress.onWorkingTimeDomReady('%s', %d);", 
						getMarkupId(), System.currentTimeMillis() - getStopWatch().getDate().getTime());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getStopWatch() != null);
			}
		}.setOutputMarkupId(true));
		
		add(new AjaxLink<Void>("stopWork") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				getStopWatchManager().stopWork(getStopWatch());
				stopwatchModel.setObject(null);
				target.add(IssueProgressPanel.this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getStopWatch() != null);
			}
		});

		add(new ChangeObserver() {
			@Override
			protected Collection<String> findObservables() {
				return Sets.newHashSet(Issue.getDetailChangeObservable(getIssue().getId()));
			}
		});
		
	}

	private Stopwatch getStopWatch() {
		return stopwatchModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		stopwatchModel.detach();
		super.onDetach();
	}

	private StopwatchService getStopWatchManager() {
		return CodiBase.getInstance(StopwatchService.class);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getIssue().getProject().isTimeTracking() 
				&& WicketUtils.isSubscriptionActive() 
				&& SecurityUtils.canAccessTimeTracking(getIssue().getProject()));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProgressResourceReference()));
	}

	protected abstract Issue getIssue();
	
}
