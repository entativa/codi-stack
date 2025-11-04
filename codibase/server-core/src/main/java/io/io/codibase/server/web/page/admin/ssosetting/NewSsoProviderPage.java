package io.codibase.server.web.page.admin.ssosetting;

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
import io.codibase.server.service.SsoProviderService;
import io.codibase.server.model.SsoProvider;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.Path;
import io.codibase.server.util.PathNode;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.editable.BeanEditor;
import io.codibase.server.web.page.admin.AdministrationPage;
import io.codibase.server.web.page.admin.groupmanagement.GroupCssResourceReference;

public class NewSsoProviderPage extends AdministrationPage {
	
	public NewSsoProviderPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var bean = new SsoProviderBean();		
		BeanEditor editor = BeanContext.edit("editor", bean);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				SsoProvider providerWithSameName = getSsoProviderService().find(bean.getName());
				if (providerWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							_T("This name has already been used by another provider"));
				} 
				if (editor.isValid()) {			
					var provider = new SsoProvider();
					bean.populate(provider);
					getSsoProviderService().createOrUpdate(provider);
					var newAuditContent = VersionedXmlDoc.fromBean(provider).toXML();
					CodiBase.getInstance(AuditService.class).audit(null, "created SSO provider \"" + provider.getName() + "\"", null, newAuditContent);
					Session.get().success(_T("SSO provider created"));
					setResponsePage(SsoProviderListPage.class);
				}
			}
			
		};
		form.add(editor);
		add(form);
	}

	private SsoProviderService getSsoProviderService() {
		return CodiBase.getInstance(SsoProviderService.class);
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
		fragment.add(new BookmarkablePageLink<Void>("ssoProviders", SsoProviderListPage.class));
		return fragment;
	}

}
