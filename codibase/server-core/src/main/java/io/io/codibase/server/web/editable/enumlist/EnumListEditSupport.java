package io.codibase.server.web.editable.enumlist;

import static io.codibase.server.web.translation.Translation._T;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.codibase.server.util.ReflectionUtils;
import io.codibase.server.web.editable.EditSupport;
import io.codibase.server.web.editable.EmptyValueLabel;
import io.codibase.server.web.editable.PropertyContext;
import io.codibase.server.web.editable.PropertyDescriptor;
import io.codibase.server.web.editable.PropertyEditor;
import io.codibase.server.web.editable.PropertyViewer;
import io.codibase.server.web.util.TextUtils;

public class EnumListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (List.class.isAssignableFrom(descriptor.getPropertyClass())) {
			final Class<?> elementClass = ReflectionUtils.getCollectionElementClass(descriptor.getPropertyGetter().getGenericReturnType());
			if (elementClass != null && Enum.class.isAssignableFrom(elementClass)) {
	            return new PropertyContext<List<Enum<?>>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<Enum<?>>> model) {

						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        if (model.getObject() != null && !model.getObject().isEmpty()) {
						            String content = "";
						            for (Enum<?> each: model.getObject()) {
						            	if (content.length() == 0)
						            		content += _T(TextUtils.getDisplayValue(each));
						            	else
						            		content += ", " + _T(TextUtils.getDisplayValue(each));
						            }
						            return new Label(id, content);
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
					public PropertyEditor<List<Enum<?>>> renderForEdit(String componentId, IModel<List<Enum<?>>> model) {
						return new EnumListPropertyEditor(componentId, descriptor, model);
					}
	            	
	            };
			}
		}
		return null;
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
