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
import javax.swing.event.*;
import java.awt.Toolkit;
import java.text.*;
import java.io.*;
import java.lang.reflect.*;

import globals.*;
import classesWrittenByOthers.*;
import votingObjects.*;
import actions.*;
import com.tomtessier.scrollabledesktop.*;
import parliaments.*;
import parliaments.uninominal.*;
//import parliaments.plurinominal.*;

public class ListOfParliaments {
	MainFrame mainFrame;
	Language language = Language.getInstance();
	HashMap <String,String> uninominalSystems = new HashMap <String,String> ();
	HashMap <String,String> plurinominalSystems = new HashMap <String,String> ();
	int numberCandidates;
	SimulationRepository simulationRepository;
	JInternalFrame listFrame;
	static Parliament newParliament;
	static JProgressBar progressBar = new JProgressBar();
	static boolean showProgressBar = true;
	static JTextField displayInProgressPanel = new JTextField(25);
	classesWrittenByOthers.SwingWorker worker;

	
	public ListOfParliaments (SimulationRepository simRep) {
		super();
		mainFrame = MainFrame.getInstance();
		simulationRepository = simRep;

		numberCandidates = ((Integer)(simulationRepository.getGeneralParameters()).get("numberCandidates")).intValue();

		progressBar.setStringPainted(true);
		progressBar.setMinimum(0);

	}

	// functions to access some of the variables, needed by parliaments (eg vst)
	static public JProgressBar getProgressBar() {
		return progressBar;
	}


	static public void setDisplayInProgressPanel(String display) {
		displayInProgressPanel.setText(display);
	}

//	public String getDisplayInProgressPanel() {
//		return displayInProgressPanel;
//	}
//
	private void resetDefaults() {
		displayInProgressPanel.setText("");
		progressBar.setValue(0);
	}

