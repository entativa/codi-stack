package io.codibase.server.web.page.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.codibase.server.web.asset.align.AlignResourceReference;
import io.codibase.server.web.asset.autosize.AutoSizeResourceReference;
import io.codibase.server.web.asset.bootstrap.BootstrapResourceReference;
import io.codibase.server.web.asset.clipboard.ClipboardResourceReference;
import io.codibase.server.web.asset.cookies.CookiesResourceReference;
import io.codibase.server.web.asset.perfectscrollbar.PerfectScrollbarResourceReference;
import io.codibase.server.web.asset.tippy.TippyResourceReference;

public class BaseResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public BaseResourceReference() {
		super(BaseResourceReference.class, "base.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<HeaderItem>();
		
	    dependencies.add(JavaScriptHeaderItem.forReference(Application.get()
	    		.getJavaScriptLibrarySettings().getJQueryReference()));		
	    dependencies.add(JavaScriptHeaderItem.forReference(new BootstrapResourceReference()));
	    dependencies.add(JavaScriptHeaderItem.forReference(new AlignResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new TippyResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AutoSizeResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new PerfectScrollbarResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		
		dependencies.add(CssHeaderItem.forReference(new BaseCssResourceReference()));
		
		return dependencies;
	}

}
