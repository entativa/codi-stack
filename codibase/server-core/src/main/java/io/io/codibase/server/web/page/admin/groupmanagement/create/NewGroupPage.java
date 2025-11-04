package io.codibase.server.web.page.admin.groupmanagement.create;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.AuditService;
import io.codibase.server.service.GroupService;
import io.codibase.server.model.Group;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.Path;
import io.codibase.server.util.PathNode;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.editable.BeanEditor;
import io.codibase.server.web.page.admin.AdministrationPage;
import io.codibase.server.web.page.admin.groupmanagement.GroupCssResourceReference;
import io.codibase.server.web.page.admin.groupmanagement.GroupListPage;
import io.codibase.server.web.page.admin.groupmanagement.membership.GroupMembershipsPage;

public class NewGroupPage extends AdministrationPage {

	private Group group = new Group();
	
	public NewGroupPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BeanEditor editor = BeanContext.edit("editor", group);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				GroupService groupService = CodiBase.getInstance(GroupService.class);
				Group groupWithSameName = groupService.find(group.getName());
				if (groupWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							_T("This name has already been used by another group"));
				} 
				if (editor.isValid()) {
					groupService.create(group);
					var newAuditContent = VersionedXmlDoc.fromBean(group).toXML();
					CodiBase.getInstance(AuditService.class).audit(null, "created group \"" + group.getName() + "\"", null, newAuditContent);
					Session.get().success(_T("Group created"));
					setResponsePage(GroupMembershipsPage.class, GroupMembershipsPage.paramsOf(group));
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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GroupCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("groups", GroupListPage.class));
		return fragment;
	}

}
