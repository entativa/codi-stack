package io.codibase.server.web.component.user.choice;

import java.util.List;

import org.apache.wicket.model.IModel;

import io.codibase.server.CodiBase;
import io.codibase.server.service.UserService;
import io.codibase.server.model.User;
import io.codibase.server.util.Similarities;
import io.codibase.server.util.facade.UserCache;
import io.codibase.server.web.WebConstants;
import io.codibase.server.web.component.select2.Response;
import io.codibase.server.web.component.select2.ResponseFiller;

public class UserChoiceProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final IModel<List<User>> choicesModel;
	
	public UserChoiceProvider(IModel<List<User>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<User> response) {
		List<User> users = choicesModel.getObject();
		UserService userService = CodiBase.getInstance(UserService.class);
		
		UserCache cache = userService.cloneCache();
		
		List<User> similarities = new Similarities<User>(users) {

			private static final long serialVersionUID = 1L;

			@Override
			public double getSimilarScore(User object) {
				return cache.getSimilarScore(object, term);
			}
			
		};
		
		new ResponseFiller<User>(response).fill(similarities, page, WebConstants.PAGE_SIZE);
	}

}