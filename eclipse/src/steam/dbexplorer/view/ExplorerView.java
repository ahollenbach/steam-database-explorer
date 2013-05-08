package steam.dbexplorer.view;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.*;
import steam.dbexplorer.Main;
import steam.dbexplorer.controller.ExplorerController;

/**
 * The explorer view is used to convey the database information to the user.
 * It also supports the CRUD operations on all the data.
 * 
 * @author Andrew Hollenbach (anh7216@rit.edu)
 */
public class ExplorerView {
	
	/**
	 * A reference to the controller of the ExplorerView.
	 */
	private ExplorerController controller;
	
	/*
	 * GUI Elements
	 */
	private JFrame frame;
	private JTabbedPane tabs;
	
	/**
	 * The default width and height of the initial window
	 */
	private final static int defaultWidth = 1100;
	private final static int defaultHeight = 730;
	
	public ExplorerView(ExplorerController controller) {
		setLaF();
		this.controller = controller;
	}
	
	/**
	 * Launches the GUI for the Steam Database Explorer. Closing this
	 * window will terminate the program.
	 */
	public void start() {
		frame = new JFrame(Main.programName);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(defaultWidth,defaultHeight));
		
		buildTabs();
		frame.add(tabs);
		
		frame.setVisible(true);
	}
	
	/**
	 * Sets the look and feel to match the system. If it is unable
	 * to find the system's preferred look and feel, it uses the
	 * default Java look and feel.
	 */
	private void setLaF() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
	}
	
	/**
	 * Builds the "Build a Query" and "Results" tabs and
	 * adds them to the view.
	 */
	private void buildTabs() {
		tabs = new JTabbedPane();
		
		QueryTab queryPanel = new QueryTab(tabs,controller);
		tabs.addTab("Build a Query", null, queryPanel, "Build a query here");
		tabs.setMnemonicAt(0, KeyEvent.VK_1);
		
		ResultsTab resultPanel = new ResultsTab(tabs,controller);
		tabs.addTab("Results", null, resultPanel, "View results here");
		tabs.setMnemonicAt(1, KeyEvent.VK_2);
		
		resultPanel.setQueryPanelRef(queryPanel);
		queryPanel.setResultsPanelRef(resultPanel);
	}
}
