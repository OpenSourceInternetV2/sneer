package sneer.skin.sound.speaker.impl;

import static sneer.commons.environments.Environments.my;
import sneer.pulp.reactive.Register;
import sneer.pulp.reactive.Signal;
import sneer.pulp.reactive.Signals;
import sneer.skin.sound.speaker.Speaker;

class SpeakerImpl implements Speaker {
	
	private PacketPlayer _consumer;
	private PacketSubscriber _producer;
	private Register<Boolean> _isRunning = my(Signals.class).newRegister(false);


	@Override
	synchronized public void open() {
		if (_producer != null) return;

		_consumer = new PacketPlayer();
		_producer = new PacketSubscriber(_consumer);
		_isRunning.setter().consume(true);
	}

	
	@Override
	synchronized public void close() {
		_isRunning.setter().consume(false);
		if (_producer == null) return;

		_producer.crash();
		_consumer.crash();

		_producer = null;
		_consumer = null;
	}


	@Override
	public Signal<Boolean> isRunning() {
		return _isRunning.output();
	}
}
