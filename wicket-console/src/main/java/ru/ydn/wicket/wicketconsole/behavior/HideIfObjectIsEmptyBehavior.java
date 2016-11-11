package ru.ydn.wicket.wicketconsole.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.util.string.Strings;

public class HideIfObjectIsEmptyBehavior extends Behavior {
	
	public static final HideIfObjectIsEmptyBehavior INSTANCE = new HideIfObjectIsEmptyBehavior();

	private static final long serialVersionUID = 1L;

    @Override
    public void onConfigure(Component component) {
    	Object object = component.getDefaultModelObject();
    	boolean visible = object instanceof CharSequence ? !Strings.isEmpty((CharSequence)object) : object!=null;
        component.setVisibilityAllowed(visible);
    }

}
