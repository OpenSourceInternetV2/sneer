package sneer.pulp.datastructures.cache.tests;

import static sneer.commons.environments.Environments.my;

import org.junit.Test;

import sneer.brickness.testsupport.BrickTest;
import sneer.pulp.datastructures.cache.Cache;
import sneer.pulp.datastructures.cache.CacheFactory;

public class CachePerformanceTest extends BrickTest{

	private final Cache<Object> _subject= my(CacheFactory.class).createWithCapacity(3000);
	
	@Test (timeout = 200) 
	public void containsPerformance() {
		populate();
		
		Object obj= new Object();
		for(int i=1; i<=3000;i++) 
			_subject.contains(obj);
	}

	@Test (timeout = 2000) 
	public void handleForPerformance() {
		populate();
		
		Object obj = new Object();
		for(int i=1; i<=3000;i++) 
			_subject.handleFor(obj);
	}

	private void populate() {
		for(int i=1; i<=3000;i++) 
			_subject.keep(new Object());
	}

}
