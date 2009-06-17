package sneer.bricks.software.bricks.snappstarter.tests;

import static sneer.foundation.commons.environments.Environments.my;

import org.junit.Ignore;
import org.junit.Test;

import sneer.bricks.software.bricks.snappstarter.SnappStarter;
import sneer.foundation.brickness.StoragePath;
import sneer.foundation.brickness.testsupport.BrickTest;
import sneer.foundation.brickness.testsupport.Contribute;

public class SnappsTest extends BrickTest {


	@Contribute final StoragePath _storagePath = new StoragePath(){@Override public String get() {
		return tmpDirectory().getAbsolutePath();
	}};

	@Ignore
	@Test
	public void findAndLoadSnapps() {
		my(SnappStarter.class);
		
	}
	
}