package talkboxModels;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TimerTask;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import talkboxModels.TalkboxDemoButton;

/**
 * Aids those with speech impediments to communicate using a sound generating
 * device called the TalkBox
 * 
 * This class configures the TalkBox, meaning allows the user to choose which
 * buttons and their associated images can create which sound
 * 
 * @author Zohair Ahmed
 */
public class TalkBoxConfigurator implements TalkBoxConfiguration {

	/*---------GLOBAL VARIABLES---------*/
	private static final long serialVersionUID = 1L;

	private JFrame frame; // main frame
	private JPanel innerPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // talkbox demo inner panel
	private JLabel status = new JLabel("Welcome to the TalkBox Configurator!"); // status messages
	private int width = 800; // width of main frame
	private int height = 800; // height of main frame

	private ArrayList<JButton> iconTabButtons = new ArrayList<JButton>(); // icon buttons
	private ArrayList<JButton> audioTabButtons = new ArrayList<JButton>(); // audio buttons
	private ArrayList<TalkboxDemoButton> demoButtons = new ArrayList<TalkboxDemoButton>(); // talkbox demo buttons

	// global so that when click, can mutate other panels
	JButton addB = new JButton("Add new button"); // add new set of icon and audio for the TalkBox app button
	JButton recordB = new JButton("Record my own Audio"); // record personal audio button
	JButton saveB = new JButton("Save"); // record personal audio button
	JButton clearB = new JButton("Clear"); // clear all buttons in the TalkBox app button

	private int numOfAudioSets; // number of audio sets
	private int totalButtons; // buttons in total
	private int demoInnerPanelCounter = 12; // a total of 12 TalkBoxDemoButtons can be created

	/*---------MAIN METHOD---------*/

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TalkBoxConfigurator window = new TalkBoxConfigurator();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/*---------CONSTRUCTORS---------*/

	/**
	 * Create the application.
	 * 
	 * The configurator that sets the buttons and associating audio for the sound
	 * generating device, called the TalkBox
	 */
	public TalkBoxConfigurator() {
		initialize();
	}

	/*---------USER INTERFACE---------*/

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		// MAIN FRAME
		this.frame = new JFrame("TalkBox Configurator");
		this.frame.setPreferredSize(new Dimension(this.width, this.height));
		this.frame.setLocationByPlatform(true);
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.frame.setLayout(new BorderLayout());
		this.frame.setMinimumSize(this.frame.getSize());

		// ICON AND AUDIO TABS
		JTabbedPane tabs = new JTabbedPane();
		tabs.setBackground(Color.BLUE);
		tabs.add("Icons", addIcons(this.iconTabButtons)); // icon panel
		tabs.add("Audio", addAudio(this.audioTabButtons)); // audio panel
		this.frame.getContentPane().add(BorderLayout.SOUTH, tabs);

		// BUTTON PANEL
		this.frame.getContentPane().add(BorderLayout.WEST, buttonPanel());

		// TALKBOX SIMULATOR PANEL
		this.frame.getContentPane().add(BorderLayout.CENTER, tbDemoPanel());

		// STATUS PANEL
		this.frame.getContentPane().add(BorderLayout.NORTH, statusPanel());

