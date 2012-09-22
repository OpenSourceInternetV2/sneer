package snype.whisper.skin.audio.mic.impl;

import static basis.environments.Environments.my;
import sneer.bricks.hardware.cpu.lang.contracts.Contract;
import sneer.bricks.hardware.cpu.threads.Threads;
import sneer.bricks.pulp.notifiers.Notifier;
import sneer.bricks.pulp.notifiers.Notifiers;
import sneer.bricks.pulp.notifiers.Source;
import sneer.bricks.pulp.reactive.Register;
import sneer.bricks.pulp.reactive.Signal;
import sneer.bricks.pulp.reactive.Signals;
import sneer.bricks.pulp.retrier.Retrier;
import sneer.bricks.pulp.retrier.RetrierManager;
import sneer.bricks.pulp.retrier.Task;
import snype.whisper.skin.audio.mic.Mic;
import basis.lang.Closure;
import basis.lang.arrays.ImmutableByteArray;
import basis.lang.exceptions.FriendlyException;

class MicImpl implements Mic {

	private final Threads _threads = my(Threads.class);
	private final RetrierManager _retriers = my(RetrierManager.class);
	
	private boolean _isOpenRequested;
	private Contract _stepperContract;
	
	private Register<Boolean> _isOpen = my(Signals.class).newRegister(false);
	private Notifier<ImmutableByteArray> _sound = my(Notifiers.class).newInstance();
	
	@Override
	public Signal<Boolean> isOpen() {
		return _isOpen.output();
	}

	@Override
	synchronized public void open() {
		_isOpenRequested = true;
		startToWorkIfNecessary();
	}
	
	@Override
	synchronized public void close() {
		_isOpenRequested = false;
		wakeUp();
	}

	private void startToWorkIfNecessary() {
		if (_stepperContract != null) return;

		_stepperContract = _threads.startStepping(new Closure() { @Override public void run() {
			work();
		}});
	}
	
	private void work() {
		if (doCapture()) return;
		if (doAcquireLine()) return;

		MicLine.close();
		_isOpen.setter().consume(false);

		synchronized (this) {
			if (!_isOpenRequested) {
				_stepperContract.dispose();
				_stepperContract = null;
				return;
			}
		}
	}

	
	private boolean doAcquireLine() {
		Retrier retrier;
		synchronized (this) {
			if (!isOpenRequested()) return false;

			retrier = _retriers.startRetrier(5000, new Task() { @Override public void execute() throws FriendlyException { //Fix The blinking light turned on by this retrier is redundant with the one turned on by the Audio brick. 
				MicLine.tryToAcquire();
				wakeUp();
			}});
		
			goToSleep();
		}
		
		retrier.giveUpIfStillTrying();
		return isOpenRequested();
	}

	
	private boolean doCapture() {
		if (!isOpenRequested()) return false;
		if (!MicLine.isAquired()) return false;
		
		_sound.notifyReceivers(MicLine.read());
		_isOpen.setter().consume(true);
		
		return true;
	}


	private boolean isOpenRequested() {
		synchronized (this) {
			return _isOpenRequested;
		}
	}

	synchronized private void wakeUp() {
		notify();
	}

	private void goToSleep() {
		_threads.waitWithoutInterruptions(this);
	}

	@Override
	public Source<ImmutableByteArray> sound() {
		return _sound.output();
	}
}