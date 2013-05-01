/**
 * QueryTab.java is a tab that goes into ExplorerView. It is used
 * to select the table to view and constraints to apply to that 
 * table.
 *
 * @author Andrew Hollenbach <anh7216> 
 */

package steam.dbexplorer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import steam.dbexplorer.controller.ExplorerController;

public class QueryTab extends JPanel {
	JComboBox currentTableName;
	JComboBox curType;
	JComboBox curAttribute;
	JComboBox curOperation;
	JTextField curVal;
	JPanel selectionPanel;
	private ExplorerController controller;
	private ResultsTab resultsTab;
	private JTabbedPane parent;
	
	public QueryTab(JTabbedPane parentPane, ExplorerController controller) {
		super();
		this.controller = controller;
		this.parent = parentPane;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel pickTable = createPickTable();
		
		/* TODO select only particular attributes, i.e. select only steamID and 
		 * personaName of player
		JPanel selectAttributes = new JPanel();
		JTextField attrTitle = new JTextField("Select attributes to retrieve");
		pickTable.add(title1); 		*/
		JPanel constraintsPanel = createConstraintsPanel();
		
		selectionPanel = createSelectionPanel();
		JButton runQuery = new JButton ("Run");
		runQuery.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	String table = (String) currentTableName.getSelectedItem();
            	resultsTab.updateTable(table);
            	parent.setSelectedIndex(1);
            }
        });	
		
		this.add(pickTable);
		this.add(constraintsPanel);
		this.add(selectionPanel);
		this.add(runQuery);
	}

	
	private JPanel createSelectionPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JPanel type = createLabelComboBoxPair("Type: ",ExplorerController.supportedClauses,curType);
		JPanel attr = createLabelComboBoxPair("Attribute: ",controller.getAttr((String) currentTableName.getSelectedItem()),curAttribute);
		JPanel ops  = createLabelComboBoxPair("Operator: ",ExplorerController.operators, curOperation);
		
		JPanel value  = new JPanel();
		value.add(new JLabel("Value: "));
		//String curAttr = (String) curAttribute.getSelectedItem();
		curVal = new JTextField(15);
		value.add(curVal);
		value.setMaximumSize(new Dimension(800,60));
		
		p.add(type);
		p.add(attr);
		p.add(ops);
		p.add(value);
		p.add(new JPanel()); //spacer
		p.setVisible(false);
		return p;
	}

	private JPanel createLabelComboBoxPair(String title, String[] supportedclauses,JComboBox cb) {
		JPanel p = new JPanel();
		JLabel label = new JLabel(title);
		cb = new JComboBox(supportedclauses);
		
		p.add(label);
		p.add(cb);
		
		p.setMaximumSize(new Dimension(800,60));
		return p;
	}
	
	@SuppressWarnings("unused")
	private JPanel createLabelComboBoxPair(String title, String[] supportedclauses) {
		return createLabelComboBoxPair(title, supportedclauses, new JComboBox());
	}


	private JPanel createPickTable() {
		JPanel pickTable = new JPanel();
		JLabel pickTableTitle = new JLabel("Choose a table");
		currentTableName = new JComboBox(ExplorerController.tableNames);
		//add listener on tablename change
		pickTable.add(pickTableTitle); 
		pickTable.add(currentTableName);
		pickTable.setMaximumSize(new Dimension(800,200));
		return pickTable;
	}


	private JPanel createConstraintsPanel() {
		JPanel cPanel = new JPanel();
		cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.Y_AXIS));
		
		JLabel title = new JLabel("Set constraints");
		title.setMaximumSize(new Dimension(100,50));
		
		JList constraintsList = new JList(getCurrentConstraints());
		constraintsList.setMinimumSize(new Dimension(800,200));
		constraintsList.setMaximumSize(new Dimension(800,200));
		constraintsList.setPreferredSize(new Dimension(800,200));
		
		JPanel cudConstraints = createCUDPanel("constraint");
		
		cPanel.add(title);
		cPanel.add(constraintsList);
		cPanel.add(cudConstraints);
		
		cPanel.setMinimumSize(new Dimension(800,260));
		cPanel.setMaximumSize(new Dimension(800,260));
		cPanel.setPreferredSize(new Dimension(800,260));
		
		//cPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		return cPanel;
	}

	private String[] getCurrentConstraints() {
		String[] constraints = {"where steamID = 5","sort on personaName ascending"};
		return constraints;
	}
	
	private JPanel createCUDPanel(String addDeleteWhat) {
		JPanel p = new JPanel();
		JButton add = new JButton("Add new " + addDeleteWhat);
		add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	selectionPanel.setVisible(true);
            }
        });		
		p.add(add);
		JButton edit = new JButton("Edit " + addDeleteWhat);
		edit.setEnabled(false);
		p.add(edit);
		JButton delete = new JButton("Remove " + addDeleteWhat);
		delete.setEnabled(false);
		p.add(delete);
		JButton clear = new JButton("Clear " + addDeleteWhat + "s");
		clear.setEnabled(false);
		p.add(clear);
		
		return p;
	}


	public void setResultsPanelRef(ResultsTab resultPanel) {
		this.resultsTab = resultPanel;
	}
}
