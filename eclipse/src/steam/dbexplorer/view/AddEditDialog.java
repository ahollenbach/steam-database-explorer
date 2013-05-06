package steam.dbexplorer.view;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import steam.dbexplorer.controller.ExplorerController;
import steam.dbexplorer.dbobject.DBReference;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Set;
 
class AddEditDialog extends JDialog {
    private JTextField textField;
    private String currentTable;
 
    private String createStr = "Create";
    private String cancelStr = "Cancel";
    private HashMap<String, JTextField> inputs = new HashMap<String, JTextField>();
    
    public AddEditDialog(JFrame parent, String tableName) {
    	super(parent, true);
    	JPanel main = new JPanel();
    	currentTable = tableName;
    	main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
    	main.setMinimumSize(new Dimension(400,400));
    	String[] attr = DBReference.displayNames.get(tableName);
    	for(int i=0;i<attr.length;i++) {
    		JPanel p = new JPanel();
    		JLabel label = new JLabel(attr[i]);
    		JTextField input = new JTextField(15);
    		p.add(label);
    		p.add(input);
    		p.setMaximumSize(new Dimension(400,60));
    		main.add(p);
    		inputs.put(attr[i], input);
    	}
        
    	
    	JPanel options = new JPanel();
    	JButton create = new JButton(createStr);
    	create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				String[] keys = new String[inputs.size()];
				String[] results = new String[inputs.size()];
				keys = inputs.keySet().toArray(keys);
				for(int i=0;i<keys.length;i++) {
					String attrName = keys[i];
					String val = inputs.get(attrName).getText();
					if(val.length() > 0) {
						val = null;
					} else if("string".equals(ExplorerController.getAttrType(attrName)) && val != null ) {
						val = "'" + val + "'";
					}
					results[i] = val;
				}
				
				ExplorerController.createEntry(currentTable, results);
				AddEditDialog.this.dispose();
			}
		});
    	JButton cancel = new JButton(cancelStr);
    	cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				AddEditDialog.this.dispose();
			}
		});
    	options.add(create);
    	options.add(cancel);
    	main.add(options);
    	this.add(main);
    	this.setVisible(true);
    }

}