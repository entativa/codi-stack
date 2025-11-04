package io.codibase.server.web.editable;

import io.codibase.commons.codeassist.InputCompletion;
import io.codibase.commons.codeassist.InputStatus;
import io.codibase.commons.utils.LinearRange;
import io.codibase.server.annotation.ProjectPath;
import io.codibase.server.web.behavior.inputassist.InputAssistBehavior;
import io.codibase.server.web.editable.string.StringPropertyEditor;
import io.codibase.server.web.editable.string.StringPropertyViewer;
import io.codibase.server.web.util.SuggestionUtils;
import org.apache.wicket.model.IModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ProjectPathEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		ProjectPath projectPath = propertyGetter.getAnnotation(ProjectPath.class);
        if (projectPath != null) {
        	if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(descriptor) {

    				@Override
    				public PropertyViewer renderForView(String componentId, IModel<String> model) {
    					return new StringPropertyViewer(componentId, descriptor, model.getObject());
    				}

    				@Override
    				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						InputAssistBehavior behavior = new InputAssistBehavior() {
							@Override
							protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
								List<InputCompletion> suggestions = new ArrayList<>();
								String matchWith = inputStatus.getContentBeforeCaret();
								for (var suggestion: SuggestionUtils.suggestProjectPaths(matchWith)) {
									suggestions.add(new InputCompletion(
											suggestion.getContent(), suggestion.getContent(), 
											suggestion.getContent().length(), null, suggestion.getMatch()));
								}
								return suggestions;
							}

							@Override
							protected List<LinearRange> getErrors(String inputContent) {
								return new ArrayList<>();
							}

							@Override
							protected int getAnchor(String inputContent) {
								return 0;
							}
							
						};
    		        	return new StringPropertyEditor(componentId, descriptor, model).setInputAssist(behavior);
    				}
        			
        		};
        	} else {
	    		throw new RuntimeException("Annotation 'Patterns' should be applied to property "
	    				+ "of type 'String'");
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
