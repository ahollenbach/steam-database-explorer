package steam.dbexplorer.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import steam.dbexplorer.Main;
import steam.dbexplorer.controller.ExplorerController;

public class ExplorerView {
	
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
	
	public void start() {
		frame = new JFrame(Main.programName);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(new Dimension(defaultWidth,defaultHeight));
		
		buildTabs();
		frame.add(tabs);
		
		frame.setVisible(true);
	}
	
	private void setLaF() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
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
	
	@SuppressWarnings("unused")
	protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

}
