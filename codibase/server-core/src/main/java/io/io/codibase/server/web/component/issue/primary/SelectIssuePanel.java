package io.codibase.server.web.component.issue.primary;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueService;
import io.codibase.server.model.Issue;
import io.codibase.server.web.component.issue.choice.IssueChoiceProvider;
import io.codibase.server.web.component.issue.choice.IssueSingleChoice;

abstract class SelectIssuePanel extends FormComponentPanel<Issue> {

    private Long issueId;

    private IssueSingleChoice choice;

    public SelectIssuePanel(String id) {
        super(id, Model.of((Issue)null));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new FencedFeedbackPanel("feedback"));
        choice = new IssueSingleChoice("choice", new IModel<Issue>() {

            @Override
            public void detach() {
            }

            @Override
            public Issue getObject() {
                if (issueId != null)
                    return getIssueService().load(issueId);
                else
                    return null;
            }

            @Override
            public void setObject(Issue object) {
                issueId = object.getId();
            }

        }, getChoiceProvider());        
        choice.setRequired(true);
        add(choice);

        setOutputMarkupId(true);
    }

    @Override
    public void convertInput() {
        super.convertInput();
        setConvertedInput(choice.getConvertedInput());
    }
    
    protected abstract IssueChoiceProvider getChoiceProvider();

    private IssueService getIssueService() {
        return CodiBase.getInstance(IssueService.class);
    }

}
