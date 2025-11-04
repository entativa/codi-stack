package io.codibase.server.web.component.fileupload;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.codibase.server.web.asset.fileupload.FileUploadCssResourceReference;
import io.codibase.server.web.page.base.BaseDependentResourceReference;

public class FileUploadResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public FileUploadResourceReference() {
		super(FileUploadResourceReference.class, "file-upload.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new FileUploadCssResourceReference()));
		return dependencies;
	}

}
