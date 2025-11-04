package io.codibase.server.web.editable.polymorphic;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import io.codibase.commons.utils.ClassUtils;
import io.codibase.server.web.editable.EditSupport;
import io.codibase.server.web.editable.EmptyValueLabel;
import io.codibase.server.web.editable.PropertyContext;
import io.codibase.server.web.editable.PropertyDescriptor;
import io.codibase.server.web.editable.PropertyEditor;
import io.codibase.server.web.editable.PropertyViewer;
import io.codibase.server.annotation.Editable;

public class PolymorphicEditSuport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyClass();
		if (propertyClass.getAnnotation(Editable.class) != null && !ClassUtils.isConcrete(propertyClass)) {
			return new PropertyContext<Serializable>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Serializable> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new PolymorphicPropertyViewer(id, propertyDescriptor, model.getObject());
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
				public PropertyEditor<Serializable> renderForEdit(String componentId, IModel<Serializable> model) {
					return new PolymorphicPropertyEditor(componentId, descriptor, model);
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
