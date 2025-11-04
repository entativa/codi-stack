package io.codibase.server.model.support;

import javax.persistence.*;

import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.User;

@MappedSuperclass
public abstract class EntityReaction extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    public static final String PROP_USER = "user";

    @Column(nullable = false)
    private String emoji;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false)
    private User user;

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    protected abstract AbstractEntity getEntity();
    
} 