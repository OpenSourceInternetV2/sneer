package sneer.apps.draw;

import static wheel.i18n.Language.translate;

import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import sneer.apps.draw.gui.DrawFrame;
import sneer.kernel.business.contacts.Contact;
import sneer.kernel.business.contacts.ContactId;
import sneer.kernel.communication.Channel;
import sneer.kernel.communication.Packet;
import sneer.kernel.gui.contacts.ContactAction;
import wheel.io.ui.User;
import wheel.io.ui.User.ConfirmCallback;
import wheel.lang.Omnivore;
import wheel.reactive.Signal;
import wheel.reactive.Source;
import wheel.reactive.SourceImpl;
import wheel.reactive.lists.ListSignal;

public class DrawApp {

	private static final String OPEN_REQUEST = "Request";

	private static final String OPEN_REQUEST_ACCEPTED = "Accepted";

	private static final String OPEN_REQUEST_DENIED = "Denied";

	private static final String OPEN_REQUEST_TIMEOUT = "Timeout";

	private static final String CLOSE_REQUEST = "Close";

	public DrawApp(User user, Channel channel, ListSignal<Contact> contacts) {
		_user = user;
		_channel = channel;
		_contacts = contacts;
		_channel.input().addReceiver(drawPacketReceiver());
	}

	private final User _user;

	private final Channel _channel;

	private final ListSignal<Contact> _contacts;

	private final Map<ContactId, DrawFrame> _framesByContactId = new HashMap<ContactId, DrawFrame>();

	private final Map<ContactId, SourceImpl<DrawPacket>> _inputsByContactId = new HashMap<ContactId, SourceImpl<DrawPacket>>();

	public ContactAction contactAction() {
		return new ContactAction() {

			@Override
			public void actUpon(Contact contact) {
				actUponContact(contact);
			}

			@Override
			public String caption() {
				return translate("Draw");
			}

		};
	}

	private Omnivore<Packet> drawPacketReceiver() {
		return new Omnivore<Packet>() {
			public void consume(Packet packet) {
				if (OPEN_REQUEST.equals(packet._contents)) {
					userWantsToOpen(packet._contactId);
					return;
				}

				if (CLOSE_REQUEST.equals(packet._contents)) {
					close(packet._contactId);
					return;
				}

				if (OPEN_REQUEST_ACCEPTED.equals(packet._contents)) {
					open(packet._contactId);
					return;
				}

				if (OPEN_REQUEST_DENIED.equals(packet._contents)) {
					modelessOptionPane("Information", "Draw request denied."); //Refactor: change messages
					return;
				}
				
				if (OPEN_REQUEST_TIMEOUT.equals(packet._contents)) {
					modelessOptionPane("Information", "Draw request expired."); //Refactor: change messages
					return;
				}

				Source<DrawPacket> input = getInputFor(packet._contactId);
				if (input == null)
					return;
				input.setter().consume((DrawPacket) packet._contents);
			}
		};

	}

	private void modelessOptionPane(String title, String message) { //Refactor: move this to User.
		JOptionPane pane = new JOptionPane(message);
		Dialog dialog = pane.createDialog(title);
		dialog.setModalityType(ModalityType.MODELESS);
		dialog.setVisible(true);
	}

	private void userWantsToOpen(final ContactId contactId) {
		String nick = findContact(contactId).nick().currentValue();
		_user.confirmWithTimeout(translate("%1$s wants to draw with you.\n\nDo you want to accept this call?", nick), 15, new ConfirmCallback() {
							public void response(final Object response) {
								if (response.equals(User.TIMEOUT_EXPIRED_RESPONSE)) {
									sendTo(contactId, OPEN_REQUEST_TIMEOUT);
									return;
								}
								if (response.equals(JOptionPane.YES_OPTION)) {
									open(contactId);
									sendTo(contactId, OPEN_REQUEST_ACCEPTED);
									return;
								}
								sendTo(contactId, OPEN_REQUEST_DENIED);
							}
						});
	}

	private void close(ContactId contactId) {
		_inputsByContactId.remove(contactId);
		DrawFrame frame = _framesByContactId.remove(contactId);
		if (frame != null)
			frame.close();
	}

	private void open(ContactId contactId) {
		createInputFor(contactId);
		createFrameFor(contactId);
		findContact(contactId).isOnline().addReceiver(offlineCloser(contactId));
	}

	private Omnivore<Boolean> offlineCloser(final ContactId contactId) {
		return new Omnivore<Boolean>() {
			@Override
			public void consume(Boolean isOnline) {
				if (!isOnline)
					close(contactId);
			}
		};
	}

	private void actUponContact(Contact contact) {
		if (getInputFor(contact.id()) != null)
			return;
		sendTo(contact.id(), OPEN_REQUEST);
	}

	private void sendTo(ContactId contactId, Object contents) {
		_channel.output().consume(new Packet(contactId, contents));
	}

	private void createFrameFor(ContactId contactId) {
		DrawFrame frame = new DrawFrame(findContact(contactId).nick(), inputFrom(contactId), outputTo(contactId));
		frame.addWindowListener(closingListener(contactId));
		_framesByContactId.put(contactId, frame);
	}

	private WindowListener closingListener(final ContactId contactId) {
		return new WindowAdapter() { @Override
			public void windowClosing(WindowEvent ignored) {
				close(contactId);
				sendTo(contactId, CLOSE_REQUEST);
			}
		};
	}

	private Contact findContact(ContactId id) {
		for (Contact candidate : _contacts)
			if (candidate.id().equals(id))
				return candidate;
		return null;
	}

	private Signal<DrawPacket> inputFrom(ContactId contactId) {
		return getInputFor(contactId).output();
	}

	private Source<DrawPacket> getInputFor(ContactId contactId) {
		return _inputsByContactId.get(contactId);
	}

	private void createInputFor(ContactId contactId) {
		SourceImpl<DrawPacket> input = new SourceImpl<DrawPacket>(null);
		_inputsByContactId.put(contactId, input);
	}

	private Omnivore<DrawPacket> outputTo(final ContactId contactId) {
		return new Omnivore<DrawPacket>() {
			public void consume(DrawPacket audioPacket) {
				_channel.output().consume(new Packet(contactId, audioPacket));
			}
		};
	}

}
