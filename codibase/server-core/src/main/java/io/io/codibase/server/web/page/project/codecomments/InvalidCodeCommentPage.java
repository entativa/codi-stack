package io.codibase.server.web.page.project.codecomments;

import static io.codibase.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.text.MessageFormat;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;

import io.codibase.commons.utils.StringUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentService;
import io.codibase.server.model.CodeComment;
import io.codibase.server.model.Project;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.WebSession;
import io.codibase.server.web.component.link.ViewStateAwarePageLink;
import io.codibase.server.web.component.markdown.MarkdownViewer;
import io.codibase.server.web.page.project.ProjectPage;
import io.codibase.server.web.page.project.dashboard.ProjectDashboardPage;
import io.codibase.server.web.util.ConfirmClickModifier;

public class InvalidCodeCommentPage extends ProjectPage {

	public static final String PARAM_COEDE_COMMENT = "code-comment";
	
	private IModel<CodeComment> codeCommentModel;
	
	public InvalidCodeCommentPage(PageParameters params) {
		super(params);
		
		codeCommentModel = new LoadableDetachableModel<CodeComment>() {

			@Override
			protected CodeComment load() {
				Long codeCommentId = params.get(PARAM_COEDE_COMMENT).toLong();
				CodeComment codeComment = CodiBase.getInstance(CodeCommentService.class).load(codeCommentId);
				Preconditions.checkState(!codeComment.isValid());
				return codeComment;
			}

		};
	}

	private CodeComment getCodeComment() {
		return codeCommentModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var comment = getCodeComment();
		add(new Label("missingCommits", StringUtils.join(comment.getMissingCommits().stream().map(ObjectId::getName).collect(toList()), " ")));
		add(new Label("commentFile", comment.getMark().getPath()));
		add(new MarkdownViewer("commentContent", Model.of(comment.getContent()), null));
		
		add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				CodiBase.getInstance(CodeCommentService.class).delete(getCodeComment());
				
				Session.get().success(MessageFormat.format(_T("Code comment #{0} deleted"), getCodeComment().getId()));
				
				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(CodeComment.class);
				if (redirectUrlAfterDelete != null)
					throw new RedirectToUrlException(redirectUrlAfterDelete);
				else
					setResponsePage(ProjectCodeCommentsPage.class, ProjectCodeCommentsPage.paramsOf(getProject()));
				
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageCodeComments(getCodeComment().getProject()));
			}
			
		}.add(new ConfirmClickModifier(_T("Really want to delete this code comment?"))));
	}
	
	public static PageParameters paramsOf(CodeComment codeComment) {
		PageParameters params = ProjectPage.paramsOf(codeComment.getProject());
		params.add(PARAM_COEDE_COMMENT, codeComment.getId());
		return params;
	}
	
	@Override
	protected void onDetach() {
		codeCommentModel.detach();
		super.onDetach();
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectCodeCommentsCssResourceReference()));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("codeComments", ProjectCodeCommentsPage.class, 
				ProjectCodeCommentsPage.paramsOf(getProject())));
		fragment.add(new Label("codeCommentNumber", "#" + getCodeComment().getId()));
		return fragment;
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isCodeManagement() && SecurityUtils.canReadCode(project)) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectCodeCommentsPage.class, ProjectCodeCommentsPage.paramsOf(project, 0));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}

}
