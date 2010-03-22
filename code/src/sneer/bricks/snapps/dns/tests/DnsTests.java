package sneer.bricks.snapps.dns.tests;


import static sneer.foundation.environments.Environments.my;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;

import sneer.bricks.hardware.ram.arrays.ImmutableByteArray;
import sneer.bricks.identity.seals.Seal;
import sneer.bricks.network.computers.sockets.connections.ConnectionManager;
import sneer.bricks.network.computers.sockets.connections.ContactSighting;
import sneer.bricks.pulp.events.EventNotifier;
import sneer.bricks.pulp.events.EventNotifiers;
import sneer.bricks.snapps.dns.Dns;
import sneer.bricks.snapps.dns.tests.mock.MockContactSighting;
import sneer.bricks.software.folderconfig.tests.BrickTest;
import sneer.foundation.brickness.testsupport.Bind;

public class DnsTests extends BrickTest {

	@Bind private final ConnectionManager _connectionManager = mock(ConnectionManager.class);
	private final EventNotifier<ContactSighting> _sightingsSource = my(EventNotifiers.class).newInstance();

	{
		checking(new Expectations() {{
			allowing(_connectionManager).contactSightings(); will(returnValue(_sightingsSource.output()));	
		}});	
	}
	
	private final Dns _subject = my(Dns.class);
	
	
	@Test
	public void testDnsOneContactSighted(){
		see("foo", "10.42.10.42");
		Assert.assertArrayEquals(new String[]{"10.42.10.42"}, knownIpsForContact("foo"));
	}
	
	
	@Test
	public void testDnsOneContactSightedTwice(){
		see("foo", "10.42.10.42");
		see("foo", "10.42.10.43");
		Assert.assertArrayEquals(new String[]{"10.42.10.42", "10.42.10.43"}, knownIpsForContact("foo"));
	}
	
	
	@Test
	public void testDnsTwoContactsSighted(){
		see("foo", "10.42.10.42");
		see("bar", "10.42.10.43");
		Assert.assertArrayEquals(new String[]{"10.42.10.42"}, knownIpsForContact("foo"));
		Assert.assertArrayEquals(new String[]{"10.42.10.43"}, knownIpsForContact("bar"));
	}

	
	private Object[] knownIpsForContact(String contact) {
		return  _subject.knownIpsForContact(sealForContact(contact)).currentElements().toArray();
	}

	
	private void see(String contact, String ip) {
		Seal seal = sealForContact(contact);
		_sightingsSource.notifyReceivers(new MockContactSighting(ip, seal));
	}

	
	private Seal sealForContact(String contact) {
		return new Seal(new ImmutableByteArray(contact.getBytes()));
	}

	
}
