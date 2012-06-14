package sneer.bricks.snapps.games.go.impl.gui.game;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.snapps.games.go.impl.TimerFactory;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.BoardPainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.HUDPainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.HoverStonePainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.StonePainter;
import sneer.bricks.snapps.games.go.impl.gui.game.painters.StonesInPlayPainter;
import sneer.bricks.snapps.games.go.impl.logging.GoLogger;
import sneer.bricks.snapps.games.go.impl.logic.BoardListener;
import sneer.bricks.snapps.games.go.impl.logic.GoBoard;
import sneer.bricks.snapps.games.go.impl.logic.GoBoard.StoneColor;
import sneer.bricks.snapps.games.go.impl.logic.ToroidalGoBoard;
import basis.environments.ProxyInEnvironment;

public class GoBoardPanel extends JPanel{

	private static final long serialVersionUID = 1L;

	private static final float CELL_MAX_SIZE = 100;
	private static final float CELL_MIN_SIZE = 5;

	private int _boardSize;
	private float _boardImageSize;
	private final Rectangle _boardImageRectangle = new Rectangle(0,0,(int)_boardImageSize,(int)_boardImageSize);
	private float _cellSize;
	private final GoBoard _board;

	private BufferedImage _bufferImage;

	private final StoneColor _side;

	private BoardPainter _boardPainter;
	private HoverStonePainter _hoverStonePainter;
	private StonesInPlayPainter _stonesInPlayPainter;
	private HUDPainter _hudPainter;
	
	private int _xOffset;
	private int _yOffset;

	@SuppressWarnings("unused")
	private WeakContract _referenceToAvoidGc;

	private final GuiPlayer _goFrame;

	private StonePainter _stonePainter;

	private boolean _hasResigned = false;
	private boolean _otherResigned = false;
	
	private int scrollX = 0;
	private int scrollY = 0;
	
