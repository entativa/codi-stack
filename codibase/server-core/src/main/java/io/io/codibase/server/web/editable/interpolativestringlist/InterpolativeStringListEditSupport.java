package io.codibase.server.web.editable.interpolativestringlist;

import static io.codibase.server.util.ReflectionUtils.getCollectionElementClass;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import io.codibase.server.annotation.Interpolative;
import io.codibase.server.web.editable.EditSupport;
import io.codibase.server.web.editable.EmptyValueLabel;
import io.codibase.server.web.editable.PropertyContext;
import io.codibase.server.web.editable.PropertyDescriptor;
import io.codibase.server.web.editable.PropertyEditor;
import io.codibase.server.web.editable.PropertyViewer;

public class InterpolativeStringListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (List.class.isAssignableFrom(descriptor.getPropertyClass())) {
			var propertyGetter = descriptor.getPropertyGetter();
			Class<?> elementClass = getCollectionElementClass(propertyGetter.getGenericReturnType());
			if (elementClass == String.class && propertyGetter.getAnnotation(Interpolative.class) != null) {
				return new PropertyContext<List<String>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null && !model.getObject().isEmpty()) {
									return new InterpolativeStringListPropertyViewer(id, propertyDescriptor, model.getObject());
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
					public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
						return new InterpolativeStringListPropertyEditor(componentId, descriptor, model);
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
