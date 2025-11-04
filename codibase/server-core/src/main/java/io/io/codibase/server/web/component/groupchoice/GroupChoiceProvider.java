package io.codibase.server.web.component.groupchoice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.codibase.server.model.Group;
import io.codibase.server.util.Similarities;
import io.codibase.server.web.WebConstants;
import io.codibase.server.web.component.select2.Response;
import io.codibase.server.web.component.select2.ResponseFiller;

public class GroupChoiceProvider extends AbstractGroupChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<Collection<Group>> choicesModel;
	
	public GroupChoiceProvider(IModel<Collection<Group>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<Group> response) {
		List<Group> similarities = new Similarities<Group>(choicesModel.getObject()) {

			private static final long serialVersionUID = 1L;

			@Override
			public double getSimilarScore(Group object) {
				return Similarities.getSimilarScore(object.getName(), term);
			}
			
		};
		
		new ResponseFiller<Group>(response).fill(similarities, page, WebConstants.PAGE_SIZE);
	}

}