package io.codibase.server.web.component.imagedata.upload;

import io.codibase.server.web.asset.cropper.CropperResourceReference;
import io.codibase.server.web.asset.fileupload.FileUploadCssResourceReference;
import io.codibase.server.web.page.base.BaseDependentCssResourceReference;
import io.codibase.server.web.page.base.BaseDependentResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import java.util.List;

public class ImageUploadFieldResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ImageUploadFieldResourceReference() {
		super(ImageUploadFieldResourceReference.class, "image-upload.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CropperResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new FileUploadCssResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				ImageUploadFieldResourceReference.class, "image-upload.css")));
		return dependencies;
	}

}
