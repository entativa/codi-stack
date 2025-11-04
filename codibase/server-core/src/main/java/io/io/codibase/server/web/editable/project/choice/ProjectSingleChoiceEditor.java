package io.codibase.server.web.editable.project.choice;

import static io.codibase.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.codibase.server.CodiBase;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Project;
import io.codibase.server.util.ReflectionUtils;
import io.codibase.server.util.facade.ProjectCache;
import io.codibase.server.web.component.project.choice.ProjectSingleChoice;
import io.codibase.server.web.editable.PropertyDescriptor;
import io.codibase.server.web.editable.PropertyEditor;
import io.codibase.server.annotation.ProjectChoice;

public class ProjectSingleChoiceEditor extends PropertyEditor<String> {

	private final IModel<List<Project>> choicesModel = new LoadableDetachableModel<List<Project>>() {

		@SuppressWarnings("unchecked")
		@Override
		protected List<Project> load() {
			ProjectChoice projectChoice = descriptor.getPropertyGetter().getAnnotation(ProjectChoice.class);
			if (projectChoice.value().length() != 0) {
				return (List<Project>) ReflectionUtils.invokeStaticMethod(
						descriptor.getPropertyGetter().getDeclaringClass(), projectChoice.value());
			} else {
				ProjectCache cache = getProjectService().cloneCache();
				List<Project> projects = new ArrayList<>(cache.getProjects());
				projects.sort(cache.comparingPath());
				return projects;
			}
		}
		
	};
	
	private ProjectSingleChoice input;
	
	public ProjectSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}
	
	private ProjectService getProjectService() {
		return CodiBase.getInstance(ProjectService.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Project selection;
		if (getModelObject() != null) 
			selection = getProjectService().findByPath(getModelObject());
		else 
			selection = null;
		
    	input = new ProjectSingleChoice("input", Model.of(selection), choicesModel) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(!descriptor.isPropertyRequired());
			}
    		
    	};
        
        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
        
		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		add(input);
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		Project project = input.getConvertedInput();
		if (project != null)
			return project.getPath();
		else
			return null;
	}

	@Override
	protected void onDetach() {
		choicesModel.detach();
		super.onDetach();
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
