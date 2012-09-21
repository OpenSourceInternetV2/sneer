package snype.whisper.gui.impl;

import static basis.environments.Environments.my;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.JToggleButton;

import sneer.bricks.pulp.reactive.Signal;
import sneer.bricks.skin.main.dashboard.InstrumentPanel;
import sneer.bricks.skin.main.instrumentregistry.InstrumentRegistry;
import sneer.bricks.skin.rooms.ActiveRoomKeeper;
import sneer.bricks.skin.widgets.reactive.ReactiveWidgetFactory;
import sneer.bricks.skin.widgets.reactive.TextWidget;
import snype.whisper.gui.WhisperGui;
import snype.whisper.skin.audio.loopback.LoopbackTester;
import snype.whisper.skin.audio.mic.Mic;
import basis.environments.Environments;
import basis.lang.Consumer;

class WhisperGuiImpl implements WhisperGui {

	private final LoopbackTester _loopback = my(LoopbackTester.class);
	private final InstrumentRegistry _instrumentManager = my(InstrumentRegistry.class);
	private final Mic _mic = my(Mic.class);

	private final JToggleButton _whisperButton = new JToggleButton("whisper");
	private final JToggleButton _loopBackButton = new JToggleButton("loopback");

	private TextWidget<JTextField> _roomField;

	@SuppressWarnings("unused") private Object _referenceToAvoidGc;

	WhisperGuiImpl(){
		_instrumentManager.registerInstrument(this);
	}

	private Signal<Boolean> isRunning() {
		return _mic.isOpen();
	}

	@Override
	public void init(InstrumentPanel window) {
		Container container = window.contentPane();
		container.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		ActiveRoomKeeper room = Environments.my(ActiveRoomKeeper.class);

		_roomField = Environments.my(ReactiveWidgetFactory.class).newTextField(room.room(), room.setter());
		room.setter().consume("<Room>");

		container.add(_roomField.getMainWidget());
		_roomField.getMainWidget().setPreferredSize(new Dimension(100,36)); //Fix: change this to synth
		
		container.add(_whisperButton);
		container.add(_loopBackButton);
		
		createWhisperButtonListener();
		createLoopBackButtonListener();
		
		_referenceToAvoidGc = isRunning().addReceiver(new Consumer<Boolean>() { @Override public void consume(Boolean isRunning) {
			_whisperButton.setSelected(isRunning);
			_roomField.getMainWidget().setEnabled(isRunning);
		}});

	}
	
	@Override
	public int defaultHeight() {
		return 50;
	}

	private void createWhisperButtonListener() {
		_whisperButton.addMouseListener(new MouseAdapter() {	@Override public void mouseReleased(MouseEvent e) {
			if (_whisperButton.isSelected()) whisperOn();
			else whisperOff();
			_whisperButton.setSelected(false);
		}});
	}

	private void createLoopBackButtonListener() {
		_loopBackButton.addMouseListener(new MouseAdapter() {	@Override public void mouseReleased(MouseEvent e) {
			if(_loopBackButton.isSelected()){
				_loopBackButton.setSelected(_loopback.start());
				return;
			}
			_loopback.stop();
		}});
	}

	protected void whisperOff() {
		_mic.close();
	}

	protected void whisperOn() {
		_mic.open();
	}

	@Override
	public String title() {
		return "Whisper";
	}
}