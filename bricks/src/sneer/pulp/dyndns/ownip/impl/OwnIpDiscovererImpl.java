package sneer.pulp.dyndns.ownip.impl;

import java.io.IOException;

import sneer.kernel.container.Inject;
import sneer.pulp.clock.Clock;
import sneer.pulp.dyndns.checkip.CheckIp;
import sneer.pulp.dyndns.ownip.OwnIpDiscoverer;
import sneer.pulp.propertystore.PropertyStore;
import wheel.reactive.Register;
import wheel.reactive.Signal;
import wheel.reactive.impl.RegisterImpl;

class OwnIpDiscovererImpl implements OwnIpDiscoverer {
	
	@Inject
	static private Clock _clock;
	
	@Inject
	static private CheckIp _checkip;
	
	@Inject
	static private PropertyStore _store;

	private final Register<String> _ownIp = new RegisterImpl<String>(null);
	
	private OwnIpDiscovererImpl() {
		_clock.addPeriodicAlarm(11 * 60 * 1000, new Runnable() { @Override public void run() {
			try {
				ipDiscovery();
			} catch (IOException e) {
				throw new wheel.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
			}
		}});
	}

	protected void ipDiscovery() throws IOException {
		final String ip = _checkip.check();
		
		final String current = _store.get("ownIp");
		if (ip.equals(current)) return;
		
		_store.set("ownIp", ip);
		_ownIp.setter().consume(ip);
	}

	@Override
	public Signal<String> ownIp() {
		return _ownIp .output();
	}

}
