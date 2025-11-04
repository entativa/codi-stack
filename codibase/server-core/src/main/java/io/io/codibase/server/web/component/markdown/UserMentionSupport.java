package io.codibase.server.web.component.markdown;

import java.util.List;

import io.codibase.server.model.User;

public interface UserMentionSupport {

	List<User> findUsers(String query, int count);

}
