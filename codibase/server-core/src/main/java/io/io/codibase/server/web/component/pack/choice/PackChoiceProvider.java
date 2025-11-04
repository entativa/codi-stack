package io.codibase.server.web.component.pack.choice;

import com.google.common.collect.Lists;
import io.codibase.server.CodiBase;
import io.codibase.server.service.PackService;
import io.codibase.server.model.Pack;
import io.codibase.server.web.component.select2.ChoiceProvider;
import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import java.util.Collection;
import java.util.List;

public abstract class PackChoiceProvider extends ChoiceProvider<Pack> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void toJson(Pack choice, JSONWriter writer) throws JSONException {
		writer
			.key("id").value(choice.getId())
			.key("reference").value(HtmlEscape.escapeHtml5(choice.getReference(false)));
	}

	@Override
	public Collection<Pack> toChoices(Collection<String> ids) {
		List<Pack> packs = Lists.newArrayList();
		for (String id: ids) {
			var pack = getPackService().load(Long.valueOf(id)); 
			Hibernate.initialize(pack);
			packs.add(pack);
		}
		return packs;
	}
	
	private PackService getPackService() {
		return CodiBase.getInstance(PackService.class);
	}
	
}