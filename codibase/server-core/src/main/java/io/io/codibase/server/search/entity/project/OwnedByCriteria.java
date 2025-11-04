package io.codibase.server.search.entity.project;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.Project;
import io.codibase.server.model.User;
import io.codibase.server.util.criteria.Criteria;

public abstract class OwnedByCriteria extends Criteria<Project> {

    @Nullable
    public abstract User getUser();

}
