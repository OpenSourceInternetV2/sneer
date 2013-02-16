package spikes.lucass.sliceWars.src.logic;

public class Player{
	public static final Player EMPTY = new Player(0);
	public static final Player PLAYER1  =  new Player(1);
	public static final Player PLAYER2  =  new Player(2);
	public static final Player PLAYER3  =  new Player(3);
	public static final Player PLAYER4  =  new Player(4);
	public static final Player PLAYER5  =  new Player(5);
	private int _playerCount;
	private int currentPlayer;
	
	private Player(int player) {
		currentPlayer = player;
	}
	
	public Player(int player, int playerCount) {
		_playerCount = playerCount;
		currentPlayer = player;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Player)) return false;
		Player other = (Player)obj;
		return currentPlayer == other.currentPlayer;
	}
	
	
	public boolean isLastPlayer(){
		return currentPlayer == _playerCount;
	}
	
	public Player next(){
		currentPlayer++;
		if(currentPlayer > _playerCount)
			currentPlayer = 1;
		return new Player(currentPlayer, _playerCount);
	}

	public int getPlayerNumber() {
		return currentPlayer;
	}
}