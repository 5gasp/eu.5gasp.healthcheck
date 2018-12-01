package centralLog.api;

public class CentralLogMessage {

	CLevel cLevel;
	
	String message;

	/**
	 * 
	 */
	public CentralLogMessage() {

	}
		
	/**
	 * @param cLevel
	 * @param message
	 */
	public CentralLogMessage(CLevel cLevel, String message) {
		super();
		this.cLevel = cLevel;
		this.message = message;
	}


	/**
	 * @return the cLevel
	 */
	public CLevel getcLevel() {
		return cLevel;
	}

	/**
	 * @param cLevel the cLevel to set
	 */
	public void setcLevel(CLevel cLevel) {
		this.cLevel = cLevel;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
