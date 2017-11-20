package org.jlab.clas12.DataExplorer;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;

import org.jlab.latif.clas12lib.bankdef.*;
import org.jlab.latif.clas12lib.core.*;

/**
 * 
 * @author Latif Kabir < jlab.org/~latif >
 *
 */

public class DataExplorer
{
	private JFrame mainFrame;
	private JLabel headerLabel;
	private JLabel blankLabel;
	private JPanel controlPanel;
	private JPanel detControlPanel;
	private JPanel xcontrolPanel;
	private JPanel ycontrolPanel;
	private JPanel pathPanel;
	private JPanel runPanel;
	private JPanel eventPanel;
	private JPanel plotPanel;
	private JPanel cutPanel;
	private JPanel cutExpPanel;

	private JPanel container;

    JFileChooser file_chooser = new JFileChooser();
    File[] fileList = null;
    ArrayList<String> fileArray = new ArrayList<String>();
	
	private JTextArea textArea;
	private PrintStream standardOut;	
	private Thread explorerThread;
	private boolean threadIsAlive = false;

	ClasRun run = null;
	String dataDir = Constants.DATA_DIR;
	String runNumberStr;
	String clasDetectorName = null;
	Bank bankX;
	Bank bankY;
	Bank cut1Bank;
	String bankXName;
	String bankYName;
	String cut1Name = "N/A";
	String cut2Name = "N/A";
	String cut3Name = "N/A";
	String cut4Name = "N/A";
	String cutExpStr = "N/A";
	DetectorDef clasDetector;

	int event = 0;

	String xStr;
	int xnBinsInt;
	float xMinFloat;
	float xMaxFloat;

	String yStr;
	int ynBinsInt;
	float yMaxFloat;

	boolean xReady = false;
	float yMinFloat;
	boolean yReady = false;
	boolean bankReady = false;
	boolean cutExpReady = false;
	boolean selectionIsMade = false;

	// ------ Constructor ------------------
	public DataExplorer()
	{
		prepareGUI();
	}

