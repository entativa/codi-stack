package io.codibase.server.web.editable.pullrequest.choice;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestService;
import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;
import io.codibase.server.web.editable.EditSupport;
import io.codibase.server.web.editable.EmptyValueLabel;
import io.codibase.server.web.editable.PropertyContext;
import io.codibase.server.web.editable.PropertyDescriptor;
import io.codibase.server.web.editable.PropertyEditor;
import io.codibase.server.web.editable.PropertyViewer;
import io.codibase.server.annotation.PullRequestChoice;

public class PullRequestChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        Method propertyGetter = descriptor.getPropertyGetter();
        PullRequestChoice pullRequestChoice = propertyGetter.getAnnotation(PullRequestChoice.class);
        if (pullRequestChoice != null) {
        	if (propertyGetter.getReturnType() == Long.class) {
        		return new PropertyContext<Long>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Long> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								Long pullRequestId = model.getObject();
								if (pullRequestId != null) {
									PullRequest request = CodiBase.getInstance(PullRequestService.class).get(pullRequestId);
									if (request != null) {
										if (Project.get() != null && Project.get().getForkRoot().equals(request.getNumberScope()))
											return new Label(id, "#" + request.getNumber());
										else
											return new Label(id, request.getReference().toString(null));
									} else {
										return new Label(id, "<i>Not Found</i>").setEscapeModelStrings(false);
									}
								} else { 
									return new EmptyValueLabel(id) {

										@Override
										protected AnnotatedElement getElement() {
											return propertyDescriptor.getPropertyGetter();
										}
										
									};
								}
							}
							
						};
					}

					@Override
					public PropertyEditor<Long> renderForEdit(String componentId, IModel<Long> model) {
						return new PullRequestChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'PullRequestChoice' should be applied to property with type 'Long'.");
        	}
        } else {
            return null;
        }
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
