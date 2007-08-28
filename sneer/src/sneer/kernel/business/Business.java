package sneer.kernel.business;

import sneer.kernel.business.contacts.ContactAttributes;
import wheel.graphics.JpgImage;
import wheel.reactive.Signal;
import wheel.reactive.lists.ListSignal;

public interface Business {

	Signal<String> ownName();
	Signal<String> publicKey();
	Signal<String> language();
	
	Signal<String> thoughtOfTheDay();
	Signal<JpgImage> picture();
	Signal<String> profile();
	
	Signal<Integer> sneerPort();

	ListSignal<ContactAttributes> contactAttributes();

}
