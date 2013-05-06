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
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import steam.dbexplorer.controller.ExplorerController;

public class ResultsTab extends JPanel {
	private ExplorerController controller;
	private QueryTab queryTab;
	private JTabbedPane parent;
	
	private JTable results;
	private JScrollPane scrollPane;
	
	private String currentTable;
	
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
		results.setModel(new DefaultTableModel(data,labels));
		currentTable = tableName;
	}

	private JPanel createCUDPanel(String addDeleteWhat) {
		JPanel p = new JPanel();
		JButton add = new JButton("Add new " + addDeleteWhat);
		add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	DefaultTableModel model = (DefaultTableModel) results.getModel();
            	Object[] values = {};
            	model.addRow(values);
            }
        });
		p.add(add);
		JButton update = new JButton("Update entries");
		update.setEnabled(false);
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
}
