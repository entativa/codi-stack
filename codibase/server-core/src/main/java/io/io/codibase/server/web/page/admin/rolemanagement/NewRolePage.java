package io.codibase.server.web.page.admin.rolemanagement;

import static io.codibase.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.AuditService;
import io.codibase.server.service.LinkSpecService;
import io.codibase.server.service.RoleService;
import io.codibase.server.model.LinkSpec;
import io.codibase.server.model.Role;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.Path;
import io.codibase.server.util.PathNode;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.editable.BeanEditor;
import io.codibase.server.web.page.admin.AdministrationPage;

public class NewRolePage extends AdministrationPage {

	private Role role = new Role();

	public NewRolePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		BeanEditor editor = BeanContext.edit("editor", role);

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				RoleService roleService = CodiBase.getInstance(RoleService.class);
				Role roleWithSameName = roleService.find(role.getName());
				if (roleWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							_T("This name has already been used by another role"));
				}
				if (editor.isValid()) {
					Collection<LinkSpec> authorizedLinks = new ArrayList<>();
					for (String linkName : role.getEditableIssueLinks())
						authorizedLinks.add(CodiBase.getInstance(LinkSpecService.class).find(linkName));
					roleService.create(role, authorizedLinks);
					var newAuditContent = VersionedXmlDoc.fromBean(editor.getPropertyValues()).toXML();
					CodiBase.getInstance(AuditService.class).audit(null, "created role \"" + role.getName() + "\"", null, newAuditContent);
					Session.get().success(_T("Role created"));
					setResponsePage(RoleListPage.class);
				}
			}

		};
		form.add(editor);

		add(form);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("roles", RoleListPage.class));
		return fragment;
	}

}
