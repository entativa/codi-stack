package io.codibase.server.search.code;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.codibase.commons.jsymbol.Symbol;
import io.codibase.commons.jsymbol.SymbolExtractor;

public interface CodeIndexService {
	
	void indexAsync(Long projectId, ObjectId commitId);
	
	boolean isIndexed(Long projectId, ObjectId commitId);
	
	String getIndexVersion(@Nullable SymbolExtractor<Symbol> extractor);
	
}
