package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Form;

public class CtrlEnterSubmitBehavior extends AjaxFormSubmitBehavior
{
	private static final String EVENT_TYPE = "keypress";
	private static final String CHECK_CTRL_ENTER_SCRIPT = "var e = attrs.event; return (e.keyCode === 10 || e.keyCode === 13) && e.ctrlKey;";

	public CtrlEnterSubmitBehavior(Form<?> form)
	{
		super(form, EVENT_TYPE);
	}

	public CtrlEnterSubmitBehavior()
	{
		super(EVENT_TYPE);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setPreventDefault(false);
		attributes.getAjaxCallListeners().add(new AjaxCallListener().onPrecondition(CHECK_CTRL_ENTER_SCRIPT));
	}

}
