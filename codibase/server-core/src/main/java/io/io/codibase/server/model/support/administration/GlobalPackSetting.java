package io.codibase.server.model.support.administration;

import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.model.support.pack.NamedPackQuery;
import io.codibase.server.pack.PackSupport;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Editable
public class GlobalPackSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedPackQuery> namedQueries = new ArrayList<>();
	
	public GlobalPackSetting() {
		namedQueries.add(new NamedPackQuery("All", null));
		List<PackSupport> packSupports = new ArrayList<>(CodiBase.getExtensions(PackSupport.class));
		packSupports.sort(Comparator.comparing(PackSupport::getOrder));
		for (var packSupport: packSupports)
			namedQueries.add(new NamedPackQuery(packSupport.getPackType(), "\"Type\" is \"" + packSupport.getPackType() + "\""));
	}
	
	public List<NamedPackQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedPackQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	@Nullable
	public NamedPackQuery getNamedQuery(String name) {
		for (NamedPackQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
}
