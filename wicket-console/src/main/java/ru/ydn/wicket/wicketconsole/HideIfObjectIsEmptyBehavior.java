package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.util.string.Strings;

public class HideIfObjectIsEmptyBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

    @Override
    public void onConfigure(Component component) {
        component.setVisibilityAllowed(!Strings.isEmpty(component.getDefaultModelObjectAsString()));
    }

}
