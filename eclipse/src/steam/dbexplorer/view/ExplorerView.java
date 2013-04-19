package steam.dbexplorer.view;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.UIManager;

import steam.dbexplorer.Main;
import steam.dbexplorer.controller.ExplorerController;

public class ExplorerView {
	/*
	 * GUI Elements
	 */
	private JFrame frame;
	private ExplorerController controller;
	
	/**
	 * The default width and height of the initial window
	 */
	private final static int defaultWidth = 1000;
	private final static int defaultHeight = 700;
	
	public ExplorerView(ExplorerController controller) {
		setLaF();
		this.controller = controller;
	}
	
	public void start() {
		frame = new JFrame(Main.programName);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(new Dimension(defaultWidth,defaultHeight));
		//JTable data = new JTable(controller.getData(null,null),
		//						 controller.getLabels(null));
		//frame.add()
		
		frame.setVisible(true);
	}
	
	private void setLaF() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
