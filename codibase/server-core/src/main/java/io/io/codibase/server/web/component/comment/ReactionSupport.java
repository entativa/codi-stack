package io.codibase.server.web.component.comment;

import java.util.Collection;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.codibase.server.model.support.EntityReaction;

public interface ReactionSupport {

    Collection<? extends EntityReaction> getReactions();
	
	void onToggleEmoji(AjaxRequestTarget target, String emoji);

}
