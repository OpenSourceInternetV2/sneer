package sneer.bricks.hardware.cpu.algorithms.crypto;

public interface Digester {

	void update(byte[] bytes);

	void update(byte[] bytes, int offset, int length);

	Sneer1024 digest();

}
