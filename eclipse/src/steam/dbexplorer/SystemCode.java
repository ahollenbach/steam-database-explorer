/**
 * The system codes enum is used by the interfaces to convey the result of an
 * action.
 * 
 * System codes are success/failure checkable, and an associated message can
 * be retrieved with each code.
 * 
 * @author Andrew Hollenbach <ahollenbach>
 *
 */

package steam.dbexplorer;

public enum SystemCode {
	
	/*Success codes*/
	/** A generic success code. Try to use a more explicit code if possible. */
	SUCCESS			("The operation was completed successfully."	,true),
	
	/*Error Codes*/
	/** A generic failure code. Try to use a more explicit code if possible. */
	FAILURE			("The operation was completed successfully."	,false),
	
	/*Database codes*/
	//TODO Drill down and return more explicit database errors.
	DB_SUCCESS	("The database operation was completed successfully."	 ,true),
	DB_ERROR	("The database operation was not completed successfully" ,false),
	;
	
	private final String message;
	/**
	 * True if success code
	 * False if error code
	 */
	private final Boolean type;
	
	/**
	 * Creates a system code
	 * @param message The message to associate with the code
	 * @param type Whether the code is a success or failure code.
	 */
	SystemCode(String message, Boolean type) {
		this.message = message;
		this.type = type;
	}
	
	/**
	 * Gets the associated message. 
	 * 
	 * @return A message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Checks if the code is a success code
	 * 
	 * @return True if the code is a success code, otherwise false.
	 */
	public Boolean isSuccess() {
		return type;
	}
}
