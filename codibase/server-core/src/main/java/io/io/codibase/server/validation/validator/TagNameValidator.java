package io.codibase.server.validation.validator;

import io.codibase.server.annotation.TagName;
import io.codibase.server.git.GitUtils;
import org.eclipse.jgit.lib.Repository;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TagNameValidator implements ConstraintValidator<TagName, String> {
	
	private String message;
	
	@Override
	public void initialize(TagName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (!Repository.isValidRefName(GitUtils.tag2ref(value))) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) {
				message = "Invalid git tag name";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