	// ------------- Initialize required objects for GUI-------------------------------
	public void prepareGUI()
	{
		// -------------------- The main frame -------------------------------
		mainFrame = new JFrame("CLAS Data Explorer");		
		mainFrame.setLayout(new BorderLayout());
									
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		Dimension frameSize = new Dimension((int) (width / 1.7), (int) (height / 1.10));		
		int x = (int) (frameSize.width / 2);
		int y = (int) (frameSize.height / 2);
		mainFrame.setBounds(x, y, frameSize.width, frameSize.height);
		mainFrame.pack();
		mainFrame.setSize(frameSize.width, frameSize.height);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
									
		//mainFrame.setSize(800, 700);
		//mainFrame.setLayout(new GridLayout(0, 1));

		mainFrame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent windowEvent)
			{
				System.exit(0);
			}
		});

		// ----------------- Header Label properties ------------------------------
		blankLabel = new JLabel("                        ", JLabel.CENTER);
		headerLabel = new JLabel("", JLabel.CENTER);
		
		// ---------------- The Panel (drop/down menu)-----------------------
		pathPanel = new JPanel();
		pathPanel.setLayout(new FlowLayout());

		detControlPanel = new JPanel();
		detControlPanel.setLayout(new FlowLayout());

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());

		xcontrolPanel = new JPanel();
		xcontrolPanel.setLayout(new FlowLayout());

		ycontrolPanel = new JPanel();
		ycontrolPanel.setLayout(new FlowLayout());

		plotPanel = new JPanel();
		plotPanel.setLayout(new FlowLayout());

		runPanel = new JPanel();
		runPanel.setLayout(new FlowLayout());

		eventPanel = new JPanel();
		eventPanel.setLayout(new FlowLayout());

		cutPanel = new JPanel();
		cutPanel.setLayout(new FlowLayout());
		
		cutExpPanel = new JPanel();
		cutExpPanel.setLayout(new FlowLayout());
		
		container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	}

	//---------------------- Title area ----------------------------
	public void titleArea()
	{
		headerLabel.setText("             CLAS Data Explorer             ");
        Font fancyFont = new Font("Serif", Font.BOLD | Font.ITALIC, 26);
        headerLabel.setFont(fancyFont);
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
		headerLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
               
        //Icon clasIcon = new ImageIcon("res/clas.jpg");
        //headerLabel.setIcon(clasIcon);
        
		Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
		headerLabel.setBorder(border);
	}
	
	// --------- Title and Run selection area --------------------------
	public void runArea()
	{				
		JButton enterButton = new JButton(" ... Choose files");
		
		enterButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{				
				System.out.println("Select the files .......");

				file_chooser.setMultiSelectionEnabled(true);
				int result= file_chooser.showOpenDialog(null);
				
				if (result == JFileChooser.APPROVE_OPTION) 
				{
					
					fileList = file_chooser.getSelectedFiles();
					//fileArray = new ArrayList<String>();
					fileArray.clear();
					for (File file : fileList)
					{
						System.out.println("You have selected: " + file);
						fileArray.add(file.toString());
					}										
					runNumberStr = fileArray.get(0);
				}
				else if (result == JFileChooser.CANCEL_OPTION) 
				{
				    System.out.println("Cancel was selected");
				    return;
				}
								
				if(threadIsAlive)
				{
					System.out.println("\n !!!! You must wait until current request is finished !!!\n");
					return;
				}
				threadIsAlive = true;
				explorerThread = new Thread(new Runnable()
				{
					public void run()
					{
						run = new ClasRun(fileArray);
						run.setFilePath(fileArray.get(0));
						if (run.runExist())
						{
							System.out.println("\n Successfully Loaded files.");
							//System.out.println("\n Total number of entries " + run.getEntries());
						}
						else
							System.out.println("\n The requested run files NOT found ");
						threadIsAlive = false;
					}
				});
				explorerThread.start();
			}
		});
		runPanel.add(enterButton);
		mainFrame.setVisible(true);
	}

	// ------------- Event Area -----------------------
	public void eventArea()
	{
		JLabel eventLabel = new JLabel("Event #: ", JLabel.RIGHT);
		final JTextField eventText = new JTextField(6);

		JButton eventButton = new JButton("Get Info");
		eventButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String data = "Event: " + eventText.getText().replaceAll("\\s", "");
				try
				{
					int num1 = Integer.parseInt(eventText.getText().replaceAll("\\s", ""));
				} catch (Exception e2)
				{
					System.out.println("\n Incomplete Field");
					return;
				}
				event = Integer.parseInt(eventText.getText().replaceAll("\\s", ""));
				if (run == null)
				{
					System.out.println("\n Invalid run number");
					return;
				}
				if(threadIsAlive)
				{
					System.out.println("\n !!!! You must wait until current request is finished !!!\n");
					return;
				}
				threadIsAlive = true;
				explorerThread = new Thread(new Runnable()
				{
					public void run()
					{
						if (run.runExist())
							run.getEventInfo(event);
						else
							System.out.println("\n The requested run NOT found ");
						threadIsAlive = false;
					}
				});
				explorerThread.start();
			}
		});
		eventPanel.add(eventLabel);
		eventPanel.add(eventText);
		eventPanel.add(eventButton);

		mainFrame.setVisible(true);
	}

	// ------------- Detector selection area ----------------------------------
	public void detectorSelArea()
	{
		JLabel detLabel = new JLabel("Detector: ", JLabel.RIGHT);
		final DefaultComboBoxModel detName = new DefaultComboBoxModel();

		detName.addElement("CND");
		detName.addElement("CVT");
		detName.addElement("DATA");
		detName.addElement("DC");

		detName.addElement("DETECTOR");
		detName.addElement("ECAL");
		detName.addElement("EVENT");
		detName.addElement("FT");
		detName.addElement("HEADER");

		detName.addElement("HTCC");
		detName.addElement("LTCC");
		detName.addElement("MC");
		detName.addElement("SVT");
		detName.addElement("TOF");

		final JComboBox detCombo = new JComboBox(detName);
		detCombo.setSelectedIndex(0);

		JScrollPane detScrollPane = new JScrollPane(detCombo);
		JButton showDetButton = new JButton("Enter");

		showDetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (detCombo.getSelectedIndex() != -1)
				{
					clasDetectorName = (String) detCombo.getItemAt(detCombo.getSelectedIndex());
					if (run == null)
					{
						System.out.println("\n Please enter a run number");
						return;
					}
					if (threadIsAlive)
					{
						System.out.println("\n !!!! You must wait until current request is finished !!!\n");
						return;
					}
					threadIsAlive = true;
					explorerThread = new Thread(new Runnable()
					{
						public void run()
						{

							if (run.runExist())
							{
								clasDetector = new DetectorDef(clasDetectorName);
								System.out.println("\n Selected detector system: " + clasDetectorName);
							}
							else
							{
								System.out.println("\n The requested run NOT found ");
								return;
							}
							bankSelArea();
							xSelArea(null);
							ySelArea(null);
							xReady = false;
							yReady = false;
							bankReady = true;
							cutExpReady = true;
							threadIsAlive = false;
						}
					});
					explorerThread.start();
				}
			}
		});
		detControlPanel.add(detLabel);
		detControlPanel.add(detScrollPane);
		detControlPanel.add(showDetButton);
		mainFrame.setVisible(true);
	}

	// ------------- Bank selection area ----------------------------------
	public void bankSelArea()
	{
		controlPanel.removeAll();
		JLabel bankLabel = new JLabel("Bank: ", JLabel.RIGHT);
		final DefaultComboBoxModel branchName = new DefaultComboBoxModel();

		if (clasDetectorName == null)
			branchName.addElement("Not Selected");
		else
		{
			for (int i = 0; i < clasDetector.getBankList().length; i++)
				branchName.addElement(clasDetector.getBankList()[i].getBank());
		}
		final JComboBox bankXCombo = new JComboBox(branchName);
		bankXCombo.setSelectedIndex(0);

		JScrollPane scrollPane = new JScrollPane(bankXCombo);
		JButton showButton = new JButton("Enter");

		showButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String data = "";
				if (bankXCombo.getSelectedIndex() != -1)
				{
					if (clasDetectorName == null || !bankReady)
					{
						System.out.println("\n Complete all selections first.");
						return;
					}
					if (threadIsAlive)
					{
						System.out.println("\n !!!! You must wait until current request is finished !!!\n");
						return;
					}
					threadIsAlive = true;
					explorerThread = new Thread(new Runnable()
					{
						public void run()
						{

							bankXName = (String) bankXCombo.getItemAt(bankXCombo.getSelectedIndex());
							bankX = clasDetector.getBank(bankXName);
							//data = "Bank Selected: " + bankXName;
							if (run == null)
							{
								System.out.println("\n Please enter a run number");
								return;
							}
							if (run.runExist())
								run.getBankInfo(bankXName, event);
							else
							{
								System.out.println("\n The requested run NOT found ");
								return;
							}

							xSelArea(bankX);
							ySelArea(bankX);
							cutArea(bankX);
							bankReady = true;
							threadIsAlive = false;
						}
					});
					explorerThread.start();

				}
			}
		});
		controlPanel.add(bankLabel);
		controlPanel.add(scrollPane);
		controlPanel.add(showButton);
		controlPanel.updateUI();
		mainFrame.setVisible(true);
	}

	// ------------- X and Y axis variable selection area
	// ----------------------------------
	public void xSelArea(Bank bSelected)
	{
		xcontrolPanel.removeAll();
		xReady = false;

		JLabel xLabel = new JLabel("X", JLabel.RIGHT);
		final DefaultComboBoxModel xName = new DefaultComboBoxModel();

		if (bSelected == null)
			xName.addElement("N/A");
		else
		{
			for (int i = 0; i < bSelected.getItems().size(); i++)
				xName.addElement(bSelected.getItems().get(i).getName());
		}

		final JComboBox xCombo = new JComboBox(xName);
		xCombo.setSelectedIndex(0);

		JScrollPane scrollPane = new JScrollPane(xCombo);
		JButton showButton = new JButton("Set");

		JLabel nBins = new JLabel("nBins: ", JLabel.RIGHT);
		JLabel Max = new JLabel("xMax : ", JLabel.CENTER);
		JLabel Min = new JLabel("xMin : ", JLabel.CENTER);
		final JTextField nBinsText = new JTextField(6);
		final JTextField MaxText = new JTextField(6);
		final JTextField MinText = new JTextField(6);

		xcontrolPanel.add(xLabel);
		xcontrolPanel.add(scrollPane);
		xcontrolPanel.add(nBins);
		xcontrolPanel.add(nBinsText);
		xcontrolPanel.add(Min);
		xcontrolPanel.add(MinText);
		xcontrolPanel.add(Max);
		xcontrolPanel.add(MaxText);
		xcontrolPanel.add(showButton);
		xcontrolPanel.updateUI();
		mainFrame.setVisible(true);

		showButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (xCombo.getSelectedIndex() != -1)
				{
					xStr = (String) xCombo.getItemAt(xCombo.getSelectedIndex());
					if (xStr == "N/A")
					{
						System.out.println("\n This option NOT available right now");
						return;
					}

					try
					{
						int num1 = Integer.parseInt(nBinsText.getText().replaceAll("\\s", ""));
						float num2 = Float.parseFloat(MinText.getText().replaceAll("\\s", ""));
						float num3 = Float.parseFloat(MaxText.getText().replaceAll("\\s", ""));
					} catch (Exception e2)
					{
						System.out.println("\n Incomplete Field");
						return;
					}
					xnBinsInt = Integer.parseInt(nBinsText.getText().replaceAll("\\s", ""));
					xMinFloat = Float.parseFloat(MinText.getText().replaceAll("\\s", ""));
					xMaxFloat = Float.parseFloat(MaxText.getText().replaceAll("\\s", ""));

					System.out.println("\n For " + xStr + " set");
					System.out.println(" nBins: " + xnBinsInt);
					System.out.println(" xMin: " + xMinFloat);
					System.out.println(" xMax: " + xMaxFloat);
					if (xnBinsInt < 0)
					{
						System.out.println("\nThe number of bins should NOT be negative");
						return;
					}
					xReady = true;
				}
			}
		});

	}

	public void ySelArea(Bank bSelected)
	{
		ycontrolPanel.removeAll();
		yReady = false;

		JLabel yLabel = new JLabel("Y", JLabel.RIGHT);
		final DefaultComboBoxModel yName = new DefaultComboBoxModel();

		if (bSelected == null)
			yName.addElement("N/A");
		else
		{
			for (int i = 0; i < bSelected.getItems().size(); i++)
				yName.addElement(bSelected.getItems().get(i).getName());
		}

		final JComboBox yCombo = new JComboBox(yName);
		yCombo.setSelectedIndex(0);

		JScrollPane scrollPane = new JScrollPane(yCombo);
		JButton showButton = new JButton("Set");

		JLabel nBins = new JLabel("nBins: ", JLabel.RIGHT);
		JLabel Max = new JLabel("yMax : ", JLabel.CENTER);
		JLabel Min = new JLabel("yMin : ", JLabel.CENTER);
		final JTextField nBinsText = new JTextField(6);
		final JTextField MaxText = new JTextField(6);
		final JTextField MinText = new JTextField(6);

		ycontrolPanel.add(yLabel);
		ycontrolPanel.add(scrollPane);
		ycontrolPanel.add(nBins);
		ycontrolPanel.add(nBinsText);
		ycontrolPanel.add(Min);
		ycontrolPanel.add(MinText);
		ycontrolPanel.add(Max);
		ycontrolPanel.add(MaxText);
		ycontrolPanel.add(showButton);
		ycontrolPanel.updateUI();
		mainFrame.setVisible(true);

		showButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (yCombo.getSelectedIndex() != -1)
				{
					yStr = (String) yCombo.getItemAt(yCombo.getSelectedIndex());
					if (yStr == "N/A")
					{
						System.out.println("\n This option NOT available right now");
						return;
					}

					try
					{
						int num1 = Integer.parseInt(nBinsText.getText());
						float num2 = Float.parseFloat(MinText.getText());
						float num3 = Float.parseFloat(MaxText.getText());
					} catch (Exception e2)
					{
						System.out.println("\n Incomplete Field");
						return;
					}
					ynBinsInt = Integer.parseInt(nBinsText.getText().replaceAll("\\s", ""));
					yMinFloat = Float.parseFloat(MinText.getText().replaceAll("\\s", ""));
					yMaxFloat = Float.parseFloat(MaxText.getText().replaceAll("\\s", ""));

					System.out.println("\n For " + yStr + " set");
					System.out.println(" nBins: " + ynBinsInt);
					System.out.println(" yMin: " + yMinFloat);
					System.out.println(" yMax: " + yMaxFloat);
					if (ynBinsInt < 0)
					{
						System.out.println("\nThe number of bins should NOT be negative");
						return;
					}
					yReady = true;
				}
			}
		});

	}

	public void cutArea(Bank selBank)
	{
		cutPanel.removeAll();

		// -------------- Cut1 selection box -----------------------
		JLabel cut1Label = new JLabel("Sel:: a", JLabel.RIGHT);
		final DefaultComboBoxModel cut1Box = new DefaultComboBoxModel();

		if (selBank == null)
			cut1Box.addElement("N/A");
		else
		{
			cut1Box.addElement("N/A");
			for (int i = 0; i < selBank.getItems().size(); i++)
				cut1Box.addElement(selBank.getItems().get(i).getName());
		}

		final JComboBox cut1Combo = new JComboBox(cut1Box);
		cut1Combo.setSelectedIndex(0);

		JScrollPane cut1ScrollPane = new JScrollPane(cut1Combo);

		// -------------- Cut2 selection box -----------------------
		JLabel cut2Label = new JLabel("b", JLabel.RIGHT);
		final DefaultComboBoxModel cut2Box = new DefaultComboBoxModel();

		if (selBank == null)
			cut2Box.addElement("N/A");
		else
		{
			cut2Box.addElement("N/A");
			for (int i = 0; i < selBank.getItems().size(); i++)
				cut2Box.addElement(selBank.getItems().get(i).getName());
		}

		final JComboBox cut2Combo = new JComboBox(cut2Box);
		cut1Combo.setSelectedIndex(0);

		JScrollPane cut2ScrollPane = new JScrollPane(cut2Combo);

		// -------------- Cut3 selection box -----------------------
		JLabel cut3Label = new JLabel("c", JLabel.RIGHT);
		final DefaultComboBoxModel cut3Box = new DefaultComboBoxModel();

		if (selBank == null)
			cut3Box.addElement("N/A");
		else
		{
			cut3Box.addElement("N/A");
			for (int i = 0; i < selBank.getItems().size(); i++)
				cut3Box.addElement(selBank.getItems().get(i).getName());
		}

		final JComboBox cut3Combo = new JComboBox(cut3Box);
		cut1Combo.setSelectedIndex(0);

		JScrollPane cut3ScrollPane = new JScrollPane(cut3Combo);

		// -------------- Cut4 selection box -----------------------
		JLabel cut4Label = new JLabel("d", JLabel.RIGHT);
		final DefaultComboBoxModel cut4Box = new DefaultComboBoxModel();

		if (selBank == null)
			cut4Box.addElement("N/A");
		else
		{
			cut4Box.addElement("N/A");
			for (int i = 0; i < selBank.getItems().size(); i++)
				cut4Box.addElement(selBank.getItems().get(i).getName());
		}

		final JComboBox cut4Combo = new JComboBox(cut4Box);
		cut1Combo.setSelectedIndex(0);

		JScrollPane cut4ScrollPane = new JScrollPane(cut4Combo);

		JButton showCutButton = new JButton("Enter");

		showCutButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String data = "";
				if (cut1Combo.getSelectedIndex() != -1)
				{
					if (clasDetectorName == null || !bankReady)
					{
						System.out.println("\n Complete all selection first.");
						return;
					}
					cut1Name = (String) cut1Combo.getItemAt(cut1Combo.getSelectedIndex());
					cut2Name = (String) cut2Combo.getItemAt(cut2Combo.getSelectedIndex());
					cut3Name = (String) cut3Combo.getItemAt(cut3Combo.getSelectedIndex());
					cut4Name = (String) cut4Combo.getItemAt(cut4Combo.getSelectedIndex());

					if (run == null)
					{
						System.out.println("\n Please enter a run number");
						return;
					}
					if (run.runExist())
					{
						System.out.println("\n Seleted cuts are:");
						System.out.println(" Cut1 variable:" + cut1Name);
						System.out.println(" Cut2 variable:" + cut2Name);
						System.out.println(" Cut3 variable:" + cut3Name);
						System.out.println(" Cut4 variable:" + cut4Name);
						selectionIsMade = true;
					} else
					{
						System.out.println("\n The requested run NOT found ");
						return;
					}
				}
			}
		});
		cutPanel.add(cut1Label);
		cutPanel.add(cut1ScrollPane);
		cutPanel.add(cut2Label);
		cutPanel.add(cut2ScrollPane);
		cutPanel.add(cut3Label);
		cutPanel.add(cut3ScrollPane);
		cutPanel.add(cut4Label);
		cutPanel.add(cut4ScrollPane);
		cutPanel.add(showCutButton);
		cutPanel.updateUI();
		mainFrame.setVisible(true);
	}

	// --------- Cut expression area --------------------------
	public void cutExpArea()
	{
		JLabel expLabel = new JLabel("Expression: ", JLabel.CENTER);
		final JTextField expText = new JTextField(20);

		JButton enterExpButton = new JButton("Enter");
		enterExpButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cutExpStr = expText.getText();
				if (cutExpReady && selectionIsMade)
				{
					System.out.println("\n Selection entered: " + cutExpStr);					
				} else
					System.out.println("\n Make other selections first.");
			}
		});
		cutExpPanel.add(expLabel);
		cutExpPanel.add(expText);
		cutExpPanel.add(enterExpButton);

		mainFrame.setVisible(true);
	}


	// --------------------- Text Area --------------------------
	public void textArea()
	{
		// -------------- The text area -------------------------------
		textArea = new JTextArea(40, 80);
		textArea.setEditable(false);
		textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
		PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));

		// keeps reference of standard output stream
		standardOut = System.out;

		// re-assigns standard output stream and error output stream
		System.setOut(printStream);
		System.setErr(printStream);
	}

	public void makePlot()
	{
		JButton plotXButton = new JButton("Plot X 1D");
		plotXButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!xReady)
				{
					System.out.println("\n Invalid value entered");
					return;
				}
				if(threadIsAlive)
				{
					System.out.println("\n !!!! You must wait until current filling  is done !!!\n");
					return;
				}
	
				if (run.runExist())
				{
					threadIsAlive = true;
					explorerThread = new Thread(new Runnable()
					{
						public void run()
						{
							if (cutExpStr.equals("") || cutExpStr.equals("N/A") || !selectionIsMade)
							{
								System.out.println("\n Filling the histogram ....");
								new RunExplorer().make1DPlot(run, clasDetectorName, bankXName, xStr, xnBinsInt,
										xMinFloat,
										xMaxFloat);
							}
							else
							{
								System.out.println("\n Filling the histogram with cut selected....");
								new RunExplorer().make1DPlotCut(run, clasDetectorName, bankXName, xStr, cut1Name,
										cut2Name, cut3Name, cut4Name, cutExpStr, xnBinsInt, xMinFloat,
										xMaxFloat);
							}
							System.out.println("\n Done with the histogram!");
							threadIsAlive = false;
						}
					});
					explorerThread.start();
				}
				else
					System.out.println("\n The requested run NOT found.");
			}
		});

		JButton plotYButton = new JButton("Plot Y 1D");
		plotYButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!yReady)
				{
					System.out.println("\n Invalid value entered");
					return;
				}
				if(threadIsAlive)
				{
					System.out.println("\n !!!! You must wait until current filling  is done !!!\n");
					return;
				}

				if (run.runExist())
				{
					threadIsAlive = true;
					explorerThread = new Thread(new Runnable()
					{
						public void run()
						{
							if (cutExpStr.equals("") || cutExpStr.equals("N/A") || !selectionIsMade)
							{
								System.out.println("\n Filling the histogram ....");
								new RunExplorer().make1DPlot(run, clasDetectorName, bankXName, yStr, ynBinsInt,
										yMinFloat,
										yMaxFloat);
							}
							else
							{
								System.out.println("\n Filling the histogram with cut selected....");
								new RunExplorer().make1DPlotCut(run, clasDetectorName, bankXName, yStr, cut1Name,
										cut2Name, cut3Name, cut4Name, cutExpStr, ynBinsInt, yMinFloat,
										yMaxFloat);
							}
							System.out.println("\n Done with the histogram!");
							threadIsAlive = false;
						}
					});
					explorerThread.start();
				}
				else
					System.out.println("\n The requested run NOT found.");
			}
		});

		JButton plotXYButton = new JButton("Plot XY 2D");
		plotXYButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!xReady || !yReady)
				{
					System.out.println("\n Invalid value entered");
					return;
				}
				if(threadIsAlive)
				{
					System.out.println("\n !!!! You must wait until current filling  is done !!!\n");
					return;
				}

				if (run.runExist())
				{
					threadIsAlive = true;
					explorerThread = new Thread(new Runnable()
					{
						public void run()
						{

							if (cutExpStr.equals("") || cutExpStr.equals("N/A") || !selectionIsMade)
							{
								System.out.println("\n Filling the histogram ....");
								new RunExplorer().make2DPlot(run, clasDetectorName, bankXName, xStr, yStr, xnBinsInt,
										xMinFloat,
										xMaxFloat, ynBinsInt, yMinFloat, yMaxFloat);
							}
							else
							{
								System.out.println("\n Filling the histogram with cut selected ....");
								new RunExplorer().make2DPlotCut(run, clasDetectorName, bankXName, xStr, yStr, cut1Name,
										cut2Name, cut3Name, cut4Name, cutExpStr, xnBinsInt, xMinFloat,
										xMaxFloat, ynBinsInt, yMinFloat, yMaxFloat);
							}

							System.out.println("\n Done with the histogram!");
							threadIsAlive = false;
						}
					});
					explorerThread.start();
				}
				else
					System.out.println("\n The requested run NOT found.");
			}
		});

		plotPanel.add(plotXButton);
		plotPanel.add(plotYButton);
		plotPanel.add(plotXYButton);
		mainFrame.setVisible(true);
	}

	// -------------------- Combine all the panels with the main frame --------------------
	public void combineComponents()
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.anchor = GridBagConstraints.WEST;

		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		// ------------------ Add all the button/box/text area components to the frame------------
		container.add(blankLabel);
		container.add(headerLabel);
		container.add(pathPanel);
		container.add(runPanel);
		container.add(eventPanel);
		container.add(detControlPanel);
		container.add(controlPanel);
		container.add(xcontrolPanel);
		container.add(ycontrolPanel);
		container.add(cutPanel);
		container.add(cutExpPanel);
		container.add(plotPanel);
		container.add(textArea);
		container.add(new JScrollPane(textArea), constraints);

		mainFrame.add(container);
		// mainFrame.pack();
		mainFrame.setVisible(true);

		System.out.println(
				"\t\t-----------------------------------------------------------------------------");
		System.out.println("\t\t\t\t\tWelcome to CLAS Data Explorer");
		System.out.println(
				"\t\t-----------------------------------------------------------------------------");
		System.out.println("\n\n\tGeneral Instructions:");
		System.out.println("\t----------------------");
		System.out.println("\n\t1. Select the hipo file you want to explore.");
		System.out.println("\n\t2. The bank menu and menu for variables are dynamic. It will get "
				+ "updated based on parent selection.");
		System.out.println("\n\t3. To select cut, use logical expressions in terms of a,b,c,d."
				+ " An example is a==2 && b==1");
		System.out.println("\n\t   Report issues or bug to: latif@jlab.org");
		System.out.println("\n\t -----------------------------------------------------------------------------------\n\n");
	}

	// ----------------- Call to Analysis To Explore----------------
	public void Explorer()
	{

		System.out.println("\nFor X set");
		System.out.println(" nBins: " + xnBinsInt);
		System.out.println(" xMin: " + xMinFloat);
		System.out.println(" xMax: " + xMaxFloat);

		System.out.println("\nFor Y set");
		System.out.println(" nBins: " + ynBinsInt);
		System.out.println(" yMin: " + yMinFloat);
		System.out.println(" yMax: " + yMaxFloat);

	}
}