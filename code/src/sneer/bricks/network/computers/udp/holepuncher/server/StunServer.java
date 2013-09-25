package sneer.bricks.network.computers.udp.holepuncher.server;

import java.net.DatagramPacket;

import basis.brickness.Brick;


@Brick
public interface StunServer {

	DatagramPacket[] repliesFor(DatagramPacket received);

	DatagramPacket[] repliesForAlternate(DatagramPacket packet);

}
