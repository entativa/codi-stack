package io.codibase.server.web.editable.buildspec.job.choice;

import static io.codibase.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.codibase.server.annotation.JobChoice;
import io.codibase.server.model.Project;
import io.codibase.server.web.component.job.JobSingleChoice;
import io.codibase.server.web.editable.PropertyDescriptor;
import io.codibase.server.web.editable.PropertyEditor;

public class JobSingleChoiceEditor extends PropertyEditor<String> {
	
	private JobSingleChoice input;
	
	public JobSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<String> choices = new ArrayList<>();
		for (String jobName: Project.get().getJobNames())
			choices.add(jobName);
		
		JobChoice jobChoice = Preconditions.checkNotNull(descriptor.getPropertyGetter().getAnnotation(JobChoice.class));
		
		String selection = getModelObject();
		if (!jobChoice.tagsMode() && !choices.contains(selection))
			selection = null;
		
		input = new JobSingleChoice("input", Model.of(selection), Model.ofList(choices), jobChoice.tagsMode()) {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
		input.setRequired(descriptor.isPropertyRequired());
        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
        
        add(input);
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
