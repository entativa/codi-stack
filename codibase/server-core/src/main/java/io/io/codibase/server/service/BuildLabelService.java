package io.codibase.server.service;

import io.codibase.server.model.Build;
import io.codibase.server.model.BuildLabel;

import java.util.List;

public interface BuildLabelService extends EntityLabelService<BuildLabel> {

	void create(BuildLabel buildLabel);

	void populateLabels(List<Build> builds);
}
