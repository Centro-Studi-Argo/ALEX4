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
package actions;

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
import java.lang.ref.*;

import globals.*;
import gui.*;
import votingObjects.*;
import classesWrittenByOthers.*;
import com.tomtessier.scrollabledesktop.*;
import inAndOut.*;
import parliaments.*;

public class MenuElectoralSystemsActions implements ActionListener {

	MainFrame mainFrame;
	Language language = Language.getInstance();
	RepositoryDataHandler repHandler = new RepositoryDataHandler();
	Parliament parliament;
	boolean saveSim=false;
	static JProgressBar progressBar = new JProgressBar();
	static boolean showProgressBar = true;
	static JTextField displayInProgressPanel = new JTextField(25);
	private classesWrittenByOthers.SwingWorker worker;

	/*
	 * Constructor
	 */
	public MenuElectoralSystemsActions () {

		super();
		mainFrame=MainFrame.getInstance();
		progressBar.setStringPainted(true);
		progressBar.setMinimum(0);
		
	}

	public MenuElectoralSystemsActions (Parliament parl) {
		super();
		mainFrame = MainFrame.getInstance();
		parliament = parl;
	}

	/*
	 * actions
	 */
	// functions to access some of the variables, needed by parliaments (eg vst)
	static public JProgressBar getProgressBar() {
		return progressBar;
	}


