package sneer.pulp.propertystore.impl;

import static sneer.commons.environments.Environments.my;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import sneer.brickness.StoragePath;
import sneer.commons.io.Streams;
import sneer.pulp.propertystore.PropertyStore;
import wheel.io.Logger;

class PropertyStoreImpl implements PropertyStore {

	private static final String FILE_NAME = "propertystore.txt";


	private final StoragePath _config = my(StoragePath.class);

	
	private final Properties _properties = loadProperties();

	@Override
	public String get(String property) {
		return _properties.getProperty(property);
	}
	
	@Override
	public boolean containsKey(String property) {
		return _properties.containsKey(property);
	}
	
	@Override
	public void set(String property, String value) {
		_properties.setProperty(property, value);
		persist();
	}
	
	private Properties loadProperties() {
		Properties result = new Properties();
		InputStream in = null;
		try {
			in = in();
			result.load(in);
		} catch (FileNotFoundException e) {
			Logger.log("No properties found yet.");
		} catch (IOException e) {
			throw new sneer.commons.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
		} finally {
			if (in != null) Streams.crash(in);
		}
		return result;
	}

	private void persist() {
		try {
			OutputStream out = out();
			try {
				_properties.store(out, "Sneer System Persistence File - Handle with Care :)");
			} finally {
				Streams.crash(out);
			}
		} catch (IOException e) {
			throw new sneer.commons.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
		} 
	}

	private InputStream in() throws IOException {
		Logger.log("Reading Sneer properties file from: {}", FILE_NAME);
		return new FileInputStream(file());
	}

	private OutputStream out() throws IOException {
		return new FileOutputStream(file());
	}

	private File file() {
		return new File(_config.get(), FILE_NAME);
	}

}
