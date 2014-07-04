package md;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class MagicDownloader extends JFrame implements Observer{
	private JTextField addTextField;
	private TableModel tableModel;
	private JTable table;
	private JButton pauseButton, resumeButton;
	private JButton cancelButton, clearButton;
	private Download selectedDownload;
	private boolean clearing;
	private final int FTP = 0;
	private final int HTTP = 1;
	private int protocolType = -1;
  
	public MagicDownloader(){
		setTitle("Magic Downloader");
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception exc) {
			System.out.println("Nie umiem ustalić L&F");
		}
		SwingUtilities.updateComponentTreeUI(this);
      
		setBounds(200, 50, 850, 600);
		setMinimumSize(new Dimension(700, 300));
	    addWindowListener(new WindowAdapter(){
	    	public void windowClosing(WindowEvent e){
	    		actionExit();
	    	}
	    }); 
	    JMenuBar menuBar = new JMenuBar();
	    JMenu fileMenu = new JMenu("Plik");
	    fileMenu.setMnemonic(KeyEvent.VK_P);
	    JMenuItem fileExitMenuItem = new JMenuItem("Zakończ", KeyEvent.VK_Z);
	    fileExitMenuItem.addActionListener((e)->actionExit()); //J8
	    fileMenu.add(fileExitMenuItem);
	    menuBar.add(fileMenu);
	    setJMenuBar(menuBar);
	    
	    JPanel addPanel = new JPanel();
	    addTextField = new JTextField(50);
	    addPanel.add(addTextField);
	    JButton addButton = new JButton("Rozpocznij pobieranie");
	    addButton.addActionListener((e)->actionAdd()); //J8
	    addPanel.add(addButton);

	    tableModel = new TableModel();
	    table = new JTable(tableModel);
	    table.getSelectionModel().addListSelectionListener((e)->tableSelectionChanged());
	    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  
	    ProgressRenderer renderer = new ProgressRenderer(0, 100);
	    renderer.setStringPainted(true); 
	    table.setDefaultRenderer(JProgressBar.class, renderer);

	    table.setRowHeight(
	      (int) renderer.getPreferredSize().getHeight());

	    JPanel downloadsPanel = new JPanel();
	    downloadsPanel.setBorder(
	      BorderFactory.createTitledBorder("Lista pobierania"));
	    downloadsPanel.setLayout(new BorderLayout());
	    downloadsPanel.add(new JScrollPane(table),
	      BorderLayout.CENTER);

	    JPanel buttonsPanel = new JPanel();
	    pauseButton = new JButton("Pauza");
	    pauseButton.addActionListener((e)->actionPause()); //J8
	    pauseButton.setEnabled(false);
	    resumeButton = new JButton("Wznów");
	    resumeButton.addActionListener((e)->actionResume()); //J8
	    resumeButton.setEnabled(false);
	    cancelButton = new JButton("Anuluj");
	    cancelButton.addActionListener((e)->actionCancel()); //J8
	    cancelButton.setEnabled(false);
	    clearButton = new JButton("Usuń");
	    clearButton.addActionListener((e)->actionClear()); //J8
	    clearButton.setEnabled(false);
	    
	    
	    buttonsPanel.add(pauseButton);
	    buttonsPanel.add(resumeButton);
	    buttonsPanel.add(cancelButton);
	    buttonsPanel.add(clearButton);

	    getContentPane().setLayout(new BorderLayout());
	    getContentPane().add(addPanel, BorderLayout.NORTH);
	    getContentPane().add(downloadsPanel, BorderLayout.CENTER);
	    getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
	    
	    setVisible(true);
	}

	private void actionExit(){
		System.exit(0);
	}

	private void actionAdd() {
		 URL verifiedUrl = verifyUrl(addTextField.getText());
		 if (verifiedUrl != null) {
		    Download d = null;
		    switch(protocolType){
		    case HTTP:
		    	d = new DownloadHttp(verifiedUrl);
		    	break;
		    case FTP:
		    	d = new DownloadFtp(verifiedUrl);
		    	break;
		    }
		    tableModel.addDownload(d);
		    addTextField.setText(""); 
		    } else {
		    	JOptionPane.showMessageDialog(this, "niepoprawny adres URL", "Błąd", JOptionPane.ERROR_MESSAGE);
		    }  
	}

	private URL verifyUrl(String url) {
		if (!url.toLowerCase().startsWith("ftp://") && !url.toLowerCase().startsWith("http://"))
		      return null;
		    if(url.startsWith("ftp://")) protocolType = FTP;
		    else protocolType = HTTP;
		    URL verifiedUrl = null;
		    
		    try {
		      verifiedUrl = new URL(url);
		    } catch (Exception e) {
		      return null;
		    }
		    if (verifiedUrl.getFile().length() < 2)
		      return null;
		    return verifiedUrl;
	}

 	private void tableSelectionChanged() {
 		if (selectedDownload != null)
 			selectedDownload.deleteObserver(MagicDownloader.this);
 		if (!clearing) {
 			selectedDownload = tableModel.getDownload(table.getSelectedRow());
 			selectedDownload.addObserver(MagicDownloader.this);
 			updateButtons();
 		}
 	}

 	private void actionPause() {
 		selectedDownload.pause();
 		updateButtons();
 	}

 	private void actionResume() {
 		selectedDownload.resume();
 		updateButtons();
 	}

 	private void actionCancel() {
 		selectedDownload.cancel();
 		updateButtons();
 	}

 	private void actionClear() {
 		clearing = true;
 		tableModel.clearDownload(table.getSelectedRow());
 		clearing = false;
 		selectedDownload = null;
 		updateButtons();
 	}

 	private void updateButtons() {
 		 if (selectedDownload != null) {
 			 int status = selectedDownload.status;
 		     switch (status) {
 		     case Download.DOWNLOADING:
 		    	 pauseButton.setEnabled(true);
 		         resumeButton.setEnabled(false);
 		         cancelButton.setEnabled(true);
 		         clearButton.setEnabled(false);
 		         break;
 		     case Download.PAUSED:
 		         pauseButton.setEnabled(false);
 		         resumeButton.setEnabled(true);
 		         cancelButton.setEnabled(true);
 		         clearButton.setEnabled(false);
 		         break;
 		     case Download.ERROR:
 		         pauseButton.setEnabled(false);
 		         resumeButton.setEnabled(true);
 		         cancelButton.setEnabled(false);
 		         clearButton.setEnabled(true);
 		         break;
 		     default: 
 		         pauseButton.setEnabled(false);
 		         resumeButton.setEnabled(false);
 		         cancelButton.setEnabled(false);
 		         clearButton.setEnabled(true);
 		     }
 		 } else {
 			 pauseButton.setEnabled(false);
 		     resumeButton.setEnabled(false);
 		     cancelButton.setEnabled(false);
 		     clearButton.setEnabled(false);
 		 }
 	}

 	public void update(Observable o, Object obj) {
 		if (selectedDownload != null && selectedDownload.equals(o))
 			updateButtons();
 	}

 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(()->new MagicDownloader());
 	}
}