	static public void setDisplayInProgressPanel(String display) {
		displayInProgressPanel.setText(display);
	}
	private void resetDefaults() {
		displayInProgressPanel.setText("");
		progressBar.setValue(0);
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

	
	public void actionPerformed (ActionEvent event) {

		String whatTheMenuDoes=event.getActionCommand();
		String[] p = whatTheMenuDoes.split("-");
		String action = p[0];
		String what = "";
		if (p.length>1) {
			what = p[1];
		}
		
		if ((action.compareTo("open"))==0) {
			openSomething(what);
		} else if ((action.compareTo("save"))==0) {
			saveSomething(what);
		} else if ((action.compareTo("simulate"))==0) {
			simulateSomething(what);
		} else if ((action.compareTo("exit"))==0) {
			System.exit(0);
		} else if ((action.compareTo("edit"))==0) {
			editSomething(what);
		} else if ((action.compareTo("changeLanguage"))==0) {
			changeLanguage(what);
		} else if ((action.compareTo("show"))==0) {
			showSomething(what);
		} else if ((action.compareTo("close"))==0) {
			closeSomething(what);
		} else {
		JOptionPane.showMessageDialog(mainFrame.getContentPane(),language.getString("messages","menuActionError"));
		}
	}

	
	void openSomething (String what) {
		if ((what.compareTo("preferences"))==0) {

			// create a swingWorker object to load the file,
			final JPanel panel = new JPanel();
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			panel.setLayout(gridbag);

			final JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
			final JInternalFrame progressFrame = new BaseInternalFrame(language.getString("labels","openPreferences"));

			worker = new classesWrittenByOthers.SwingWorker () {
				public Object construct() {
System.out.println("start construct");
					repHandler.load();
System.out.println("construct is done");
					return repHandler.getReturnCode();
					}
					
				public void finished() {
System.out.println("start finish");
					progressFrame.dispose();
					String returned = get().toString();
System.out.println("return from thread is "+returned);
					if (returned.compareTo("interrupted")==0) {
System.out.println("thread has been interrupted");						
						JOptionPane.showMessageDialog(mainFrame,language.getString("labels","interrupted"));
						resetDefaults();
					} else if (returned.compareTo("done")==0) {
// 						JOptionPane.showMessageDialog(mainFrame,language.getString("messages","loadingSuccess"));
						resetDefaults();
					} else {
						JOptionPane.showMessageDialog(mainFrame,language.getString("labels","interrupted"));
						resetDefaults();
					}
				}// end of finished function
			}; // end of swingWorker
			worker.start();

			Action interruptAction = new AbstractAction(language.getString("labels","cancel")) {
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
		} else if ((what.compareTo("parliament"))==0) {

			// create a swingWorker object to load the file,
			final JPanel panel = new JPanel();
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			panel.setLayout(gridbag);

			final JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
			final JInternalFrame progressFrame = new BaseInternalFrame(language.getString("labels","openPreferences"));

			worker = new classesWrittenByOthers.SwingWorker () {
				public Object construct() {
System.out.println("start construct");
					repHandler.loadParliament();
System.out.println("construct is done");
					return repHandler.getReturnCode();
					}
					
				public void finished() {
System.out.println("start finish");
					progressFrame.dispose();
					String returned = get().toString();
System.out.println("return from thread is "+returned);
					if (returned.compareTo("interrupted")==0) {
System.out.println("thread has been interrupted");						
						JOptionPane.showMessageDialog(mainFrame,language.getString("labels","interrupted"));
						resetDefaults();
					} else if (returned.compareTo("done")==0) {
// 						JOptionPane.showMessageDialog(mainFrame,language.getString("messages","loadingSuccess"));
						resetDefaults();
					} else {
						JOptionPane.showMessageDialog(mainFrame,language.getString("labels","interrupted"));
						resetDefaults();
					}
				}// end of finished function
			}; // end of swingWorker
			worker.start();

			Action interruptAction = new AbstractAction(language.getString("labels","cancel")) {
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
		} else {
			JOptionPane.showMessageDialog(mainFrame.getContentPane(),"opening "+what);
		}
	}


	

	void closeSomething (String what) {
		if ((what.compareTo("preferences"))==0) {
			// saving preferences: show modal dialog with list of simulations, and when simulation chosen, open the Repository handler
			ListOfSimulations listOfSimulations = ListOfSimulations.getInstance();
			SimulationRepository simRep = null;
			int n = listOfSimulations.size();
			if (n == 0) {
				JOptionPane.showMessageDialog(mainFrame,language.getString("messages","simulatePreferencesFirst"));
				simRep = null;
			} else {
				Object[] choiceOfRepositories = new Object[n];
				for (int i = 0 ; i < n ; ++i) {
					SimulationRepository sr = (SimulationRepository)listOfSimulations.get(i);
					String repNameAndDesc = sr.getRepositoryName();
					String desc = sr.getRepositoryDescription();
					if (desc.compareTo("") != 0) {
						repNameAndDesc += " (" + desc + ")";
					}
					
					choiceOfRepositories[i] =  repNameAndDesc;
				}// end for
				Object obj = JOptionPane.showInputDialog(mainFrame,
						language.getString("messages","chooseRepositoryClosing"),
						language.getString("messages","closing"),
						JOptionPane.QUESTION_MESSAGE,
						null,
						choiceOfRepositories,
						null);
System.out.println("object = "+obj);
				if (obj != null) {
System.out.println("obj not null");
					Iterator i = listOfSimulations.iterator();
					while (i.hasNext()) {
						SimulationRepository sr = (SimulationRepository)i.next();
						String repNameAndDesc = sr.getRepositoryName();
						String desc = sr.getRepositoryDescription();
						if (desc.compareTo("") != 0) {
							repNameAndDesc += " (" + desc + ")";
						}
						
						if (repNameAndDesc.compareTo((String)obj) == 0) {
System.out.println("same name as repository");
							simRep = sr;
							break;
						}// end if
					}// end while
				}// end if obj
			}// and if n (size of listOfSimulations
			// now open the fileChooser for the repository
			if (simRep!=null) {
System.out.println("removing simulation"+simRep.getRepositoryName());
				// remove from list
				listOfSimulations.remove(simRep);
				// close any window opened on desktop
				JDesktopPane deskPane = mainFrame.getDesktopPane();
				if (deskPane!=null) {
					JInternalFrame[] intFrames=deskPane.getAllFrames();
					String begFrameTitle =language.getString("labels","allSystems")+" - "+
						simRep.getRepositoryName()+" - ";
					for (int k=0;k<intFrames.length;++k) {
						JInternalFrame intframe = intFrames[k];
						if (intframe.getTitle().startsWith(begFrameTitle)) {
							intframe.dispose();
							break;
						}
					} // end for
				}// end if deskPane 
				
				// set simRep to null
				simRep = null;
			} // end if simRep != null
		}// end if what=preferences
	}// end function


	void saveSomething (String what) {
		if ((what.compareTo("preferences"))==0) {
			// saving preferences: show modal dialog with list of simulations and when simulation chosen
			// open the Repository Handler
			ListOfSimulations listOfSimulations = ListOfSimulations.getInstance();
			SimulationRepository simRep = null;
			int n = listOfSimulations.size();
			if (n == 0) {
				JOptionPane.showMessageDialog(mainFrame,language.getString("messages","simulatePreferencesFirst"));
				simRep = null;
			} else if (n == 1) {
				simRep = (SimulationRepository)listOfSimulations.getFirst();
			} else {
				Object[] choiceOfRepositories = new Object[n];
				for (int i = 0 ; i < n ; ++i) {
					SimulationRepository sr = (SimulationRepository)listOfSimulations.get(i);
					String repNameAndDesc = sr.getRepositoryName();
					String desc = sr.getRepositoryDescription();
					if (desc.compareTo("") !=0) {
						repNameAndDesc += " (" + desc + ")";
					}
					choiceOfRepositories[i] = repNameAndDesc;
				}// end for
				Object obj = JOptionPane.showInputDialog (mainFrame,
						language.getString("messages","chooseRepositorySaving"),
						language.getString("messages","saving"),
						JOptionPane.QUESTION_MESSAGE,
						null,
						choiceOfRepositories,
						null);
				if (obj != null) {
					Iterator  i = listOfSimulations.iterator();
					while (i.hasNext()) {
						SimulationRepository sr = (SimulationRepository)i.next();
						String repNameAndDesc = sr.getRepositoryName();
						String desc = sr.getRepositoryDescription();
						if (desc.compareTo("") !=0) {
							repNameAndDesc += " (" + desc + ")";
						}
						if (repNameAndDesc.compareTo((String)obj) == 0) {
							simRep = sr;
							break;
						}// end if
					}// end while
				}// end if obj
			}// end if n (size of listOfSimulations)
			// now open the fileChooser for the repository
			if (simRep!=null) {
				repHandler.save(simRep,false); // false: I want it reopenable, not sorted
			}
		}// end if what = preferences
		else if (what.compareTo("parliament") ==0) {
System.out.println("saving a parliament");
			// check wether object parliament exists
			// if so save it, otherwise, show all the dialogs to choose it
			if (parliament != null) {
//				// dialog about saving simulation data
//				int n = JOptionPane.showOptionDialog(mainFrame,
//						language.getString("messages","saveSimulationAndParliament"),
//						language.getString("messages","saving"),
//						JOptionPane.YES_NO_OPTION,
//						JOptionPane.QUESTION_MESSAGE,
//						null,null,null);
//				saveSim = (n == JOptionPane.YES_OPTION)? true : false;
				saveSim = true;
				// repHandler save(simulationRepository,parliament,true or false)
				LinkedList <String> key = new LinkedList<String>();
				key.add(parliament.getParliamentKey());
System.out.println("saving simulation "+saveSim+" for parliament "+parliament.getParliamentKey());
				repHandler.save(parliament.getSimulationRepository(),key,saveSim);
				
			} else { // parliament null
				// choose repository
				ListOfSimulations listOfSimulations = ListOfSimulations.getInstance();
				SimulationRepository simRep = null;
				int n = listOfSimulations.size();
				if (n == 0) {
					JOptionPane.showMessageDialog(mainFrame,language.getString("messages","noParliamentToSave"));
					simRep = null;
				} else if (n == 1) {
					simRep = (SimulationRepository)listOfSimulations.getFirst();
				} else {
					Object[] choiceOfRepositories = new Object[n];
					for (int i = 0 ; i < n ; ++i) {
						SimulationRepository sr = (SimulationRepository)listOfSimulations.get(i);
						String repNameAndDesc = sr.getRepositoryName();
						String desc = sr.getRepositoryDescription();
						if (desc.compareTo("") !=0) {
							repNameAndDesc += " (" + desc + ")";
						}
						choiceOfRepositories[i] = repNameAndDesc;
					}// end for
					Object obj = JOptionPane.showInputDialog (mainFrame,
							language.getString("messages","chooseRepositorySaving"),
							language.getString("messages","saving"),
							JOptionPane.QUESTION_MESSAGE,
							null,
							choiceOfRepositories,
							null);
					if (obj != null) {
						Iterator i = listOfSimulations.iterator();
						while (i.hasNext()) {
							SimulationRepository sr = (SimulationRepository)i.next();
							String repNameAndDesc = sr.getRepositoryName();
							String desc = sr.getRepositoryDescription();
							if (desc.compareTo("") !=0) {
								repNameAndDesc += " (" + desc + ")";
							}
							if (repNameAndDesc.compareTo((String)obj) == 0) {
								simRep = sr;
								break;
							}// end if
						}// end while
					}// end if obj
				}// end if n (size of listOfSimulations)
				// choose parliament, or all
				// repHandler save(simulationRepository,parliament or "all",true or false);
				// now open the fileChooser for the repository
				if (simRep != null) {

					// create panel containing checkboxes for all the parliaments saved in repository
					final HashMap <String,Parliament> parliaments = simRep.getParliaments();
					final LinkedList <String> parliamentsToSave = new LinkedList<String>();
					JPanel dialogContents = new JPanel();
					dialogContents.setLayout(new BoxLayout(dialogContents,BoxLayout.Y_AXIS));
					dialogContents.add(new JLabel(language.getString("messages","chooseParliament")));

					// add a box to choose all parliaments
					final JCheckBox allBox = new JCheckBox( new AbstractAction(language.getString("messages","allParliaments")) {
						public void actionPerformed(ActionEvent e) {
							JCheckBox cb = (JCheckBox) e.getSource();
							boolean isSelected = cb.isSelected();
							if (isSelected == true) {
								parliamentsToSave.addAll(parliaments.keySet());
							}
						}
					});
					dialogContents.add(allBox);

					dialogContents.add(new JLabel("--------------"));

					LinkedList <String> keys = new LinkedList <String> (parliaments.keySet());
					for (int k =0 ; k < keys.size() ; ++k) {
						final String key = keys.get(k);
						final JCheckBox ck = new JCheckBox( new AbstractAction(key) {
							public void actionPerformed(ActionEvent e) {
								JCheckBox cb = (JCheckBox)e.getSource();
								boolean isSelected = cb.isSelected();
System.out.println("parliamentsToSave: \n"+parliamentsToSave.toString()+"\nkey: "+key);
System.out.println("key contained? "+parliamentsToSave.contains(key));
								if (isSelected == true) {
									if (!parliamentsToSave.contains(key)) {
										parliamentsToSave.add(key);
System.out.println("key added");
									}
								} else {
									if (parliamentsToSave.contains(key)) {
										parliamentsToSave.remove(key);
System.out.println("key removed");
									}
								}
							}// end of actionPerforned
						});// end of creation of check box
						dialogContents.add(ck);
						
					}

					dialogContents.add(new JLabel("--------------"));
					
//					// add a box to add simulationData
//					JCheckBox simBox = new JCheckBox( new AbstractAction(language.getString("messages","saveSimulationAndParliament")) {
//						public void actionPerformed(ActionEvent e) {
//							JCheckBox cb = (JCheckBox) e.getSource();
//							boolean isSelected = cb.isSelected();
//							setSaveSim(isSelected);
//						}
//					});
//					dialogContents.add(simBox);
					// now create dialog
					final JDialog dialog = new JDialog (mainFrame,language.getString("messages","saving"),true);
					// button
					JButton button = new JButton( new AbstractAction(language.getString("labels","ok")) {
						public void actionPerformed(ActionEvent e) {
							dialog.dispose();
						}
					}) ;
					dialogContents.add(button);
					dialog.getContentPane().add(dialogContents);
					dialog.pack();
					dialog.setLocationRelativeTo(mainFrame);
					dialog.setVisible(true);
					setSaveSim(true);
					repHandler.save(simRep,parliamentsToSave,saveSim);
				}
			}
		} // end if what = parliament
	}
	void setSaveSim (boolean val) {
		saveSim=val;
	}
	

	void simulateSomething (String what) {
	
		if ((what.compareTo("preferences"))==0) {
//System.out.println("create general parameters");
			ParametersGeneral generalParam = new ParametersGeneral (mainFrame);
//System.out.println("setup general parameters and create panel");
			JPanel generalParametersPanel = generalParam.setupGeneralParameters(new HashMap<String,Object>());
//System.out.println("obtain content pane");
			JScrollableDesktopPane desktop=mainFrame.getScrollableDesktop();//(JScrollableDesktopPane)mainFrame.getContentPane();
//System.out.println("display panel in desktop");
			desktop.add(language.getString("labels","generalParameters"),generalParametersPanel);
		} else {
			JOptionPane.showMessageDialog(mainFrame.getContentPane(),"simulating "+what);
		}
	}

	void editSomething (String what) {
		JOptionPane.showMessageDialog(mainFrame.getContentPane(),"editing "+what);
	}

	void changeLanguage(String what) {
System.out.println("what "+what);
		if (what.compareTo("setDefault")==0) {
			// set up current language as default:
			// write the language code in a file, and overwrite it. Instead of loading currentLocale in Language, first check if
			// a default file exists and if so, loads the locale in it.
			try {
				FileWriter fw = new FileWriter("languages/default.lang",false);
				String lang = language.getCurrentLanguage();
				fw.write(lang);
				fw.close();
			} catch (IOException e) {
			}
		} else {
			// in language, set currentLocale to the locale corresponding to the extension
			language.setCurrentLocale(new Locale(what));
			// redraw everything!
		}
	 }

	 void showSomething(String what) {
	 	JOptionPane.showMessageDialog(mainFrame.getContentPane(),"show: "+what);
	 }
}	