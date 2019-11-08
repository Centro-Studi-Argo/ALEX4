/*   Copyright: Marie-Edith Bissey, Guido Ortona - 2007
*    Dipartimento di Politiche Pubbliche e Scelte Collettive
*    Universita' del Piemonte Orientale (Italia)
*    Contact: bissey@sp.unipmn.it

*    This file is part of ALEX4.1
*
*    ALEX4.1 is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    ALEX4.1 is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with ALEX4.1; if not, write to the Free Software
*    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package gui;
																   
import java.lang.*;												   
import java.util.*;												   
import java.awt.*;												   
import java.awt.event.*;										   
import java.awt.Color.*;										   
import javax.swing.*;											   
import javax.swing.border.*;									   
import javax.swing.table.*;										   
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.Toolkit;										   
import java.text.*;												   
import java.io.*;
import java.lang.reflect.*;
import java.beans.*;

import classesWrittenByOthers.*;
import globals.*;
import com.tomtessier.scrollabledesktop.*;
import parliaments.*;
import parliaments.uninominal.*;
//import parliaments.plurinominal.*;
import graphs.*;
import indexes.*;
import actions.*;
import votingObjects.*;

public class GUIParliament extends JPanel implements ParliamentEventListener
	{
//	// global variables for the class
	public MainFrame mainFrame;

	HashMap <Integer,Integer> seatAllocation = new HashMap<Integer,Integer>();
	String keyParliament;
	String nameParliament;
	ArrayList <Party>arrayOfParties = new ArrayList<Party>();

	Language language = Language.getInstance();
	final Parliament parliament;

	private LinkedList<Integer> government = new LinkedList<Integer>();
	private int sumOfVotes = 0;
	private boolean majorityFound=false;
	private double majorityLevel;
	Dimension smallDim = new Dimension();
	
	// constructor
	public GUIParliament(Parliament parl) {
		parliament = parl;
		seatAllocation = parliament.getAllocationOfSeats();
		nameParliament = parliament.getParliamentName();
		keyParliament = parliament.getParliamentKey();
		arrayOfParties = parliament.getArrayOfParties();
		majorityLevel = parliament.getMajorityLevel();
		// some gui stuff
		mainFrame=MainFrame.getInstance();

		// get desktop and add panel containing the gui
		JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
		desktop.add(nameParliament+" "+parliament.getNameDescrSimulation(),guiParliament());

		parliament.addParliamentEventListener(this);
	}

	public void governmentHasChanged(ParliamentEvent evt) {
		// if it receives an indication from parliament that government has changed:
		System.out.println("-------------\nGovernment has changed\n...............");
		updatePanels();
	}

	protected void updatePanels() {
		JPanel newPanel = guiParliament();
		// close any window opened on desktop
		JDesktopPane deskPane = mainFrame.getDesktopPane();
		if (deskPane!=null) {
			JInternalFrame[] intFrames=deskPane.getAllFrames();
			String begFrameTitle =nameParliament+" "+parliament.getNameDescrSimulation();
			for (int k=0;k<intFrames.length;++k) {
				JInternalFrame intframe = intFrames[k];
				if (intframe.getTitle().startsWith(begFrameTitle)) {
					intframe.dispose();
					break;
				}
			} // end for
		}// end if deskPane 
		// get desktop and add panel containing the gui
		JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
		desktop.add(nameParliament+" "+parliament.getNameDescrSimulation(),newPanel);
		
	}
	
	protected JPanel guiParliament() {
		// create main panel
		JPanel mainPanel = new JPanel();
		// create parliament panel
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2,2));

		// get dimension of desktop, and set dimension of panel (should be 90% of desktop)
		Dimension dimDesk = mainFrame.getScrollableDesktop().getSize();
		int height = (int)(dimDesk.height * 0.8);
		int width = (int)(dimDesk.width * 0.8);

		// dimensions for each small panel: half that of main panel
		int smallHeight = (int)(height/2);
		int smallWidth = (int)(width/2);
		smallDim = new Dimension(smallWidth,smallHeight);
		// dimensions for buttonPanel:
		int buttonHeight = (int)(smallHeight/4);
		Dimension buttonDim = new Dimension(smallWidth,buttonHeight);

		// create 4 subpanels, all the same size
		Border border = BorderFactory.createLineBorder(Color.black);

		// 1- graph of the parliament, with legend indicating which colour is which
		JPanel graphPanel = createGraphPanel((int)(smallHeight*0.7));
		graphPanel.setMinimumSize(smallDim);
		graphPanel.setPreferredSize(smallDim);
		graphPanel.setMaximumSize(smallDim);
		graphPanel.setBorder(border);
		panel.add(graphPanel,BorderLayout.NORTH);		
		JScrollPane graphPane = new JScrollPane(graphPanel);
		graphPane.setPreferredSize(graphPanel.getPreferredSize());
		panel.add(graphPane);

		// 2- checkboxes for creating the government
		JPanel gvtPanel = createGvtPanel();
//		gvtPanel.setMinimumSize(smallDim);
//		gvtPanel.setPreferredSize(smallDim);
//		graphPanel.setMaximumSize(smallDim);
		gvtPanel.setBorder(border);
		panel.add(gvtPanel);		
		JScrollPane gvtPane = new JScrollPane(gvtPanel);
		gvtPane.setPreferredSize(smallDim);
		panel.add(gvtPane);

		// 3- table confronting the seats of each party with current system and one district proportionality
		int compWidth = (int)((smallDim.width)*0.8);//(int)((smallDim.width/2)*0.9);
		int compHeight = (int)(smallDim.height);
		JPanel compPanel = createCompPanel();
//		compPanel.setPreferredSize(new Dimension(compWidth,compHeight));
//		compPanel.setMinimumSize(smallDim);
//		compPanel.setMaximumSize(smallDim);
		compPanel.setBorder(border);
//		panel.add(compPanel);		
		JScrollPane compPane = new JScrollPane(compPanel);
		compPane.setPreferredSize(new Dimension(compWidth,compHeight));
//		compPane.setViewportView(compPanel);
//		compPane.setPreferredSize(compPanel.getPreferredSize());
		panel.add(compPane);

		// 4- list of indexes
		JPanel indexPanel = createIndexPanel();
		indexPanel.setMinimumSize(smallDim);
		indexPanel.setMaximumSize(smallDim);
		indexPanel.setPreferredSize(smallDim);
		indexPanel.setBorder(border);
		panel.add(indexPanel);		
		JScrollPane indexPane = new JScrollPane(indexPanel);
		indexPane.setPreferredSize(indexPanel.getPreferredSize());
		panel.add(indexPane);

		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(panel,BorderLayout.PAGE_START);
		// buttons
		JPanel buttonPanel = createButtonPanel();
//		buttonPanel.setMinimumSize(buttonDim);
//		buttonPanel.setMaximumSize(buttonDim);
//		buttonPanel.setPreferredSize(buttonDim);
//		buttonPanel.setBorder(border);
		mainPanel.add(buttonPanel,BorderLayout.PAGE_END);
		return mainPanel;
	}
		

	protected JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		JButton saveButton = new JButton(language.getString("labels","saveParliament"));
		saveButton.setActionCommand("save-parliament");
		saveButton.addActionListener(new MenuElectoralSystemsActions(parliament));
		panel.add(saveButton);
		return panel;
	}
	
	protected JPanel createGraphPanel(int size) {
		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		panel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new TitleLabel(language.getString("labels","graphPanel")),c);
		c.gridy = 1;
		SemiCircle g=new SemiCircle(parliament.getAllocationOfSeats(),new Dimension(size,size),arrayOfParties);
		panel.add(g,c);
		String note = parliament.getNoteToGraph();
		if (note.length()>0) {
			c.gridx = 0;
			++c.gridy;
			panel.add(new JLabel(note),c);
		}
		return panel;
	}


	protected JPanel createCompPanel() {		
		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		panel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		panel.add(new TitleLabel(language.getString("labels","compPanel")),c);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		panel.add(new TitleLabel("<html>"+language.getString("labels","current")+" "+parliament.getParliamentName()),c);
		boolean notProportional = ((parliament.getParliamentKey()).compareTo("OneDistrictProportional")!=0);
		if (notProportional) {
			c.gridx = 0;
			++c.gridy;
			panel.add(new TitleLabel("<html>"+language.getString("labels","reference")+" "+language.getString("uninominal","OneDistrictProportional")),c);
		}
		HashMap <Integer,LinkedList<Integer>> mapForTable = new HashMap<Integer,LinkedList<Integer>>();
		HashMap <Integer,Integer> curAllocSeats = parliament.getAllocationOfSeats();
		int nbcols = 0;
//		if (notProportional) {
//System.out.println("the system is not proportional");
			HashMap <Integer,Integer> propAllocSeats = parliament.getProportionalAllocationOfSeats();
			HashMap <Integer,Integer> currentVotes = parliament.getVotesForParliament();
			Set <Integer> keysmi = curAllocSeats.keySet();
			Iterator <Integer> mi = keysmi.iterator();
			while (mi.hasNext()) {
				Integer key = mi.next();
				LinkedList <Integer> vals = new LinkedList<Integer>();
				vals.add(0,curAllocSeats.get(key));
				vals.add(1,currentVotes.get(key));
				if (notProportional) {
					vals.add(2,propAllocSeats.get(key));
					nbcols=4;
				} else {
					nbcols=3;
				}
				mapForTable.put(key,vals);
			}
//			nbcols=3;
//		} else {
//			mapForTable = curAllocSeats;
//			nbcols=2;
//		}
//System.out.println("size of mapForTable: "+mapForTable.size());
//System.out.println("value of notProportional "+notProportional);
		JTable tableCurrent = new JTable(new TableSeatsDisplayModel(mapForTable,notProportional));
		JScrollPane currentScrollPane = new JScrollPane(tableCurrent);

		c.gridx = 0;
		++c.gridy;
		++c.gridy;
		panel.add(currentScrollPane,c);

		String note = parliament.getNoteToComposition();
		if (note.length()>0) {
			c.gridx = 0;
			++c.gridy;
			panel.add(new JLabel(note),c);
		}

		return panel;
	}
	
	class TableSeatsDisplayModel extends AbstractTableModel {
	
		String[] columnNames;
		Object[][] mapOfSeats;
		public TableSeatsDisplayModel(HashMap <Integer,LinkedList<Integer>> map,boolean notProportional) {
			if (notProportional) {
				columnNames = new String[4];
				columnNames[0] = language.getString("labels","parties");
				columnNames[1] =  language.getString("labels","nbSeats")+" "+language.getString("labels","current");
				columnNames[2] = language.getString("labels","nbVotes")+" "+language.getString("labels","current");
				columnNames[3] =  language.getString("labels","nbSeats")+" "+language.getString("labels","reference");
				mapOfSeats = new Object[map.size()][4];
			} else {
				columnNames = new String[3];
				columnNames[0] = language.getString("labels","parties");
				columnNames[1] =  language.getString("labels","nbSeats")+" "+language.getString("labels","current");
				columnNames[2] = language.getString("labels","nbVotes")+" "+language.getString("labels","current");
				mapOfSeats = new Object[map.size()][3];
			}
			for (int j = 0;j<map.size();++j) {
				Integer indparty = new Integer(j+1);
				Party party = arrayOfParties.get(j);
				String nameParty = party.getNameParty();
//				if (notProportional) {
					LinkedList <Integer> seats = map.get(indparty);
					Integer seatsc = seats.get(0);
					Integer votes = seats.get(1);
					if (notProportional) {
						Integer seatsp = seats.get(2);
//System.out.println("seatsc "+seatsc+" seatsp "+seatsp);
					mapOfSeats[j] = new Object[] {nameParty,seatsc,votes,seatsp};
					} else {
						mapOfSeats[j] = new Object[] {nameParty , seatsc,votes};
					}
			}
		}
	
		public int getColumnCount() {
//System.out.println("number of cols: "+columnNames.length);
			return columnNames.length;
		}
	
		public int getRowCount() {
			return mapOfSeats.length;
		}
	
		public String getColumnName(int col) {
//System.out.println("column name "+columnNames[col]);
			return columnNames[col];
		}
	
		public Object getValueAt(int row,int col) {
//System.out.println("value at the place "+row+","+col+" is "+ mapOfSeats[row][col]);
			return mapOfSeats[row][col];
		}

		public Class getColumnClass(int c) {
			return getValueAt(0,c).getClass();
		}
		
	}

	protected JPanel createGvtPanel() {
		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		panel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		TitleLabel titleLabel = new TitleLabel(language.getString("labels","gvtPanel"));
		titleLabel.setToolTipText(language.getString("labels","toolTipGvtPanel"));
		panel.add(titleLabel,c);

		// create partity boxes
		HashMap <Integer,Integer> allocSeats = parliament.getAllocationOfSeats();
		LinkedList <Integer> currentGovernment = parliament.getCurrentGovernment();
		final JLabel majorityField = new JLabel();
		for(int i = 0;i<allocSeats.size();++i) {
			Integer partyName = new Integer(i+1);
			Party party = arrayOfParties.get(i);
			String nameParty = party.getNameParty();
			int nbSeats = (allocSeats.get(partyName)).intValue();
			if (nbSeats>0) {
			++c.gridy;
			boolean isSelected = (currentGovernment.contains(partyName));
			JCheckBox checkbox = new JCheckBox(nameParty,isSelected);
			checkbox.addActionListener(checkGvt(partyName,nbSeats,majorityField));
			panel.add(checkbox,c);
			}
		}
		++c.gridy;
		panel.add(new JLabel(language.getString("labels","majority")),c);
		++c.gridx;
		majorityField.setText((majorityFound==true) ? language.getString("labels","majorityFound") : language.getString("labels","majorityNotFound"));
		panel.add(majorityField,c);
		// reset button unchecks governement boxes and resets government list
		++c.gridy;		
		JButton resetButton = new JButton(resetGvt(panel));
		panel.add(resetButton,c);
		String note = parliament.getNoteToGovernment();
		if (note.length()>0) {
			c.gridx = 0;
			++c.gridy;
			panel.add(new JLabel(note),c);
		}
		return panel;
	}

	// action that listens to whether the majority is found
	// and creates the governement list
//	Action checkGvt (final Integer partyName,final int nbSeats,final JTextField majorityField) {
	Action checkGvt (final Integer partyName,final int nbSeats,final JLabel majorityField) {
		Action action= new AbstractAction (partyName.toString()) {
			public void actionPerformed(ActionEvent e) {
System.out.println("doing checkGvt");
				int majority=(parliament.getSizeOfParliament()*(int)majorityLevel/100);
				if (majorityLevel==50) {
					++majority;
				};
				LinkedList <Integer> currentGovernment = parliament.getCurrentGovernment();
				
            	JCheckBox cb = (JCheckBox)e.getSource();
				boolean isSelected = cb.isSelected();
System.out.println("party: "+partyName.toString()+" selected "+isSelected+" currentGovernment "+currentGovernment.toString());			
				if (isSelected) {
					government.add(partyName);
					sumOfVotes+=nbSeats;
					majorityFound = (sumOfVotes>=majority) ? true : false;
					parliament.updateParliament(government,majorityFound,sumOfVotes);
				} else {
					government.remove(partyName);
					sumOfVotes-=nbSeats;
					majorityFound = (sumOfVotes>=majority) ? true : false;
					parliament.updateParliament(government,majorityFound,sumOfVotes);
				}
				majorityField.setText((majorityFound==true) ? language.getString("labels","majorityFound") : language.getString("labels","majorityNotFound"));
			}
		};
		return action;
	}

	// action to reset all
	Action resetGvt (final JPanel panel) {
		Action action = new AbstractAction(language.getString("labels","resetGvt")) {
			public void actionPerformed (ActionEvent e) {
				government = new LinkedList <Integer> ();
				sumOfVotes = 0;
				majorityFound = false;
				parliament.updateParliament(government,majorityFound,sumOfVotes);
				// get all checkboxes of panel and set unselected + set majority textField to its "non found" value
				Component[] components = panel.getComponents();
				for (int i=0;i<components.length;++i) {
					if ((components[i].getClass().getName()).compareTo("javax.swing.JCheckBox")==0){
						JCheckBox ck = (JCheckBox)components[i];
						ck.setSelected(false);
					} else if ((components[i].getClass().getName()).compareTo("javax.swing.JLabel")==0) {
						JLabel tf = (JLabel)components[i];
						if (tf.getText().compareTo(language.getString("labels","majority"))!=0) {
							tf.setText(language.getString("labels","majorityNotFound"));
						}
					}
				}
			}
		};
		return action;
	}

	protected JPanel createIndexPanel() {
		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		panel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new TitleLabel(language.getString("labels","indexPanel")),c);

		// show all the indexes from indexes.properties file
		String indexResults = new String();
		try {
			File f = new File ("languages/indexes.properties");
			FileReader fr = new FileReader(f);
			BufferedReader bf = new BufferedReader(fr);
			String s = new String();
			int y = ++c.gridy;
			while ((s = bf.readLine())!=null) {
				if (s.indexOf("=")>0) {
					StringTokenizer st = new StringTokenizer(s,"=");
					String key = st.nextToken().trim();
					String name = st.nextToken().trim();
					// name of index
					c.gridx = 0;
					c.gridy = y;
					panel.add(new TitleLabel(language.getString("indexes",key)),c);
//					indexResults+="<b>"+language.getString("indexes",key)+ "</b> = ";
					// value of index
					c.gridx = 1;
					try {
						// need to instantiate the index class with the name , looking in the package parliament
						Class <?> NewIndex = Class.forName("indexes."+key);
						Class[] argsConstructorClass = new Class[] {Parliament.class};
						Object[] argsConstructor = new Object[] {parliament};
						Constructor <?> constructor = NewIndex.getConstructor(argsConstructorClass);
						final Index index = (Index)constructor.newInstance(argsConstructor);
						final IndexValue textLabel = new IndexValue(index);
//						index.addObserver(textLabel);
						textLabel.setText((index.getIndexValue()).toString());
						panel.add(textLabel,c);
//						indexResults+=(index.getIndexValue()).toString()+"<br>";
					} catch (InstantiationException er) {
						JOptionPane.showMessageDialog(mainFrame,language.getString("messages","instantiation")+" "+name);
					} catch (NoSuchMethodException er) {
						JOptionPane.showMessageDialog(mainFrame,language.getString("messages","noSuchMethod")+" "+name);
					} catch (InvocationTargetException er) {
						JOptionPane.showMessageDialog(mainFrame,language.getString("messages","invocationTarget")+" "+name);
					} catch (IllegalAccessException er) {
						JOptionPane.showMessageDialog(mainFrame,language.getString("messages","illegalAccess")+" "+name);
					} catch (ClassNotFoundException er) {
						JOptionPane.showMessageDialog(mainFrame,language.getString("messages","classNotFound")+" "+name);
					} 

					++y;
				}
			}
			++c.gridy;
//			JEditorPane ir = new JEditorPane("text/html",indexResults);
//			ir.setEditable(false);
//			ir.setPreferredSize(new Dimension(smallDim.width-20,smallDim.height-50));
//			JScrollPane sir = new JScrollPane(ir);
//			sir.setPreferredSize(new Dimension(smallDim.width-20,smallDim.height-50));
//System.out.println("smallDim: "+smallDim.width+","+smallDim.height);
//			panel.add(new JLabel(indexResults),c);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String note = parliament.getNoteToIndexes();
		if (note.length()>0) {
			c.gridx =0;
			++c.gridy;
			panel.add(new JLabel(note),c);
		}
		++c.gridy;
		c.gridx=0;
		panel.add(new JLabel(" "),c);
		++c.gridy;
		JButton powerButton = new JButton(powerIndexes(panel));
		panel.add(powerButton,c);
		return panel;
	}

	// action to create power indexes
	Action powerIndexes (JPanel ppanel) {
		Action action = new AbstractAction(language.getString("labels","powerMessage")) {
			public void actionPerformed (ActionEvent e) {
 				mainFrame=MainFrame.getInstance();
				final JDialog dialog = new JDialog(mainFrame);
				dialog.setPreferredSize(new Dimension(600,800));
				final JPanel panel = new JPanel();
				GridBagLayout gridbag = new GridBagLayout();
				final GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
				panel.setLayout(gridbag);
				c.gridx = 0;
				c.gridy = 0;
				final ArrayList <Index> simulateIndexes = new ArrayList <Index> ();
				
				// show all the indexes from power.properties file
				int width = 500;
				int maxwidth = 800;
				try {
					File f = new File ("languages/powerIndexes.properties");
					FileReader fr = new FileReader(f);
					BufferedReader bf = new BufferedReader(fr);
					String s = new String();
					while ((s = bf.readLine())!=null) {
						if (s.indexOf("=")>0) {
							StringTokenizer st = new StringTokenizer(s,"=");
							String key = st.nextToken().trim();
							String name = st.nextToken().trim();
							// name of index
							++c.gridy;
//							panel.add(new TitleLabel(name),c);
							try {
								// need to instantiate the index class with the name , looking in the package parliament
								Class <?> NewIndex = Class.forName("indexes."+key);
								Class[] argsConstructorClass = new Class[] {Parliament.class,HashMap.class};
								Object[] argsConstructor = new Object[] {parliament,parliament.getAllocationOfSeats()};
								Constructor <?> constructor = NewIndex.getConstructor(argsConstructorClass);
								// make it a checkbox
								final Index index = (Index)constructor.newInstance(argsConstructor);
								final JCheckBox box = new JCheckBox(language.getString("powerIndexes",key),false);
								final JPanel parpanel = index.parameters();
								enable(parpanel,false);
								panel.add(box,c);
								// parameters
								++c.gridy;
								c.gridx=0;
								panel.add(parpanel,c);
								int pwidth = parpanel.getWidth();
System.out.println("width "+width+" pwidth "+pwidth);
								if ((pwidth>width) && (pwidth<=maxwidth)) {
									width = pwidth;
								}
								box.addActionListener (new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										if (box.isSelected()) {
											simulateIndexes.add (index);
											enable(parpanel,true);
System.out.println("simulateIndexes has "+simulateIndexes.size());
										} else {
											simulateIndexes.remove (index);
											enable(parpanel,false);
System.out.println("simulateIndexes has "+simulateIndexes.size());
										}
									}
								});
							} catch (InstantiationException er) {
								JOptionPane.showMessageDialog(mainFrame,language.getString("messages","instantiation")+" "+name);
							} catch (NoSuchMethodException er) {
								JOptionPane.showMessageDialog(mainFrame,language.getString("messages","noSuchMethod")+" "+name);
							} catch (InvocationTargetException er) {
								JOptionPane.showMessageDialog(mainFrame,language.getString("messages","invocationTarget")+" "+name);
							} catch (IllegalAccessException er) {
								JOptionPane.showMessageDialog(mainFrame,language.getString("messages","illegalAccess")+" "+name);
							} catch (ClassNotFoundException er) {
								JOptionPane.showMessageDialog(mainFrame,language.getString("messages","classNotFound")+" "+name);
							} 
				
						}
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				JButton button = new JButton(language.getString("labels","powerMessage")) ;
				button.addActionListener (new ActionListener() {
					public void actionPerformed (ActionEvent e) {
						dialog.setVisible(false);
						mainFrame=MainFrame.getInstance();
						JPanel pani = new JPanel();
						GridBagLayout gridbagp = new GridBagLayout();
						GridBagConstraints cp = new GridBagConstraints();
						cp.fill = GridBagConstraints.VERTICAL;
						pani.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
						pani.setLayout(gridbagp);
						Iterator <Index> ind = simulateIndexes.iterator();
						cp.gridx=0;
						cp.gridy=0;
						String powerResults = new String(language.getString("labels","majorityLevel")+" "+parliament.getMajorityLevel()+"%<br>");
						while (ind.hasNext()) {
							Index pi = ind.next();
							pi.getIndex();
							Object pv = pi.getIndexValue();
							Object ppar = pi.recallParameters();
							String pvname =(pi.getClass()).getName();
//System.out.println("pvname "+pvname);
							pvname = pvname.substring(pvname.indexOf('.')+1);
//System.out.println("pvname "+pvname);
							powerResults+="<b>"+language.getString("powerIndexes",pvname)+"</b><br>";
							++cp.gridy;
//							if (ppar instanceof JPanel) {
//System.out.println("ppar is a panel");
//								enable((JPanel)ppar,false);
//								JScrollPane sppar = new JScrollPane((JPanel)ppar);
//								sppar.setPreferredSize(new Dimension(400,150));
							if (ppar!=null) {
								powerResults+=(String)ppar+"<br>";
							}
//							}
							++cp.gridy;
							powerResults+=pv.toString()+"<br>";
//System.out.println("powerResults "+powerResults);
 							// at this point, compute the representativeness indexes that use the power indexes:
							++cp.gridy;
							try {
								File f = new File ("languages/repPowerIndexes.properties");
								FileReader fr = new FileReader(f);
								BufferedReader bf = new BufferedReader(fr);
								String s = new String();
								while ((s = bf.readLine())!=null) {
									if (s.indexOf("=")>0) {
										StringTokenizer st = new StringTokenizer(s,"=");
										String key = st.nextToken().trim();
										String name = st.nextToken().trim();
System.out.println("key of index = "+key+" name of index = "+name);
										// name of index
										++cp.gridy;
										cp.gridx = 0;
										powerResults+="<b><i>"+language.getString("repPowerIndexes",key)+":</i></b> ";
System.out.println("powerResults "+powerResults);
										try {
											// need to instantiate the index class with the name , looking in the package parliament
											Class <?> NewRepIndex = Class.forName("indexes."+key);
											Class[] argsConstructorClass = new Class[] {Parliament.class,Index.class};
											Object[] argsConstructor = new Object[] {parliament,pi};
											Constructor <?> constructor = NewRepIndex.getConstructor(argsConstructorClass);
											final Index repIndex = (Index)constructor.newInstance(argsConstructor);
											repIndex.getIndex();
											String val = (String)repIndex.getIndexValue();
											cp.gridx = 1;
											powerResults+=" "+val+"<br>";
//System.out.println("powerResults "+powerResults);
											cp.gridx = 0;
										} catch (InstantiationException er) {
											JOptionPane.showMessageDialog(mainFrame,language.getString("messages","instantiation")+" "+name);
										} catch (NoSuchMethodException er) {
											JOptionPane.showMessageDialog(mainFrame,language.getString("messages","noSuchMethod")+" "+name);
										} catch (InvocationTargetException er) {
											JOptionPane.showMessageDialog(mainFrame,language.getString("messages","invocationTarget")+" "+name);
										} catch (IllegalAccessException er) {
											JOptionPane.showMessageDialog(mainFrame,language.getString("messages","illegalAccess")+" "+name);
										} catch (ClassNotFoundException er) {
											JOptionPane.showMessageDialog(mainFrame,language.getString("messages","classNotFound")+" "+name);
										}
									}
								}
							} catch (IOException ex) {
								ex.printStackTrace();
							}
							
							++cp.gridy;
//System.out.println("powerResults "+powerResults);
//							HTMLDocument doc = new HTMLDocument();
//							try {
//								doc.insertString(0,powerResults,null);
//							} catch (BadLocationException er) {
//								JOptionPane.showMessageDialog(mainFrame,"wrong offset");
//							}
						}
						JEditorPane pr = new JEditorPane("text/html",powerResults);
						pr.setEditable(false);
						JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
						JScrollPane spr = new JScrollPane(pr);
						spr.setPreferredSize(new Dimension(400,desktop.getHeight()-200));
						pani.add(spr,cp);
						desktop.add(language.getString("labels","powerMessage"),pani);
					}
				
				});
				++c.gridy;
				panel.add(button,c);
				JScrollPane spane = new JScrollPane(panel);
				spane.setPreferredSize(null);
				dialog.getContentPane().add(spane);
				
				dialog.setLocation(mainFrame.getX()+mainFrame.WIDTH/2,mainFrame.getY()+mainFrame.HEIGHT/2);
//				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.pack();
				dialog.setVisible(true);
				
			}
		};
		return action;
	}

	
	class IndexValue extends JLabel {// implements Observer {
		private Index index;
		IndexValue(Index ind) {
			super();
			index = ind;
//			index.addObserver(this);
		}
//		public void update(Observable obs,Object obj) {
//			this.setText(index.getIndexValue());
//		}
	}
	
	protected boolean getMajorityFound() {
		return majorityFound;
	}
	
	protected void enable(Container c,boolean val) {
		c.setEnabled(val);
		Component[] components= c.getComponents();;
		for (int i = 0 ; i < components.length ; ++i) {
			enable((Container)components[i],val);
		}
	}



}

