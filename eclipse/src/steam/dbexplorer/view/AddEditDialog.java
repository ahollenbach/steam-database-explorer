package steam.dbexplorer.view;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import steam.dbexplorer.dbobject.DBReference;

import java.awt.*;
import java.awt.event.*;
 
class AddEditDialog extends JDialog {
    private JTextField textField;
 
    private String createStr = "Create";
    private String cancelStr = "Cancel";
    private JOptionPane optionPane;
    
    
    public AddEditDialog(JFrame parent, String tableName) {
    	super(parent, true);
    	this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    	String[] attr = DBReference.displayNames.get(tableName);
    	for(int i=0;i<attr.length;i++) {
    		JPanel p = new JPanel();
    		JLabel label = new JLabel(attr[i]);
    		JTextField input = new JTextField();
    		p.add(label);
    		p.add(input);
    		p.setMaximumSize(new Dimension(400,60));
    		this.add(p);
    	}
    	Object[] options = {createStr, cancelStr};
		 
        //Create the JOptionPane.
        optionPane = new JOptionPane("Create a new " + tableName.toLowerCase(),
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);
       
        setContentPane(optionPane);
    	/*
    	JPanel options = new JPanel();
    	JButton create = new JButton(createStr);
    	create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
			}
		});
    	JButton cancel = new JButton(createStr);
    	cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				ae.getSource()
			}
		});
    	options.add(create);
    	options.add(cancel);*/
    }

}