package io.codibase.server.service;

import io.codibase.server.model.LinkSpec;

import org.jspecify.annotations.Nullable;
import java.util.List;

public interface LinkSpecService extends EntityService<LinkSpec> {

	@Nullable
	LinkSpec find(String name);
	
	List<LinkSpec> queryAndSort();

	void updateOrders(List<LinkSpec> links);
	
	void update(LinkSpec spec, @Nullable String oldName, @Nullable String oldOppositeName);

	void create(LinkSpec spec);
}
