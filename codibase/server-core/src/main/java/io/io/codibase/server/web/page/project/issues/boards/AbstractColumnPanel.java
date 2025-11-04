package io.codibase.server.web.page.project.issues.boards;

import java.util.HashSet;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.visit.IVisitor;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueChangeService;
import io.codibase.server.service.IssueService;
import io.codibase.server.service.IterationService;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Iteration;
import io.codibase.server.model.Project;
import io.codibase.server.model.support.administration.GlobalIssueSetting;
import io.codibase.server.search.entity.issue.IssueQuery;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.EditContext;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.codibase.server.web.page.base.BasePage;

abstract class AbstractColumnPanel extends Panel implements EditContext {

	protected final IModel<Integer> countModel = new LoadableDetachableModel<>() {

		@Override
		protected Integer load() {
			if (getQuery() != null) {
				try {
					return getIssueService().count(SecurityUtils.getSubject(), getProjectScope(), getQuery().getCriteria());
				} catch (ExplicitException ignored) {
				}
			}
			return 0;
		}

	};
	
	public AbstractColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onDetach() {
		countModel.detach();
		super.onDetach();
	}
	
	protected void onCardAdded(AjaxRequestTarget target, Issue issue) {
		findParent(RepeatingView.class).visitChildren(AbstractColumnPanel.class, (IVisitor<AbstractColumnPanel, Void>) (columnPanel, visit) -> {
			if (columnPanel.getQuery() != null && columnPanel.getQuery().matches(issue)) {
				var cardListPanel = columnPanel.getCardListPanel();
				var firstCard = cardListPanel.findCard(null);
				if (firstCard != null)
					issue.setBoardPosition(getIssueService().load(firstCard.getIssueId()).getBoardPosition() - 1);
				getIssueService().open(issue);
				cardListPanel.onCardAdded(target, issue.getId());
				visit.stop();
			}
		});		
	}
	
	protected abstract IssueQuery getQuery();
	
	protected abstract CardListPanel getCardListPanel();

	protected GlobalIssueSetting getIssueSetting() {
		return CodiBase.getInstance(SettingService.class).getIssueSetting();
	}
	
	protected IssueChangeService getIssueChangeService() {
		return CodiBase.getInstance(IssueChangeService.class);
	}
	
	protected IssueService getIssueService() {
		return CodiBase.getInstance(IssueService.class);
	}

	protected IterationService getIterationService() {
		return CodiBase.getInstance(IterationService.class);
	}
	
	@Override
	public Object getInputValue(String name) {
		return null;
	}

	protected Project getProject() {
		return getProjectScope().getProject();
	}
	
	protected abstract ProjectScope getProjectScope();

	protected abstract IterationSelection getIterationSelection();
	
	@Nullable
	protected abstract String getIterationPrefix();
	
	protected Component newAddToIterationLink(String componentId) {
		if (getQuery() != null && SecurityUtils.canScheduleIssues(getProject())) {

			return new AjaxLink<Void>(componentId) {
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					var bean = new AddToIterationBean();
					bean.setProjectId(getProject().getId());
					bean.setBacklog(isBacklog());
					bean.setIterationPrefix(getIterationPrefix());
					if (getIterationSelection().getIteration() != null)
						bean.setCurrentIteration(getIterationSelection().getIteration().getName());
					var excludeProperties = new HashSet<String>();
					if (SecurityUtils.getUser().isServiceAccount())
						excludeProperties.add(AddToIterationBean.PROP_SEND_NOTIFICATIONS);
					new BeanEditModalPanel<>(target, bean, excludeProperties, true, null) {

						@Override
						protected String onSave(AjaxRequestTarget target, AddToIterationBean bean) {
							BasePage page = (BasePage) getPage();
							close();
							var issues = getIssueService().query(SecurityUtils.getSubject(), getProjectScope(), getQuery(),
									false, 0, Integer.MAX_VALUE);
							var addIteration = getIterationService().findInHierarchy(getProject(), bean.getIteration());
							Iteration removeIteration;
							if (!isBacklog() && bean.isRemoveFromCurrentIteration())
								removeIteration = getIterationSelection().getIteration();
							else 
								removeIteration = null;
							getIssueChangeService().changeSchedule(SecurityUtils.getUser(), issues, addIteration, removeIteration, bean.isSendNotifications());
							for (var issue : issues)
								page.notifyObservablesChange(target, issue.getChangeObservables(true));
							return null;
						}

					};
				}

			}.add(AttributeAppender.append("style", new LoadableDetachableModel<String>() {
				@Override
				protected String load() {
					// We may also toggle this link in javascript when card is moved between columns, 
					// so simply hide it instead of calling setVisible(false) 
					if (countModel.getObject() == 0)
						return "display: none;";
					else
						return "";
				}
			}));
		} else {
			return new WebMarkupContainer(componentId).setVisible(false);
		}
	}
	
	protected abstract boolean isBacklog();
}