		this.frame.pack();
		this.frame.setVisible(true);
	}

	/**
	 * Adds the icon buttons to the JTabbedPane
	 * 
	 * @param buttons   - the ArrayList for the buttons created
	 * @param thisPanel - the JPanel where the buttons are to be displayed
	 * 
	 * @return - a JScrollPane of the desired icons as buttons
	 */
	private JScrollPane addIcons(ArrayList<JButton> buttons) {

		// icon panel
		JPanel iconPanel = new JPanel(new GridLayout(0, 8, 10, 10));
		iconPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		iconPanel.setBackground(Color.WHITE);

		// access icon file
		File iconFile = new File(".//icons/");

		// for every icon, create a button that takes shape of the respective icon
		JButton iconButton; // button
		BufferedImage buttonImg = null; // image of button
		String filename; // file of image of button
		for (File file : iconFile.listFiles()) {
			try {
				filename = file.getName();
				if (filename.endsWith(".png") && !filename.equals("Sound.png")) {
					buttonImg = ImageIO.read(new File(".//icons/" + file.getName()));
//					iconButton = new JButton(
//							new ImageIcon(buttonImg.getScaledInstance(50, 50, java.awt.Image.SCALE_SMOOTH)));
//					iconButton.setBorder(BorderFactory.createEmptyBorder());
//					buttons.add(iconButton);
//					iconPanel.add(iconButton);
					JLabel img = new JLabel(
							new ImageIcon(buttonImg.getScaledInstance(50, 50, java.awt.Image.SCALE_SMOOTH)));
					img.setName(file.getName());
					img.addMouseListener(new SelectIcon());
					iconPanel.add(img);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// create JScrollPane of the panel icons are being added to
		JScrollPane icons = new JScrollPane(iconPanel);
		icons.setPreferredSize(new Dimension(this.width, 150));

		return icons;
	}

	/**
	 * Adds the audio buttons to the JTabbedPane
	 * 
	 * @param buttons   - the ArrayList holding each audio button
	 * @param thisPanel - panel audio buttons are added to
	 * 
	 * @return - a JScrollPane of the desired audio as buttons
	 */
	private JScrollPane addAudio(ArrayList<JButton> buttons) {

		// audio Panel
		JPanel audioPanel = new JPanel(new GridLayout(0, 1, 10, 10));
		audioPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// access sound file
		File soundFile = new File(".//sounds");

		// for every sub-directory in the sounds file,
		// create a sub-panel for the sub-directory
		// inside each sub-panel, put respective buttons
		// for the audio files
		JButton audioButton;
		for (File subDir : soundFile.listFiles()) {
			if (subDir.isDirectory()) {
				JPanel subPanel = new JPanel(new GridLayout(0, 4));
				subPanel.setBorder(BorderFactory.createTitledBorder(subDir.getName()));
				subPanel.setName(subDir.getName());
				audioPanel.add(subPanel);

				for (File file : subDir.listFiles()) {
					if (file.getName().endsWith(".wav")) {
						audioButton = new JButton(file.getName());
						audioButton.setPreferredSize(new Dimension(0, 25));
						audioButton.addMouseListener(new PlayAudio());
						this.audioTabButtons.add(audioButton);
						subPanel.add(audioButton);
					}
				}
			}
		}

		// create JScrollPane of the panel the audio buttons are being added to
		JScrollPane audios = new JScrollPane(audioPanel);
		audios.setPreferredSize(new Dimension(this.width, 150));

		return audios;
	}

	/**
	 * Returns a JPanel holding mutator buttons such as add, remove, record
	 * 
	 * @return - a JPanel holding mutator buttons such as add, remove, record
	 */
	private JPanel buttonPanel() {

		// main panel
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

		// add the add, clear and record buttons
		buttonPanel.add(this.addB);
		this.addB.addActionListener(new AddDemoButton());
		buttonPanel.add(this.recordB);
		
		buttonPanel.add(this.saveB);
		
		buttonPanel.add(this.clearB);
		this.clearB.addActionListener(new ClearDemoButton());

		return buttonPanel;
	}

	/**
	 * The message panel
	 * 
	 * @return - a JPanel informing the users of the status of the Talkbox
	 *         Configurator
	 */
	private JPanel statusPanel() {

		// main panel
		JPanel statusPanel = new JPanel(new CardLayout());
		statusPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10),
				BorderFactory.createTitledBorder("Log")));
		statusPanel.setPreferredSize(new Dimension(this.width - 20, 60));

		// message label
		status.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		statusPanel.add(status);

		return statusPanel;
	}

	/**
	 * Returns the TalkBox demo panel, which is where the modifications for the main
	 * simulator take place
	 * 
	 * @return - the TalkBox demo panel
	 */
	private JPanel tbDemoPanel() {

		// outer panel added to help with padding
		JPanel containerPanel = new JPanel(new CardLayout());
		containerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

		// main inner panel
		this.innerPanel.setBackground(Color.WHITE);
		this.innerPanel.setBorder(BorderFactory.createTitledBorder("TalkBox Demo"));
		containerPanel.add(this.innerPanel);

		return containerPanel;
	}

	/*---------BUTTON FUNCTIONALITY---------*/

	/**
	 * Functionality for the Add button on the left button panel. Adds a
	 * TalkboxDemoButton to the inner JPanel
	 */
	private class AddDemoButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (demoInnerPanelCounter != 0) {
				TalkboxDemoButton tdb = new TalkboxDemoButton(innerPanel);
				demoButtons.add(tdb);
				demoInnerPanelCounter--;
				status.setText("Added button. " + demoInnerPanelCounter + " buttons remaining.");
			} else {
				status.setText("Reached maximum button capacity.");
				((JButton) arg0.getSource()).setEnabled(false);
			}
		}
	}

	/**
	 * Functionality for the Add button on the left button panel. Clears the Talkbox
	 * Demo Panel
	 */
	private class ClearDemoButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			int confirmClear = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear?", "Clear Message",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			if (confirmClear == JOptionPane.YES_OPTION) {
				innerPanel.removeAll();
				demoButtons.clear();
				demoInnerPanelCounter = 12;
				status.setText("TalkBox Demo cleared");
				innerPanel.updateUI();
				addB.setEnabled(true);
			}

		}

	}

	/**
	 * Play audio when the respective buttons in the audio tabs is clicked If single
	 * click, play audio; if double clicked, add audio
	 */
	private class PlayAudio extends MouseAdapter {

		private int clickCount = 0;
		java.util.Timer timer = new java.util.Timer("doubleClickTimer", false);
		private AudioInputStream audioIn; // audio file
		private Clip audio; // audio clip

		@Override
		public void mouseClicked(MouseEvent e) {

			this.clickCount = e.getClickCount();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {

					String buttonName = (((JButton) e.getSource()).getText());

					if (clickCount == 1) {

						String panelName = (((JButton) e.getSource()).getParent().getName());

						try {
							audioIn = AudioSystem
									.getAudioInputStream(new File(".//sounds/" + panelName + "/" + buttonName));
							audio = AudioSystem.getClip();
							audio.open(audioIn);
							status.setText(buttonName + " played");
							audio.start();
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (clickCount > 1) {
						status.setText(buttonName + " added");
					}
					clickCount = 0;
				}
			}, 300);
		}
	}

	private class SelectIcon extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			JLabel icon = (JLabel) e.getSource();
			TransferHandler handler = icon.getTransferHandler();
			status.setText(icon.getName());

			for (int i = 0; i < demoButtons.size(); i++) {

				JButton thisButton = demoButtons.get(i).getIconButton();

				if (thisButton.isSelected()) {
					thisButton.setIcon(icon.getIcon());
					thisButton.setText("");
				}
			}
		}

	}

	/*--------- ACCESSORS ---------*/

	@Override
	public int getNumberOfAudioButtons() {
		return this.audioTabButtons.size();
	}

	@Override
	public int getNumberOfAudioSets() {
		return this.numOfAudioSets;
	}

	@Override
	public int getTotalNumberOfButtons() {
		return this.totalButtons;
	}

	@Override
	public Path getRelativePathToAudioFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[][] getAudioFileNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
