package sneer.games.mediawars.mp3sushi;

import sneer.kernel.pointofview.Contact;

public class GameConfiguration {

	public final static int BEGINING = 0;
	public final static int ENDING = 1;
	public final static int RANDOM = 2;

	private String _theme ="";
	private Integer _type = BEGINING;
	private Integer _secondsOfMusic;
	private Integer _secondsToGuess;
	private Integer _rounds;
	private Contact _externalHost;
	
	
	public void setExternalHost(Contact externalHost) {
		_externalHost = externalHost;
	}
	public GameConfiguration(String theme, Integer type,
			Integer secondsOfMusic, Integer secondsToGuess, Integer rounds) {
		super();
		this._theme = theme;
		_type = type;
		_secondsOfMusic = secondsOfMusic;
		_secondsToGuess = secondsToGuess;
		_rounds = rounds;
	}
	public String getTheme() {
		return _theme;
	}
	public Integer getType() {
		return _type;
	}
	public Integer getSecondsOfMusic() {
		return _secondsOfMusic;
	}
	public Integer getSecondsToGuess() {
		return _secondsToGuess;
	}
	public Contact getExternalHost() {
		return _externalHost;
	}
	public Integer getRounds() {
		return _rounds;
	}

	
	public String getSummary() {
		return _theme + 
		" [ " +_secondsOfMusic + " : " + _secondsToGuess + " : " + _rounds + " : " + this.getStringType() + " ] " + 
		((_externalHost == null) ? "" : _externalHost.nick().currentValue());
		
	}
	
	public String getStringType() {
		if (_type == BEGINING) return "begining";
		if (_type == ENDING) return "ending";
		if (_type == RANDOM) return "random";
		return "";
	}
	
}
