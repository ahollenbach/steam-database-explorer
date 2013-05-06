/**
 * Results.java is a tab that goes into ExplorerView. It is used
 * to view the results of a query defined in the query tab.
 *
 * @author Andrew Hollenbach <anh7216> 
 */

package steam.dbexplorer.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import steam.dbexplorer.controller.ExplorerController;
import steam.dbexplorer.dbobject.DBReference;

public class ResultsTab extends JPanel {
	private ExplorerController controller;
	private QueryTab queryTab;
	private JTabbedPane parent;
	
	private JTable results;
	private JScrollPane scrollPane;
	
	private String currentTable;
	private ArrayList<Integer> rowsChanged = new ArrayList<Integer>();
	private ArrayList<Integer> notEditableColumns = new ArrayList<Integer>();;
	
	public ResultsTab(JTabbedPane parent, ExplorerController controller) {
		super();
		this.controller = controller;
		this.parent = parent;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		initializeTable();
				
		JPanel CUDpanel = createCUDPanel("entry");
		
		this.add(scrollPane);
		this.add(CUDpanel);
	}
	
	private void initializeTable() {
		results = new JTable();
		results.setModel(new DefaultTableModel(controller.getData(null,null), controller.getLabels(null)));
		scrollPane = new JScrollPane(results);
		results.setFillsViewportHeight(true);
	}
	
	public void updateTable(String tableName, Enumeration<String> constraintsEnum) {
		ArrayList<String> constraintsAL = Collections.list(constraintsEnum);
		String[] constraints = new String[constraintsAL.size()];
		constraints = constraintsAL.toArray(constraints);
		Object[][] data = controller.getData(tableName,constraints);
		String[] labels = controller.getLabels(tableName);
		currentTable = tableName;
		
		//find out what columns are editable
		notEditableColumns.clear();
		for(int i=0;i<labels.length;i++) {
			if(DBReference.isPK(currentTable, labels[i])) {
				notEditableColumns.add(i);
			}
		}
		
		DefaultTableModel tableModel = new DefaultTableModel(data,labels) {
		   @Override
		   public boolean isCellEditable(int row, int column) {
			    return !notEditableColumns.contains(column);
		   }
		};
		tableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				rowsChanged.add(event.getFirstRow());
			}
		});
		results.setModel(tableModel);
	}

	private JPanel createCUDPanel(String addDeleteWhat) {
		JPanel p = new JPanel();
		JButton add = new JButton("Add new " + addDeleteWhat);
		add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	String tableName = controller.getCurrentTable();
            	tableName = tableName.substring(0, tableName.length()-1); //remove s
    			tableName = tableName.replace(" ", ""); //remove spaces 
            	new AddEditDialog(new JFrame(),tableName);
            	DefaultTableModel model = (DefaultTableModel) results.getModel();
            	Object[] values = {};
            	model.addRow(values);
            }
        });
		p.add(add);
		JButton update = new JButton("Update entries");
		//update.setEnabled(true);
		update.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	try {
            		JSONObject json = new JSONObject();
	            	for(int row : rowsChanged) {
	            		for(int col=0;col<results.getColumnCount();col++) {
		                	json.put(results.getColumnName(col), 
		                			 results.getValueAt(row, col));
		            	}
	            	}
	            	controller.updateEntity(currentTable, json);
            	} catch(JSONException e) {
	            }
            }
        });
		p.add(update);
		JButton delete = new JButton("Remove " + addDeleteWhat);
		//delete.setEnabled(false);
		delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	try {
            		JSONObject json = new JSONObject();
	            	int curRow = results.getSelectedRow();
	            	for(int col=0;col<results.getColumnCount();col++) {
	                	json.put(results.getColumnName(col), 
	                			 results.getValueAt(curRow, col));
	            	}
	            	controller.deleteEntity(currentTable, json);
            	} catch(JSONException ex) {
            	}
            }
        });
		p.add(delete);
		
		return p;
	}

	public void setQueryPanelRef(QueryTab queryPanel) {
		this.queryTab = queryPanel;
	}
	
	public class Tuple<X,Y> { 
		public final int row; 
		public final int col; 
		public Tuple(int row,int col) { 
			this.row = row; 
			this.col = col; 
		} 
	} 
}
