package sneer.skin.widgets.reactive;

import javax.swing.JComponent;

import sneer.pulp.reactive.Signal;
import sneer.software.lang.PickyConsumer;


public interface TextWidget<WIDGET extends JComponent> extends ComponentWidget<WIDGET> {

	Signal<?> output();
	
	PickyConsumer<? super String> setter();	

	JComponent[] getWidgets();
}