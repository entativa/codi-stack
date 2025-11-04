package io.codibase.server.web.page.project.blob.search.result;

import java.io.Serializable;
import java.util.List;

import io.codibase.commons.utils.LinearRange;
import io.codibase.server.search.code.hit.QueryHit;

public class MatchedBlob implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String blobPath;
	
	private final List<QueryHit> hits;
	
	private LinearRange match;
	
	public MatchedBlob(String blobPath, List<QueryHit> hits) {
		this.blobPath = blobPath;
		this.hits = hits;
	}

	public String getBlobPath() {
		return blobPath;
	}

	public List<QueryHit> getHits() {
		return hits;
	}

	public LinearRange getMatch() {
		return match;
	}

	public void setMatch(LinearRange match) {
		this.match = match;
	}

}
