import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI{
	
	private File inFile = null;
	private int maxMatch = 0;
	private int windowSize = 0;
	
	public GUI()
	{
		JFrame frame = new JFrame();
		
		JButton fileChoosingButton = new JButton("Select File");
		
		JPanel panel = new JPanel();
		
		panel.setBorder(BorderFactory.createEmptyBorder(30,30,10,30));
		panel.setLayout(new GridLayout(0,1));
		panel.add(fileChoosingButton);
		
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("LZSS Compactor");
		frame.pack();
		frame.setVisible(true);
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		
		int result = fileChooser.showOpenDialog(frame);
	}

}