	public GoBoardPanel(final GuiPlayer goFrame,final TimerFactory timerFactory,final int boardSize, StoneColor side) {
		_goFrame = goFrame;		
		_side = side;
		
		_boardSize = boardSize;
		_board = new ToroidalGoBoard(_boardSize);
		
		createPainters();
		
		addMouseListener();
		
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {
			
        	private static final int SCROLL_KEYBOARD_SPEED = 10;
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if(e.isControlDown()){
					if(e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS ){
						updateCellSize(1);
					}
					if(e.getKeyCode() == KeyEvent.VK_MINUS){
						updateCellSize(-1);
					}
				}
				if(e.getKeyCode() == KeyEvent.VK_UP){
					increaseYOffset(-SCROLL_KEYBOARD_SPEED);
				}
				if(e.getKeyCode() == KeyEvent.VK_DOWN){
					increaseYOffset(SCROLL_KEYBOARD_SPEED);
				}
				if(e.getKeyCode() == KeyEvent.VK_LEFT){
					increaseXOffset(-SCROLL_KEYBOARD_SPEED);
				}
				if(e.getKeyCode() == KeyEvent.VK_RIGHT){
					increaseXOffset(SCROLL_KEYBOARD_SPEED);
				}
				return false;
			}
		});
		
		_referenceToAvoidGc = timerFactory.wakeUpEvery(30, new Runnable() {@Override public void run() {
			update();
			repaint();
		}});    	
	}
	
	public void update(){
		increaseXOffset(scrollX);
		increaseYOffset(scrollY);
	}
	
	@Override
	public void paint(final Graphics graphics) {
		Graphics2D buffer = getBuffer();
		_boardPainter.draw(buffer);
		_hoverStonePainter.draw(buffer, _board);
		_stonesInPlayPainter.draw(buffer, _board);
		drawBoardTiled(graphics);
		_hudPainter.draw(graphics);
	}

	public int scoreWhite() {
		return _board.whiteScore();
	}

	public int scoreBlack() {
		return _board.blackScore();
	}

	public void setBoardListener(BoardListener boardListener) {
		_board.setBoardListener(boardListener);
	}

	public float getCellSize() {
		return _cellSize;
	}

	public void setLostByReign() {
		_hasResigned = true;
	}

	void receiveMoveAddStone(int xCoordinate, int yCoordinate) {
		GoLogger.log("GoBoardPanel.receiveMoveAddStone("+xCoordinate+","+yCoordinate+")");
		_board.playStone(xCoordinate, yCoordinate);
		decideWinner();
	}

	void receiveMoveMarkStone(int xCoordinate, int yCoordinate) {
		GoLogger.log("GoBoardPanel.receiveMoveMarkStone("+xCoordinate+","+yCoordinate+")");
		_board.toggleDeadStone(xCoordinate, yCoordinate);
		decideWinner();
	}

	void receiveMovePassTurn() {
		GoLogger.log("GoBoardPanel.receiveMovePassTurn()");
		_board.passTurn();
		decideWinner();
	}

	void receiveMoveResign() {
		if(!_hasResigned)
			_otherResigned = true;
		GoLogger.log("GoBoardPanel.receiveMoveResign()");
		_board.resign();
		decideWinner();
	}

	private void createPainters() {
		_cellSize = 40;
		_boardImageSize = _cellSize*(_boardSize);
		_boardImageRectangle.width =(int) _boardImageSize;
		_boardImageRectangle.height =(int) _boardImageSize;
		_xOffset = (int) (_cellSize - _boardImageSize);
		_yOffset = (int) (_cellSize - _boardImageSize);
		
		_boardPainter = new BoardPainter(_boardSize, _boardImageSize, _cellSize);
		_stonePainter = new StonePainter(_boardImageSize, _cellSize);
		_hoverStonePainter = new HoverStonePainter(_stonePainter,_boardSize, _cellSize);		
		_stonesInPlayPainter = new StonesInPlayPainter(_stonePainter,_cellSize);
		_hudPainter = new HUDPainter();
	}
	
	private void updateCellSize(int add) {
		float newCellSize = _cellSize + add;
		if(newCellSize > CELL_MAX_SIZE){
			newCellSize = CELL_MAX_SIZE;
		}
		if(newCellSize < CELL_MIN_SIZE){
			newCellSize = CELL_MIN_SIZE;
		}
		
		float oldBoardImageSize = _boardImageSize;
		
		_cellSize = newCellSize;
		_boardImageSize = _cellSize*(_boardSize);
		_boardImageRectangle.width =(int) _boardImageSize;
		_boardImageRectangle.height =(int) _boardImageSize;
		
		_xOffset = (int) ((_xOffset / oldBoardImageSize)*_boardImageSize);
		_yOffset = (int) ((_yOffset / oldBoardImageSize)*_boardImageSize);
		
		_boardPainter.setBoardDimensions(_boardSize, _boardImageSize, _cellSize);
		_stonePainter.setBoardDimensions(_boardImageSize, _cellSize);
		_hoverStonePainter.setBoardDimensions(_boardSize, _cellSize);
		_stonesInPlayPainter.setBoardDimensions(_cellSize);
	}
	private void doMoveAddStone(int x, int y) {
		GoLogger.log("GoBoardPanel.doMoveAddStone("+x+","+y+")");
		_goFrame.doMoveAddStone(x,y);
		decideWinner();
	}
	
	private void doMoveMarkStone(int x, int y) {
		GoLogger.log("GoBoardPanel.doMoveMarkStone("+x+","+y+")");
		_goFrame.doMoveMarkStone(x,y);
		decideWinner();
	}
	
	private void addMouseListener() {
		Object listener = ProxyInEnvironment.newInstance(new GoMouseListener());
		addMouseListener((MouseListener) listener);
	    addMouseMotionListener((MouseMotionListener) listener);
	    addMouseWheelListener((MouseWheelListener) listener);
	}
	
	private void decideWinner() {
		int winState = HUDPainter.NOONE_WIN;
		if (_board.nextToPlay()==null){
			int scoreWhite=scoreWhite();
			int scoreBlack=scoreBlack();
			boolean isNotADraw = scoreWhite!=scoreBlack;
			if (isNotADraw){
				boolean isWinner=false;
				if (_side==StoneColor.WHITE){
					isWinner=(scoreWhite>scoreBlack);
				}else{
					isWinner=(scoreWhite<scoreBlack);
				}
				if(isWinner){
					winState = HUDPainter.PLAYER_WIN;
				}else{
					winState = HUDPainter.PLAYER_LOSES;
				}
			}
		}
		
		if(_hasResigned){
			winState = HUDPainter.PLAYER_LOSES;
		}
		if(_otherResigned){
			winState = HUDPainter.PLAYER_WIN;
		}
		
		_hudPainter.setWinState(winState);
		
	}

	private void drawBoardTiled(Graphics graphics) {
		final Rectangle clipBounds = graphics.getClipBounds();
		
		_boardImageRectangle.x = _xOffset;
		_boardImageRectangle.y = _yOffset;
		int count = 0;
		
		while(clipBounds.intersects(_boardImageRectangle)){
			while(clipBounds.intersects(_boardImageRectangle)){
				count++;
				graphics.drawImage(_bufferImage, _boardImageRectangle.x, _boardImageRectangle.y, this);
				_boardImageRectangle.x += (_boardImageSize+1);
			}
			_boardImageRectangle.x = _xOffset;
			_boardImageRectangle.y += (_boardImageSize+1);
		}
		if(count == 0){
			System.out.println(count);
		}
	}
	
	private Graphics2D getBuffer() {
		_bufferImage = new BufferedImage((int)(_boardImageSize+_cellSize), (int)(_boardImageSize+_cellSize), 
			      BufferedImage.TYPE_INT_ARGB);
		return (Graphics2D)_bufferImage.getGraphics();
	}

	
	private int toScreenPosition(final int coordinate) {
		int coordinateInsideBoard = (int) (coordinate %  _boardImageSize);
		float result = (coordinateInsideBoard -  (_cellSize / 2)) / _cellSize;
		return (int)Math.ceil(result)%_boardSize;
	}
	
	public void increaseXOffset(final float xIncrease){
		_xOffset += xIncrease - _boardImageSize;
		_xOffset = (int) (_xOffset % _boardImageSize);
	}

	public void increaseYOffset(final float yIncrease){
		_yOffset += yIncrease - _boardImageSize;
		_yOffset = (int) (_yOffset % _boardImageSize);
	}
	
	private class GoMouseListener extends MouseAdapter {
		private static final int SCROLL_SPEED = 5;
		private static final int SCROLL_EDGE = 60;
		private int _startX;
		private int _startY;

		@Override 
		public void mouseMoved(final MouseEvent e) {
			final int mouseX = e.getX();
			final int mouseY = e.getY();
			
			_hoverStonePainter.setHoverX(toScreenPosition(mouseX-_xOffset));
			_hoverStonePainter.setHoverY(toScreenPosition(mouseY-_yOffset));
			
			scrollIfOnScrollRegion(mouseX, mouseY);
			
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			stopScrolling();
		}

		private void stopScrolling() {
			scrollX = 0;
			scrollY = 0;
		}
		
		private void scrollIfOnScrollRegion(final int mouseX, final int mouseY) {
			scrollHorizontallyIfOnScrollRegion(mouseX);
			scrollVerticallyIfOnScrollRegion(mouseY);
		}

		private void scrollHorizontallyIfOnScrollRegion(final int mouseX) {
			if(mouseX < SCROLL_EDGE){
				scrollX = -SCROLL_SPEED;
				return;
			}
			if(mouseX > getWidth()-SCROLL_EDGE){
				scrollX = SCROLL_SPEED;
				return;
			}
			scrollX = 0;
		}
		
		private void scrollVerticallyIfOnScrollRegion(final int mouseY) {
			if(mouseY < SCROLL_EDGE){
				scrollY = -SCROLL_SPEED;
				return;
			}
			if(mouseY > getHeight()-SCROLL_EDGE){
				scrollY = SCROLL_SPEED;
				return;
			}
			scrollY = 0;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			final int mouseX = e.getX();
			final int mouseY = e.getY();
			
			if(!dragActionButtonsPressed(e))return;
			
			increaseXOffset(mouseX - _startX);
			increaseYOffset(mouseY - _startY);
			
			_startX = mouseX;
			_startY = mouseY;
			repaint();
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(dragActionButtonsPressed(e)){
				_startX = e.getX();
				_startY = e.getY();
			}
		}

		private boolean dragActionButtonsPressed(MouseEvent e) {
			boolean middleMouseButton = SwingUtilities.isMiddleMouseButton(e);
			boolean ctrlClick =  e.isControlDown() && SwingUtilities.isLeftMouseButton(e);
			return middleMouseButton || ctrlClick;
		}
		
		@Override 
		public void mouseReleased(MouseEvent e) {
			if(dragActionButtonsPressed(e)) return;
			
			int x = toScreenPosition(e.getX()-_xOffset);
			int y = toScreenPosition(e.getY()-_yOffset);
			if (_board.nextToPlay()==null) {
				doMoveMarkStone(x, y);
				return;
			}
			if (!_board.canPlayStone(x, y)) return;
			if (_side != _board.nextToPlay()) return;
			doMoveAddStone(x, y);
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			final int wheelRotation = e.getWheelRotation();
			int factor = 2;
			updateCellSize(wheelRotation*factor*-1);
		}
	}

}
