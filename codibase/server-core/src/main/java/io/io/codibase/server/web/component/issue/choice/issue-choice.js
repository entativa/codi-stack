codibase.server.issueChoiceFormatter = {
	formatIssue: function(issue) {
		return issue.title + " (" + issue.reference +")";
	},
	formatSelection: function(issue) {
		return codibase.server.issueChoiceFormatter.formatIssue(issue);
	},
	formatResult: function(issue) {
		return codibase.server.issueChoiceFormatter.formatIssue(issue);
	},
	escapeMarkup: function(m) {
		return m;
	},
	
}; 
