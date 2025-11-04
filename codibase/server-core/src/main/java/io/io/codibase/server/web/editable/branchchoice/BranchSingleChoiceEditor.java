package io.codibase.server.web.editable.branchchoice;

import static io.codibase.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.codibase.server.annotation.BranchChoice;
import io.codibase.server.git.GitUtils;
import io.codibase.server.git.service.RefFacade;
import io.codibase.server.model.Project;
import io.codibase.server.web.component.branch.choice.BranchSingleChoice;
import io.codibase.server.web.editable.PropertyDescriptor;
import io.codibase.server.web.editable.PropertyEditor;

public class BranchSingleChoiceEditor extends PropertyEditor<String> {
	
	private BranchSingleChoice input;
	
	public BranchSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
		List<String> choices = new ArrayList<>();
		if (Project.get() != null) {
			for (RefFacade ref: Project.get().getBranchRefs()) {
				choices.add(GitUtils.ref2branch(ref.getName()));
			}
		}
		
		BranchChoice branchChoice = Preconditions.checkNotNull(descriptor.getPropertyGetter().getAnnotation(BranchChoice.class));
		String selection = getModelObject();
		if (!branchChoice.tagsMode() && !choices.contains(selection))
			selection = null;

    	input = new BranchSingleChoice("input", Model.of(selection), Model.ofList(choices), branchChoice.tagsMode()) {
    		
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
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
