codibase.server.pullRequestChoiceFormatter = {
	formatPullRequest: function(request) {
		return request.title + " (" + request.reference +")";
	},
	formatSelection: function(request) {
		return codibase.server.pullRequestChoiceFormatter.formatPullRequest(request);
	},
	formatResult: function(request) {
		return codibase.server.pullRequestChoiceFormatter.formatPullRequest(request);
	},
	escapeMarkup: function(m) {
		return m;
	},
	
}; 
