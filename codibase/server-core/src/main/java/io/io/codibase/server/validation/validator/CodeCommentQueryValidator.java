package io.codibase.server.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.codibase.commons.utils.StringUtils;
import io.codibase.server.model.Project;
import io.codibase.server.annotation.CodeCommentQuery;

public class CodeCommentQueryValidator implements ConstraintValidator<CodeCommentQuery, String> {

	private String message;
	
	@Override
	public void initialize(CodeCommentQuery constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			Project project = Project.get();
			try {
				io.codibase.server.search.entity.codecomment.CodeCommentQuery.parse(project, value, true);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				String message = this.message;
				if (message.length() == 0) {
					if (StringUtils.isNotBlank(e.getMessage()))
						message = e.getMessage();
					else
						message = "Malformed query";
				}
				
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
