package io.codibase.server.web.page.admin.rolemanagement;

import static io.codibase.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.LinkSpecService;
import io.codibase.server.service.RoleService;
import io.codibase.server.model.LinkAuthorization;
import io.codibase.server.model.LinkSpec;
import io.codibase.server.model.Role;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.Path;
import io.codibase.server.util.PathNode;
import io.codibase.server.web.WebSession;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.editable.BeanEditor;
import io.codibase.server.web.page.admin.AdministrationPage;
import io.codibase.server.web.util.ConfirmClickModifier;

public class RoleDetailPage extends AdministrationPage {
	
	public static final String PARAM_ROLE = "role";
	
	protected final IModel<Role> roleModel;
	
	private String oldName;
	
	private BeanEditor editor;
	
	public RoleDetailPage(PageParameters params) {
		super(params);
		
		String roleIdString = params.get(PARAM_ROLE).toString();
		if (StringUtils.isBlank(roleIdString))
			throw new RestartResponseException(RoleListPage.class);
		
		roleModel = new LoadableDetachableModel<Role>() {

			@Override
			protected Role load() {
				Role role = getManager().load(Long.valueOf(roleIdString));
				for (LinkAuthorization linkAuthorization: role.getLinkAuthorizations())
					role.getEditableIssueLinks().add(linkAuthorization.getLink().getName());
				return role;
			}
			
		};
	}
	
	private RoleService getManager() {
		return CodiBase.getInstance(RoleService.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getRole().isOwner()) {
			add(new Fragment("content", "ownerFrag", this));
		} else {
			Fragment fragment = new Fragment("content", "nonOwnerFrag", this);
			editor = BeanContext.editModel("editor", new IModel<Serializable>() {

				@Override
				public void detach() {
				}

				@Override
				public Serializable getObject() {
					return getRole();
				}

				@Override
				public void setObject(Serializable object) {
					oldName = getRole().getName();
					editor.getDescriptor().copyProperties(object, getRole());
				}
				
			});
			var oldAuditContent = VersionedXmlDoc.fromBean(editor.getPropertyValues()).toXML();
			
			Form<?> form = new Form<Void>("form") {

				@Override
				protected void onSubmit() {
					super.onSubmit();
					
					Role role = getRole();
					RoleService roleService = CodiBase.getInstance(RoleService.class);
					Role roleWithSameName = roleService.find(role.getName());
					if (roleWithSameName != null && !roleWithSameName.equals(role)) {
						editor.error(new Path(new PathNode.Named("name")),
								_T("This name has already been used by another role."));
					} 
					if (editor.isValid()) {
						Collection<LinkSpec> authorizedLinks = new ArrayList<>();
						for (String linkName: role.getEditableIssueLinks()) 
							authorizedLinks.add(CodiBase.getInstance(LinkSpecService.class).find(linkName));
						roleService.update(role, authorizedLinks, oldName);
						var newAuditContent = VersionedXmlDoc.fromBean(editor.getPropertyValues()).toXML();
						auditService.audit(null, "changed role \"" + role.getName() + "\"", oldAuditContent, newAuditContent);
						setResponsePage(RoleDetailPage.class, RoleDetailPage.paramsOf(role));
						Session.get().success(MessageFormat.format(_T("Role \"{0}\" updated"), role.getName()));
					}
				}
				
			};	
			
			form.add(editor);
			form.add(new FencedFeedbackPanel("feedback", form));
			form.add(new Link<Void>("delete") {

				@Override
				public void onClick() {
					CodiBase.getInstance(RoleService.class).delete(getRole());
					Session.get().success(MessageFormat.format(_T("Role \"{0}\" deleted"), getRole().getName()));
					
					String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Role.class);
					if (redirectUrlAfterDelete != null)
						throw new RedirectToUrlException(redirectUrlAfterDelete);
					else
						setResponsePage(RoleListPage.class);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getRole().isOwner());
				}
				
			}.add(new ConfirmClickModifier(MessageFormat.format(_T("Do you really want to delete role \"{0}\"?"), getRole().getName()))));
			
			fragment.add(form);
			add(fragment);
		}
	}
	
	@Override
	protected void onDetach() {
		roleModel.detach();
		super.onDetach();
	}
	
	public Role getRole() {
		return roleModel.getObject();
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	public static PageParameters paramsOf(Role role) {
		PageParameters params = new PageParameters();
		params.add(PARAM_ROLE, role.getId());
		return params;
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("roles", RoleListPage.class));
		fragment.add(new Label("roleName", getRole().getName()));
		return fragment;
	}

}
