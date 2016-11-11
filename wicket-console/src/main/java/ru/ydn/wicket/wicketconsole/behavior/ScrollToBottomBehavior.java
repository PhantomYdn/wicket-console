package ru.ydn.wicket.wicketconsole.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public class ScrollToBottomBehavior extends Behavior
{
	public static final ScrollToBottomBehavior INSTANCE = new ScrollToBottomBehavior();

	@Override
	public void bind(Component component) {
		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		String markupId = component.getMarkupId();
		response.render(OnDomReadyHeaderItem.forScript("var c = $('#"+markupId+"'); c.scrollTop(c.prop('scrollHeight'));"));
	}
	
}
