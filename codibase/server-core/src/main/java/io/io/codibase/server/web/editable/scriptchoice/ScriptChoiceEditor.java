package io.codibase.server.web.editable.scriptchoice;

import static io.codibase.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.support.administration.GroovyScript;
import io.codibase.server.util.ScriptContribution;
import io.codibase.server.web.component.stringchoice.StringSingleChoice;
import io.codibase.server.web.editable.PropertyDescriptor;
import io.codibase.server.web.editable.PropertyEditor;

public class ScriptChoiceEditor extends PropertyEditor<String> {

	private FormComponent<String> input;
	
	public ScriptChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<List<String>> choicesModel = new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				List<String> choices = new ArrayList<>();
				
				for (GroovyScript script: CodiBase.getInstance(SettingService.class).getGroovyScripts())
					choices.add(script.getName());
				
				for (ScriptContribution contribution: CodiBase.getExtensions(ScriptContribution.class)) {
					GroovyScript script = contribution.getScript();
					choices.add(GroovyScript.BUILTIN_PREFIX + script.getName());
				}
				
				return choices;
			}
			
		};
		
		String selection = getModelObject();
		if (!choicesModel.getObject().contains(selection))
			selection = null;
		
		input = new StringSingleChoice("input", Model.of(selection), choicesModel, false) {

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
