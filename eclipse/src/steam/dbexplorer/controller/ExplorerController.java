/**
 * The explorer controller is used to interface between the model and the view.
 * The controller is in charge of checking that all input taken from the view
 * makes sense, and converting information from the model into a type preferred
 * by the view.
 * 
 * @author Andrew Hollenbach <ahollenbach>
 */

package steam.dbexplorer.controller;

import java.util.HashMap;
import steam.dbexplorer.model.ExplorerModel;

public class ExplorerController {
	
	//TODO populate tableLabels with proper values
	private static final HashMap<String, Object[]> tableLabels = new HashMap<String, Object[]>();
	
	/** 
	 * A string value of the last entity type to be fetched. Might be 
	 * deprecated.
	 */
	private String currentTable;
	
	public ExplorerController() {
	}
	
	public Object[][] getData(String tableName, String[] options) {
		this.currentTable = "";
		return null;
	}

	public Object[] getLabels(String tableName) {
		return tableLabels.get(tableName);
	}
	
	/**
	 * Creates an entry using the given string values. Verifies if the 
	 * values are correct. If any of the values are not parsable, returns
	 * false. 
	 * @param values The values to insert
	 * @param entityName The name of the entity to create.
	 * @return
	 */
	public boolean createEntry(String entityName, String[] values) {
		//TODO this is not actually working code.
		//Has to verify values are parsable i.e. values[0] = "fdfjk234444444"
		Object[] valuesConvertedToObjects = values;
		ExplorerModel.createEntity(entityName,valuesConvertedToObjects);
		return false;
	}
	
	/* TODO make these functions
	public boolean delete*(String entityName, String[] values) {
		return false;
	}
	
	public boolean update*(String entityName, String[] values) {
		return false;
	} */
}
