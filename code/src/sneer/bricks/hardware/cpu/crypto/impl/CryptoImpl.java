package sneer.bricks.hardware.cpu.crypto.impl;

import static basis.environments.Environments.my;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.Arrays;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JDKKeyFactory;

import sneer.bricks.hardware.cpu.crypto.Crypto;
import sneer.bricks.hardware.cpu.crypto.Digester;
import sneer.bricks.hardware.cpu.crypto.ECBCipher;
import sneer.bricks.hardware.cpu.crypto.Hash;
import sneer.bricks.hardware.cpu.threads.throttle.CpuThrottle;
import basis.lang.ProducerX;
import basis.lang.arrays.ImmutableByteArray;

class CryptoImpl implements Crypto {

	private static final String BOUNCY_CASTLE = "BC";
	private static final int FILE_BLOCK_SIZE = 1024 * 100;

	static {
		Security.addProvider(new BouncyCastleProvider()); //Optimize: remove this static dependency. Use Bouncycastle classes directly
	}

	@Override
	public Hash digest(byte[] input) {
		return newDigester().digest(input);
	}

	@Override
	public Digester newDigester() {
		return new DigesterImpl(messageDigest("SHA-512", BOUNCY_CASTLE));
	}

	private MessageDigest messageDigest(String algorithm, String provider) {
		try {
			return MessageDigest.getInstance(algorithm, provider);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	
	@Override
	public Hash digest(File file) throws IOException {
		if (file.isDirectory()) throw new IllegalArgumentException("The parameter cannot be a directory");

		Digester digester = newDigester();
		FileInputStream input = new FileInputStream(file);
		try {
			byte[] block = new byte[FILE_BLOCK_SIZE];
			for (int numOfBytes = input.read(block); numOfBytes != -1; numOfBytes = input.read(block)) {
				my(CpuThrottle.class).yield();
				digester.update(block, 0, numOfBytes);
			}
		} finally {
			try { input.close(); } catch (Throwable ignore) {}
		}

		return digester.digest();
	}


	@Override
	public Hash unmarshallHash(byte[] bytes) {
		return new Hash(new ImmutableByteArray(bytes));
	}

	
	@Override
	public Signature getSHA512WithECDSA() {
		try {
			return Signature.getInstance("SHA512WITHECDSA", "BC");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	
	@Override
	public KeyPair newECDSAKeyPair(final byte[] seed) {
		return safelyProduce(new ProducerX<KeyPair, Exception>() { @Override public KeyPair produce() throws NoSuchAlgorithmException, NoSuchProviderException {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDSA", BOUNCY_CASTLE);
			generator.initialize(256, new RandomWrapper(mix256bits(seed)));
			return generator.generateKeyPair();
		}});
	}
		

	private byte[] mix256bits(final byte[] seed) {
		byte[] sha512Hash = digest(seed).bytes.copy();
		return Arrays.copyOf(sha512Hash, 32); //32 * 8 = 256
	}
	
	
	private <T> T safelyProduce(ProducerX<T, Exception> producer) {
		try {
			return producer.produce();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ECBCipher newAES256Cipher(byte[] key) {
		return new ECBCipherImpl(key);
	}

	@Override
	public PublicKey retrievePublicKey(final byte[] keyBytes) {
		return safelyProduce(new ProducerX<PublicKey, Exception>() { @Override public PublicKey produce() throws IOException {
			return JDKKeyFactory.createPublicKeyFromDERStream(keyBytes);
		}});
	}
}
