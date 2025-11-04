package io.codibase.server.web.component.user.sshkey;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.codibase.server.CodiBase;
import io.codibase.server.service.AuditService;
import io.codibase.server.service.SshKeyService;
import io.codibase.server.model.SshKey;
import io.codibase.server.model.User;
import io.codibase.server.util.Path;
import io.codibase.server.util.PathNode;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.editable.BeanEditor;
import io.codibase.server.web.page.user.UserPage;

public abstract class InsertSshKeyPanel extends Panel {

    public InsertSshKeyPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        add(new AjaxLink<Void>("close") {

            @Override
            public void onClick(AjaxRequestTarget target) {
            	onCancel(target);
            }
            
        });
        
        Form<?> form = new Form<Void>("form");
        
        BeanEditor editor = BeanContext.edit("editor", new SshKey());
        form.add(editor);
        
        form.add(new AjaxButton("add") {
        	
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> myform) {
                super.onSubmit(target, myform);
                
                SshKeyService sshKeyService = CodiBase.getInstance(SshKeyService.class);
                SshKey sshKey = (SshKey) editor.getModelObject();
                sshKey.generateFingerprint();
                
                if (sshKeyService.findByFingerprint(sshKey.getFingerprint()) != null) {
					editor.error(new Path(new PathNode.Named("content")), "This key is already in use");
					target.add(form);
                } else {
                    sshKey.setOwner(getUser());
                    sshKey.setCreatedAt(new Date());
                    sshKeyService.create(sshKey);
                    if (getPage() instanceof UserPage)
						CodiBase.getInstance(AuditService.class).audit(null, "added SSH key \"" + sshKey.getFingerprint() + "\" in account \"" + sshKey.getOwner().getName() + "\"", null, null);
                    onSave(target);
                }
            }
            
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.add(form);
            }
            
        });
        
        form.add(new AjaxLink<Void>("cancel") {
            
        	@Override
            public void onClick(AjaxRequestTarget target) {
        		onCancel(target);
            }
            
        });
        
        add(form.setOutputMarkupId(true));
    }
    
    protected abstract User getUser();
    
    protected abstract void onSave(AjaxRequestTarget target);
    
    protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SshKeyCssResourceReference()));
	}
    
}