	// show list of parliaments
	public void showListOfParliaments() {
System.out.println("numberCandidates "+numberCandidates);
		JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
		// check whether a window for simulation already exists: if so, select it; if no, create a new one and add to desktop
		JDesktopPane deskPane = mainFrame.getDesktopPane();
		if (deskPane!=null) {
			JInternalFrame[] intFrames=deskPane.getAllFrames();
			int n=intFrames.length;
			String frameTitle =language.getString("labels","allSystems")+" - "+
					simulationRepository.getRepositoryName()+" - "+
					simulationRepository.getRepositoryDescription();
			boolean alreadyExists = false;
			for (int i=0;i<n;++i) {
				JInternalFrame intframe = intFrames[i];
				if (frameTitle.compareTo(intframe.getTitle())==0) {
					desktop.setSelectedFrame(intframe);
					alreadyExists = true;
					break;
				}
			}
			if (alreadyExists==false) {
				JPanel panel = new JPanel();
				GridBagLayout gridbag = new GridBagLayout();
				GridBagConstraints c = new GridBagConstraints();
				panel.setLayout(gridbag);
				gridbag.setConstraints(panel,c);
				c.gridx = 0;
				c.gridy = 0;
				c.ipady = 20;
				c.ipadx = 20;
				c.insets = new Insets(10,10,10,10);
				c.anchor = GridBagConstraints.LINE_START;
				// uninominal
				panel.add(new TitleLabel(language.getString("labels","uninominalSystems")),c);
				c.ipady = 0;
				ButtonGroup buttonGroup = new ButtonGroup();
				LinkedList <JRadioButton> radioButtons = new LinkedList <JRadioButton> ();
				try {
					String filename = new String("languages/uninominal_"+language.getCurrentLanguage()+".properties");
System.out.println("file name "+filename);
					File f = new File (filename);
					FileReader fr = new FileReader(f);
					BufferedReader bf = new BufferedReader(fr);
					String s = new String();
					int y = 1;
					while ((s = bf.readLine())!=null) {
						if (s.indexOf("=")>0) {
							StringTokenizer st = new StringTokenizer(s,"=");
							String key = st.nextToken().trim();
System.out.println("key = "+key);
							String name = st.nextToken().trim();
System.out.println("name = "+name);
							uninominalSystems.put(key,name);
							c.gridy = y;
							JRadioButton radioButton = new JRadioButton(createParliament(name,"uninominal."+key,simulationRepository));
							radioButtons.add(radioButton);
							buttonGroup.add(radioButton);
							panel.add(radioButton,c);
							++y;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				// plurinominal
				if (numberCandidates>1) {
					c.gridx = 1;
					c.gridy = 0;
					c.ipady = 20;
					panel.add(new TitleLabel(language.getString("labels","plurinominalSystems")),c);
					c.ipady = 0;
					try {
						File f = new File ("languages/plurinominal_"+language.getCurrentLanguage()+".properties");
						FileReader fr = new FileReader(f);
						BufferedReader bf = new BufferedReader(fr);
						String s = new String();
						int y = 1;
						while ((s = bf.readLine())!=null) {
							if (s.indexOf("=")>0) {
								StringTokenizer st = new StringTokenizer(s,"=");
								String key = st.nextToken().trim();
								String name = st.nextToken().trim();
								plurinominalSystems.put(key,name);
								c.gridy = y;
							JRadioButton radioButton = new JRadioButton(createParliament(name,"plurinominal."+key,simulationRepository));
							radioButtons.add(radioButton);
							buttonGroup.add(radioButton);
							panel.add(radioButton,c);
								++y;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				desktop.add(frameTitle,panel);
			} // end of if alreadyExists
		} // end if desktop pane not null
	}

	Action createParliament(final String name,final String where,final SimulationRepository sr) {
		Action action = new AbstractAction(name) {
			public void actionPerformed (ActionEvent e) {
System.out.println("entered createParliament name is "+name+" where is "+where);
				try {
					// need to instantiate the parliament class with the name , looking in the package parliament
					Class<?> NewParliament = Class.forName("parliaments."+where);
System.out.println("name of class = "+NewParliament.getName());
					Class<?>[] argsConstructorClass = new Class[] {SimulationRepository.class};
System.out.println("class of arguments of constructor, class has "+argsConstructorClass.length+" elements");
					final Object[] argsConstructor = new Object[] {sr};
System.out.println("arguments of constructor");
					final Constructor<?> constructor = NewParliament.getConstructor(argsConstructorClass);
System.out.println("constructor");

				// create a swingWorker object to instanciate the parliament, and put the
				// gui creation in the finished function of the swingWorker, as the creation of the
				// parliament may be long... (VST: creation of preferences for candidates)
//				final NewParliament newParliament;
				final JPanel panel = new JPanel();
				GridBagLayout gridbag = new GridBagLayout();
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;
				panel.setLayout(gridbag);

				final JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
				final JInternalFrame progressFrame = new BaseInternalFrame(language.getString("labels","creationParliament"));

				worker = new classesWrittenByOthers.SwingWorker () {
					public Object construct() {
System.out.println("start construct");
	 					try {
							newParliament = (Parliament)constructor.newInstance(argsConstructor);
	//				 		panel.add(displayInProgressPanel);
							progressFrame.setVisible(true);
							} catch (InstantiationException er) {
								JOptionPane.showMessageDialog(mainFrame,language.getString("messages","instantiation")+" "+name);
							} catch (IllegalAccessException er) {
								JOptionPane.showMessageDialog(mainFrame,language.getString("messages","illegalAccess")+" "+name);
							} catch (InvocationTargetException er) {
		er.printStackTrace();
								JOptionPane.showMessageDialog(mainFrame,language.getString("messages","invocationTarget")+" "+name+". "+er.getCause());
							}
System.out.println("List Of Parliaments: construct is done");
						return newParliament.getReturnCode();
						}
						
					public void finished() {
System.out.println("start finish");
						String returned = get().toString();
System.out.println("return from thread is "+returned);
						if (returned.compareTo("interrupted")==0) {
System.out.println("thread has been interrupted");						
							JOptionPane.showMessageDialog(mainFrame,language.getString("labels","interrupted"));
						} else if (returned.compareTo("done")==0) {
							// find the frame which contains a progress bar and dispose of it
							resetDefaults();//set showProgressBar, ecc to their default values
							// start the gui (which takes newParliament as an argument)
							// or open parliament window if it alreadyExists
							String nameOfParliamentWindow = newParliament.getParliamentName() + " " + newParliament.getNameDescrSimulation();
							JDesktopPane deskPane = mainFrame.getDesktopPane();
							boolean alreadyExists = false;
							if (deskPane!=null) {
								JInternalFrame[] intFrames=deskPane.getAllFrames();
								int n=intFrames.length;
								for (int i=0;i<n;++i) {
									JInternalFrame intframe = intFrames[i];
									if (nameOfParliamentWindow.compareTo(intframe.getTitle())==0) {
										desktop.setSelectedFrame(intframe);
										alreadyExists = true;
										break;
									}
								}
							}
							if (alreadyExists==false) {
								GUIParliament guiParliament = new GUIParliament(newParliament);
							}
						} else {
							JOptionPane.showMessageDialog(mainFrame,language.getString("messages","interruptError"));
						}
						progressFrame.dispose();
					}// end of finished function
				}; // end of swingWorker
				worker.start();

				Action interruptAction = new AbstractAction(language.getString("labels","cancelSimulation")) {
					public void actionPerformed (ActionEvent e) {
						try {
							if (waitForUserConfirmation()) {
System.out.println("INTERRUPT THREAD");
								worker.interrupt();
							}
						} catch (InterruptedException er) {
System.out.println("not interrupted, already done");
						}
					}
				};		


//System.out.println("getshowprogressbar = "+getShowProgressBar());
//System.out.println(getDisplayInProgressPanel());
//		 		panel.add(new JLabel(getDisplayInProgressPanel()));
				c.gridx = 0;
				c.gridy = 0;
				panel.add(displayInProgressPanel,c);
				c.gridy=1;
				panel.add(progressBar,c);
				c.gridy=2;
				JButton button=new JButton(interruptAction);
				panel.add(button,c);
				progressFrame.getContentPane().add(panel);
				progressFrame.pack();
				progressFrame.setVisible(true);
				desktop.add(progressFrame);
				desktop.setSelectedFrame(progressFrame);

System.out.println("parliament");
					
//				} catch (InstantiationException er) {
//					JOptionPane.showMessageDialog(mainFrame,language.getString("messages","instantiation")+" "+name);
				} catch (NoSuchMethodException er) {
					JOptionPane.showMessageDialog(mainFrame,language.getString("messages","noSuchMethod")+" "+name);
//				} catch (InvocationTargetException er) {
//er.printStackTrace();
//					JOptionPane.showMessageDialog(mainFrame,language.getString("messages","invocationTarget")+" "+name+". "+er.getCause());
//				} catch (IllegalAccessException er) {
//					JOptionPane.showMessageDialog(mainFrame,language.getString("messages","illegalAccess")+" "+name);
				} catch (ClassNotFoundException er) {
					JOptionPane.showMessageDialog(mainFrame,language.getString("messages","classNotFound")+" "+name);
				} 
			}
		};
		return action;
	}


	// create a Runnable class that shows the confirmation dialog and stores the response
	boolean waitForUserConfirmation () throws InterruptedException {
		class DoShowDialog implements Runnable {
			boolean proceedConfirmed;
			public void run() {
				final String title=language.getString("labels","cancelConfirm");
				int n=JOptionPane.showConfirmDialog(mainFrame,title,"",JOptionPane.YES_NO_OPTION);
				proceedConfirmed = (n== JOptionPane.YES_OPTION);
			}// end run
		}// end class

		DoShowDialog doShowDialog = new DoShowDialog();

		doShowDialog.run();

//		// call with with SwingUtilities.invokeAndWait to stop the thread
//		try {
//			SwingUtilities.invokeAndWait(doShowDialog);
//		} catch (java.lang.reflect.InvocationTargetException e) {
//			e.printStackTrace();
//		}
//
		return doShowDialog.proceedConfirmed;
	}// end waitForUserConfirmation method

	static public void updateProgressBar(final int index,final String what) {
		Runnable setValueAndString = new Runnable () {
			public void run() {
				progressBar.setValue(index);
				progressBar.setString(what);
			}
		};
		SwingUtilities.invokeLater(setValueAndString);
	}

}


