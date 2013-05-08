/**
 * QueryTab is a tab that goes into ExplorerView. It is used
 * to select the table to view and constraints to apply to that 
 * table.
 * 
 *  @author Andrew Hollenbach <anh7216@rit.edu>
 *  @author Andrew DeVoe <ard5852@rit.edu>
 */
package steam.dbexplorer.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import steam.dbexplorer.Utils;
import steam.dbexplorer.controller.ExplorerController;

@SuppressWarnings("serial")
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
	private JPanel filterBuilder;
	private ExplorerController controller;
	private ResultsTab resultsTab;
	private JTabbedPane parent;
	private JList constraintsList;
	private JButton delete;
	private JButton clear;
	//private JButton edit;
	
	/**
	 * The hashtable stores the current constraints. It stores the
	 * query string as the key and the human readable string as the value.
	 */
	private Hashtable<String,String> currentConstraints = new Hashtable<String,String>();
	
	/**
	 * Creates a new "Build a Query" tab.
	 * 
	 * @param parentPane The parent tab pane
	 * @param controller The controller for the view
	 */
	public QueryTab(JTabbedPane parentPane, ExplorerController controller) {
		super();
		this.controller = controller;
		this.parent = parentPane;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		//create the table select dropdown
		JPanel tableSelectPanel = createTableSelectPanel();
		
		//create the list of active constraints
		JPanel constraintsPanel = createConstraintsListPanel();
		
		//create the create/edit filter form
		filterBuilder = createFilterBuilder();
		
		//run button
		JButton runQuery = new JButton ("Run");
		runQuery.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	String table = (String) currentTableName.getSelectedItem();
            	resultsTab.updateTable(table,currentConstraints.keys());
            	parent.setSelectedIndex(1); //switch to results tab
            }
        });	
		
		this.add(tableSelectPanel);
		this.add(constraintsPanel);
		this.add(filterBuilder);
		this.add(new JPanel()); //spacer
		this.add(runQuery);
	}

	/**
	 * Creates a panel for adding a new filter.
	 * 
	 * @return a panel that contains the add new filter selection elements
	 */
	private JPanel createFilterBuilder() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		JPanel attr = createLabelComboBoxPair("Attribute: ",
				curAttribute = new JComboBox(controller.getLabels((String) currentTableName.getSelectedItem())));
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
            	//get common information
            	String type = (String) curType.getSelectedItem();
            	String attr = (String) curAttribute.getSelectedItem();
            	
            	if(type.equals("Where")) {
	            	String op   = (String) curOperation.getSelectedItem();
	            	String queryOp = ExplorerController.opEquivs[curOperation.getSelectedIndex()];
	            	String val  = curVal.getText();
	            	
	            	if(val.length() == 0) { //didn't fill everything out!
	            		PopupFactory.errorPopup("Please fill out all values.", "Value Missing!");
	            		return;
	            	} 
	            	if("string".equals(ExplorerController.getAttrType(attr))){
	            		val = Utils.surroundWithQuotes(val);
	            	}
	            	String queryString = " " + ExplorerController.convertToDbAttr(attr) + queryOp + val;
	            	String displayString = type + " " + attr + " is " + op + " " + val;
	            	currentConstraints.put(queryString, displayString);
            	} else { //sort by
            		String order   = (String) curOrder.getSelectedItem();
            		String qOrder = order.equals("Ascending") ? "asc" : "desc";
            		
	            	String queryString = "sort=" + ExplorerController.convertToDbAttr(attr) + " " + qOrder;
	            	String displayString = "Sort by " + attr + " in " + order + " order";
	            	
	            	//check if you are already sorting on this attribute.
	            	Enumeration<String> keys = currentConstraints.keys();
	            	while(keys.hasMoreElements()) {
	            		String key = keys.nextElement();
	            		if(key.contains("sort=" + ExplorerController.convertToDbAttr(attr))) {
	            			PopupFactory.errorPopup("You are already sorting on this attribute!",
		            			    				"Unable to add sort!");
	            			return;
	            		}
	            	}
	            	currentConstraints.put(queryString, displayString);
            	}
            	updateConstraints();
            	filterBuilder.setVisible(false);
            	
        		//edit.setEnabled(false);
        		delete.setEnabled(false);
            }
        });	
		actions.add(commitButton);
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	filterBuilder.setVisible(false);
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

	/**
	 * An abstracted method that creates a label from the title and pairs
	 * it in a JPanel with a supplied combobox.
	 * 
	 * @param title The label of the pair
	 * @param cb a combobox 
	 * @return the JPanel containing the label on the left and the combobox 
	 * on the right.
	 */
	private JPanel createLabelComboBoxPair(String title, JComboBox cb) {
		JPanel p = new JPanel();
		JLabel label = new JLabel(title);
		
		p.add(label);
		p.add(cb);
		
		p.setMaximumSize(new Dimension(800,60));
		return p;
	}

	/**
	 * Creates the table select pane, which prompts
	 * the user for a table to select and provides a 
	 * dropdown of the available tables to choose from.
	 * 
	 * @return The table select pane
	 */
	private JPanel createTableSelectPanel() {
		JPanel tableSelectPanel = new JPanel();
		JLabel selectTableTitle = new JLabel("Choose a table");
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
		tableSelectPanel.add(selectTableTitle); 
		tableSelectPanel.add(currentTableName);
		tableSelectPanel.setMaximumSize(new Dimension(800,200));
		return tableSelectPanel;
	}

	/**
	 * Creates the table of active constraints and the label,
	 * and wraps it up in a JPanel.
	 * 
	 * @return the table of active constraints
	 */
	private JPanel createConstraintsListPanel() {
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

	/**
	 * Gets a list of the current constraints in HUMAN-READABLE
	 * format.
	 * 
	 * @return An array of human-readable constraints
	 */
	private String[] getCurrentConstraints() {
		String[] constraints = new String[currentConstraints.size()];
		Collection<String> tmpConstraints = currentConstraints.values();
		constraints = tmpConstraints.toArray(constraints);
		return constraints;
	}
	
	/**
	 * Updates the list of constraints by resetting list data
	 * to what results from getCurrentConstraints. If no constraints
	 * remain, the delete and clear buttons are disabled.
	 */
	private void updateConstraints() {
		constraintsList.setListData(getCurrentConstraints());
		if(currentConstraints.isEmpty()) {
    		//edit.setEnabled(false);
    		delete.setEnabled(false);
    		clear.setEnabled(false);
    	} else {
    		clear.setEnabled(true);
    	}
	}
	
	/**
	 * Creates the control buttons - the add,delete,and clear
	 * buttons.
	 * 
	 * @param addDeleteWhat The thing you are deleting. Should just be
	 * "constraint"
	 * @return A JPanel containing all the controls needed.
	 */
	private JPanel createCUDPanel(String addDeleteWhat) {
		JPanel p = new JPanel();
		JButton add = new JButton("Add new " + addDeleteWhat);
		add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	filterBuilder.setVisible(true);
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

	/**
	 * Sets a reference to the results tab
	 * 
	 * @param resultPanel The results tab to reference.
	 */
	public void setResultsPanelRef(ResultsTab resultPanel) {
		this.resultsTab = resultPanel;
	}
}
