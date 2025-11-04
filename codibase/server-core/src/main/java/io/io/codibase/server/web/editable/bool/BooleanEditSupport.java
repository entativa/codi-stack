package io.codibase.server.web.editable.bool;

import static io.codibase.server.web.translation.Translation._T;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.codibase.server.web.editable.EditSupport;
import io.codibase.server.web.editable.EmptyValueLabel;
import io.codibase.server.web.editable.PropertyContext;
import io.codibase.server.web.editable.PropertyDescriptor;
import io.codibase.server.web.editable.PropertyEditor;
import io.codibase.server.web.editable.PropertyViewer;
import io.codibase.server.web.util.TextUtils;

public class BooleanEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass == boolean.class || propertyClass == Boolean.class) {
			return new PropertyContext<Boolean>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Boolean> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new Label(id, _T(TextUtils.getDisplayValue(model.getObject())));
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
				public PropertyEditor<Boolean> renderForEdit(String componentId, IModel<Boolean> model) {
					if (descriptor.isPropertyRequired())
						return new BooleanPropertyEditor(componentId, descriptor, model);
					else
						return new NullableBooleanPropertyEditor(componentId, descriptor, model);
				}
				
			};
		} else {
			return null;
		}
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
