package io.codibase.server.validation.validator;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.codibase.commons.utils.StringUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.UserName;
import io.codibase.server.service.UserService;
import io.codibase.server.model.User;
import io.codibase.server.persistence.SessionService;

public class UserNameValidator implements ConstraintValidator<UserName, String> {
	
	private static final Pattern PATTERN = Pattern.compile("\\w[\\w-\\.]*");
	
	private String message;
	
	@Override
	public void initialize(UserName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (!PATTERN.matcher(value).matches()) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) {
				message = "Should start with alphanumeric or underscore, and contains only "
						+ "alphanumeric, underscore, dash, or dot";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (value.equals("new") || value.equals(User.SYSTEM_NAME.toLowerCase()) 
				|| value.equals(User.UNKNOWN_NAME.toLowerCase())) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0)
				message = "'" + value + "' is a reserved name";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
	public static String suggestUserName(String preferredUserName) {
		return CodiBase.getInstance(SessionService.class).call(new Callable<String>() {

			@Override
			public String call() throws Exception {
				String normalizedUserName = normalizeUserName(preferredUserName);
				int suffix = 1;
				UserService userService = CodiBase.getInstance(UserService.class);
				while (true) {
					String suggestedUserName = normalizedUserName;
					if (suffix > 1)
						suggestedUserName += suffix;
					if (userService.findByName(suggestedUserName) == null) 
						return suggestedUserName;
					suffix++;
				}
			}
			
		});
	}

	public static String normalizeUserName(String preferredUserName) {		
		String normalizedUserName = StringUtils.substringBefore(preferredUserName, "@").replaceAll("[^\\w-\\.]", "-").toLowerCase();
		if (normalizedUserName.equals("new") 
				|| normalizedUserName.equals(User.SYSTEM_NAME)
				|| normalizedUserName.equals(User.UNKNOWN_NAME)) {
			normalizedUserName = normalizedUserName + "2";
		}
		return normalizedUserName;
	}
	
}
