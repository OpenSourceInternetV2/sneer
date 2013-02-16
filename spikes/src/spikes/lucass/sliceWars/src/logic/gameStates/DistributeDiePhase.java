package spikes.lucass.sliceWars.src.logic.gameStates;

import spikes.lucass.sliceWars.src.logic.Board;
import spikes.lucass.sliceWars.src.logic.BoardCell;
import spikes.lucass.sliceWars.src.logic.Player;


public class DistributeDiePhase implements GameState{
	
	private Board _board;
	private final  int _diceToAdd;
	private Player _currentPlaying;
	private int diceCount;
	
	public DistributeDiePhase(Player currentPlaying,Board board, int diceToAdd) {
		_currentPlaying = currentPlaying;
		_board = board;
		_diceToAdd = diceToAdd;
		diceCount = _diceToAdd;
	}

	public DistributeDiePhase(Player currentPlaying, Board board) {
		this(currentPlaying,board,board.getBiggestLinkedCellCountForPlayer(currentPlaying));
	}

	@Override
	public GameState play(int x, int y) {
		BoardCell cellAtOrNull = _board.getCellAtOrNull(x,y);
		if(cellAtOrNull == null) return this;
		if(!cellAtOrNull.getOwner().equals(_currentPlaying)) return this;
		if(!cellAtOrNull.canAddDie()) return this;
		diceCount --;
		cellAtOrNull.addDie();
		if(diceCount == 0){
			return new AttackPhase(_currentPlaying, _board);
		}
		return this;
	}

	@Override
	public String getPhaseName() {
		return null;
	}

	@Override
	public Player getWhoIsPlaying() {
		return null;
	}

	@Override
	public boolean canPass() {
		return false;
	}

	@Override
	public GameState pass() {
		return null;
	}

}
