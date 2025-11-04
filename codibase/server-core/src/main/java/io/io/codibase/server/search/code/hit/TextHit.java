package io.codibase.server.search.code.hit;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import io.codibase.commons.jsymbol.util.HighlightableLabel;
import io.codibase.commons.jsymbol.util.NoAntiCacheImage;
import io.codibase.commons.utils.LinearRange;
import io.codibase.commons.utils.PlanarRange;

public class TextHit extends QueryHit {

	private static final long serialVersionUID = 1L;
	
	private final String line;
	
	public TextHit(String blobPath, PlanarRange hitPos, String line) {
		super(blobPath, hitPos);
		this.line = line;
	}

	public String getLine() {
		return line;
	}
	
	@Override
	public Component render(String componentId) {
		if (getHitPos() != null) {
			return new HighlightableLabel(componentId, line, 
					new LinearRange(getHitPos().getFromColumn(), getHitPos().getToColumn()));
		} else {
			return new HighlightableLabel(componentId, line, null);
		}
	}

	@Override
	public Image renderIcon(String componentId) {
		return new NoAntiCacheImage(componentId, new PackageResourceReference(FileHit.class, "bullet.png"));
	}

	@Override
	public String getNamespace() {
		String fileName = getBlobPath();
		if (fileName.contains("/")) 
			fileName = StringUtils.substringAfterLast(fileName, "/");
		return fileName;
	}

}
