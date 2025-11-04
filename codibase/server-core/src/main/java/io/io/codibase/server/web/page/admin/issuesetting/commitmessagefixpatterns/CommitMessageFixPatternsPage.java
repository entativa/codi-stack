package io.codibase.server.web.page.admin.issuesetting.commitmessagefixpatterns;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.support.issue.CommitMessageFixPatterns;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.page.admin.issuesetting.IssueSettingPage;

public class CommitMessageFixPatternsPage extends IssueSettingPage {

	private String oldAuditContent;
	
	public CommitMessageFixPatternsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		CommitMessageFixPatterns patterns = getSetting().getCommitMessageFixPatterns();
		oldAuditContent = VersionedXmlDoc.fromBean(patterns).toXML();
		Form<?> form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSetting().setCommitMessageFixPatterns(patterns);
				var newAuditContent = VersionedXmlDoc.fromBean(patterns).toXML();
				getSettingService().saveIssueSetting(getSetting());
				auditService.audit(null, "changed commit message fix patterns", oldAuditContent, newAuditContent);
				oldAuditContent = newAuditContent;
				Session.get().success(_T("Settings updated"));
			}
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(BeanContext.edit("editor", patterns));
		add(form);
	}
	
	private SettingService getSettingService() {
		return CodiBase.getInstance(SettingService.class);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>" + _T("Commit Message Fix Patterns") + "</span>").setEscapeModelStrings(false);
	}
	
}
