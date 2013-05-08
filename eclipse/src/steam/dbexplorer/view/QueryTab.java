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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import steam.dbexplorer.controller.ExplorerController;

public class QueryTab extends JPanel {
	private JComboBox currentTableName;
	private JComboBox curType;
	private JComboBox curAttribute;
	private JComboBox curOrder;
	private JPanel order;
	private JComboBox curOperation;
	private JPanel ops;
	private JTextField curVal;
	private JPanel value;
	private JPanel selectionPanel;
	private ExplorerController controller;
	private ResultsTab resultsTab;
	private JTabbedPane parent;
	private JList constraintsList;
	private JButton delete;
	private JButton clear;
	//private JButton edit;
	
	//HashTable<queryPart,humanReadablePart>
	private Hashtable<String,String> currentConstraints = new Hashtable<String,String>();
	
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
            	resultsTab.updateTable(table,currentConstraints.keys());
            	parent.setSelectedIndex(1);
            }
        });	
		
		this.add(pickTable);
		this.add(constraintsPanel);
		this.add(selectionPanel);
		this.add(new JPanel()); //spacer
		this.add(runQuery);
	}

	
	private JPanel createSelectionPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		JPanel attr = createLabelComboBoxPair("Attribute: ",curAttribute = new JComboBox(controller.getLabels((String) currentTableName.getSelectedItem())));
		curAttribute.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	String table = (String) currentTableName.getSelectedItem();
            }
        });
		order  = createLabelComboBoxPair("Order: ", curOrder = new JComboBox(ExplorerController.supportedOrders));
		order.setVisible(false);
		ops  = createLabelComboBoxPair("Operator: ", curOperation = new JComboBox(ExplorerController.operators));
		
		value  = new JPanel();
		value.add(new JLabel("Value: "));
		curVal = new JTextField(15);
		value.add(curVal);
		value.setMaximumSize(new Dimension(800,60));
		
		JPanel type = createLabelComboBoxPair("Type: ",curType = new JComboBox(ExplorerController.supportedClauses));
		curType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	if(((String) curType.getSelectedItem()).equals("Sort by")) {
            		ops.setVisible(false);
            		value.setVisible(false);
            		
            		order.setVisible(true);
            	} else {
            		ops.setVisible(true);
            		value.setVisible(true);
            		
            		order.setVisible(false);
            	}
            }
        });

		
		JPanel actions = new JPanel();
		JButton commitButton = new JButton("Go");
		commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	// - q prefix means it is used for the query
            	// - no prefix means it is used for human readable format or is
            	//   used for both
            	String table = (String) currentTableName.getSelectedItem();
            	String type = (String) curType.getSelectedItem();
            	String attr = (String) curAttribute.getSelectedItem();
            	if(((String) curType.getSelectedItem()).equals("Where")) {
	            	String op   = (String) curOperation.getSelectedItem();
	            	String qOp   = controller.opEquivs[curOperation.getSelectedIndex()];
	            	String val  = curVal.getText();
	            	
	            	if(val.length() == 0) {
	            		JOptionPane.showMessageDialog(null,
	            			    "Please fill out all values.",
	            			    "Value Missing!",
	            			    JOptionPane.ERROR_MESSAGE);
	            		return;
	            	} 
	            	if("string".equals(controller.getAttrType(attr))){
	            		val = "\'" + val + "\'";
	            	}
	            	// #breakinMVC #yolo #TODO refactor
	            	//String qString = type + " " + controller.convertToDbAttr(attr) + qOp + val;
	            	String qString = " " + controller.convertToDbAttr(attr) + qOp + val;
	            	String displayString = type + " " + attr + " is " + op + " " + val;
	            	currentConstraints.put(qString, displayString);
            	} else { //sort by
            		String order   = (String) curOrder.getSelectedItem();
            		String qOrd = null;
            		if(order.equals("Ascending")) {
            			qOrd = "asc";
            		} else {
            			qOrd = "desc";
            		}
            		
	            	String qString = "sort=" + controller.convertToDbAttr(attr) + " " + qOrd;
	            	String displayString = "Sort by " + attr + " in " + order + " order";
	            	
	            	//check if you are already sorting on this attribute.
	            	Enumeration<String> keys = currentConstraints.keys();
	            	while(keys.hasMoreElements()) {
	            		String key = keys.nextElement();
	            		if(key.contains("sort=" + controller.convertToDbAttr(attr))) {
	            			JOptionPane.showMessageDialog(null,
		            			    "You are already sorting on this attribute!",
		            			    "Unable to add sort!",
		            			    JOptionPane.ERROR_MESSAGE);
	            			return;
	            		}
	            	}
	            	currentConstraints.put(qString, displayString);
            	}
            	
            	updateConstraints();
            	selectionPanel.setVisible(false);
            	
        		//edit.setEnabled(false);
        		delete.setEnabled(false);
            }
        });	
		actions.add(commitButton);
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	selectionPanel.setVisible(false);
            }
        });
		actions.add(cancel);
		
		p.add(type);
		p.add(attr);
		p.add(order);
		p.add(ops);
		p.add(value);
		p.add(actions);
		p.setVisible(false);
		return p;
	}

	private JPanel createLabelComboBoxPair(String title, JComboBox cb) {
		JPanel p = new JPanel();
		JLabel label = new JLabel(title);
		
		p.add(label);
		p.add(cb);
		
		p.setMaximumSize(new Dimension(800,60));
		return p;
	}
	
	@SuppressWarnings("unused")
	private JPanel createLabelComboBoxPair(String title, String[] supportedclauses) {
		return createLabelComboBoxPair(title, new JComboBox(supportedclauses));
	}

	private JPanel createPickTable() {
		JPanel pickTable = new JPanel();
		JLabel pickTableTitle = new JLabel("Choose a table");
		currentTableName = new JComboBox(ExplorerController.tableNames);
		currentTableName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	String table = (String) ((JComboBox)ae.getSource()).getSelectedItem();
            	//hacky way to do this
            	curAttribute.setModel(new JComboBox(controller.getLabels(table)).getModel());
            	currentConstraints.clear();
            	updateConstraints();
        		constraintsList.validate();
            }
        });
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
		
		constraintsList = new JList(getCurrentConstraints());
		constraintsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				delete.setEnabled(true);
				//edit.setEnabled(true);
			}
		});
		constraintsList.setMinimumSize(new Dimension(800,200));
		constraintsList.setMaximumSize(new Dimension(800,200));
		constraintsList.setPreferredSize(new Dimension(800,200));
		
		JPanel cudConstraints = createCUDPanel("constraint");
		JScrollPane scrollPane = new JScrollPane(constraintsList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		cPanel.add(title);
		cPanel.add(scrollPane);
		cPanel.add(cudConstraints);
		
		cPanel.setMinimumSize(new Dimension(800,260));
		cPanel.setMaximumSize(new Dimension(800,260));
		cPanel.setPreferredSize(new Dimension(800,260));
		
		//cPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		return cPanel;
	}

	private String[] getCurrentConstraints() {
		String[] constraints = new String[currentConstraints.size()];
		Collection<String> tmpConstraints = currentConstraints.values();
		constraints = tmpConstraints.toArray(constraints);
		return constraints;
	}
	
	private void updateConstraints() {
		constraintsList.setListData(getCurrentConstraints());
		if(currentConstraints.isEmpty()) {
    		//edit.setEnabled(false);
    		delete.setEnabled(false);
    		clear.setEnabled(false);
    	}
	}
	
	private JPanel createCUDPanel(String addDeleteWhat) {
		JPanel p = new JPanel();
		JButton add = new JButton("Add new " + addDeleteWhat);
		add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	selectionPanel.setVisible(true);
            	clear.setEnabled(true);
            }
        });
		p.add(add);
		//edit = new JButton("Edit " + addDeleteWhat);
		//edit.setEnabled(false);
		//p.add(edit);
		delete = new JButton("Remove " + addDeleteWhat);
		delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	String selectedValue = (String) constraintsList.getSelectedValue();
            	System.out.println(selectedValue);
            	System.out.println(currentConstraints.values());
            	currentConstraints.values().removeAll(Collections.singleton(selectedValue));
            	updateConstraints();
        		constraintsList.validate();
            }
        });
		delete.setEnabled(false);
		
		p.add(delete);
		clear = new JButton("Clear " + addDeleteWhat + "s");
		clear.setEnabled(false);
		clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	//hacky way to do this
            	currentConstraints.clear();
            	updateConstraints();
        		constraintsList.validate();
            }
        });
		p.add(clear);
		
		return p;
	}


	public void setResultsPanelRef(ResultsTab resultPanel) {
		this.resultsTab = resultPanel;
	}
}
