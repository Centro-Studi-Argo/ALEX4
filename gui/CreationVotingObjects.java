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

import globals.*;
import classesWrittenByOthers.*;
import votingObjects.*;
import actions.*;
import com.tomtessier.scrollabledesktop.*;

public class CreationVotingObjects {
	MainFrame mainFrame;
	Language language = Language.getInstance();
	final JProgressBar progressBar;
	HashMap <String,Object> generalParameters = new HashMap <String,Object>();
	ArrayList <Party>arrayOfParties = new ArrayList<Party>();
	private classesWrittenByOthers.SwingWorker worker;
	LinkedList <String> what = new LinkedList<String>();
	SimulationRepository simRep;

	public CreationVotingObjects (HashMap <String,Object>generalParameters,ArrayList <Party>parties,LinkedList <String>w,SimulationRepository sr) {
		super();
		mainFrame = MainFrame.getInstance();
		this.generalParameters=generalParameters;
		arrayOfParties=parties;
		what = w;
		simRep = sr;

		progressBar=new JProgressBar();
		progressBar.setStringPainted(true);
	}

	public void showProgressInCreation() {

		final JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();//(JScrollableDesktopPane) mainFrame.getContentPane();
		final CreatorVotingObjects creatorVotingObjects=new CreatorVotingObjects(generalParameters,arrayOfParties,progressBar,simRep);
		
		final JPanel panel = new JPanel();
		String nameOfFrame = new String();
		final JInternalFrame progressFrame = new BaseInternalFrame(language.getString("labels","creationVotingObjects"));
		
		worker=new classesWrittenByOthers.SwingWorker() {
			public Object construct() {
//				LinkedList what = new LinkedList();
//				what.add("all");
				return creatorVotingObjects.createVotingObjects(what);
			}
			public void finished() {
//System.out.println("\n-----------------------\nprint finished");
				progressFrame.dispose();
				String returned = get().toString();
//System.out.println("returned "+returned); 
				if (returned.compareTo("interrupted")==0) {
					JOptionPane.showMessageDialog(mainFrame,language.getString("labels","interrupted"));
				} else if (returned.compareTo("done")==0) {

					ListOfSimulations listOfSimulations = ListOfSimulations.getInstance();

					// need to check whether a new repository needs to be created.
					// a new repository is created when the value of simRep is null
					// if repository exists, then nothing needs to be done as the objects in the repository already exist and
					// contain all that is necessary

					if (simRep == null) {
						String name = "";
						while ((name!=null)&&(name.length()<4)) {
							name = JOptionPane.showInputDialog(
								mainFrame,
								language.getString("labels","allObjectsCreated")+"\n"+language.getString("labels","addToSimulationMenu"),
								language.getString("labels","defaultStartOfName")+(listOfSimulations.size()+1)
								);
							// check whether name already exists in listOfSimulations
							// if so, ask whether to overwrite the simulation with same name, or to give another name to
							// the simulation.
							// if not, just create the repository.
							Iterator i = listOfSimulations.iterator();
							while (i.hasNext()) {
								SimulationRepository sr = (SimulationRepository)i.next();
								if (sr.getRepositoryName().compareTo(name)==0) {
									int n=JOptionPane.showConfirmDialog(
											mainFrame,
											language.getString("labels","repositoryNameAlreadyExists"),
											null,
											JOptionPane.YES_NO_OPTION,
											JOptionPane.WARNING_MESSAGE);
									if (n==JOptionPane.YES_OPTION) {
										// remove from list
										i.remove();
										// if a window (list of parliaments) is opened, dispose of it
										JDesktopPane deskPane = mainFrame.getDesktopPane();
										if (deskPane!=null) {
											JInternalFrame[] intFrames=deskPane.getAllFrames();
											String begFrameTitle =language.getString("labels","allSystems")+" - "+
												sr.getRepositoryName()+" - ";
											for (int k=0;k<intFrames.length;++k) {
												JInternalFrame intframe = intFrames[k];
												if (intframe.getTitle().startsWith(begFrameTitle)) {
													intframe.dispose();
													break;
												}
											} // end for
										}// end if deskPane 
									} // end if n (option pane result)
									else { // make sure the option pane is displayed again
										name = null;
									}
								}// end if compare name of repository with existing names in list
							}// end of while i hasNext
						}// end of while name
						String description = (String)JOptionPane.showInputDialog(
								mainFrame,
								language.getString("labels","descrSimulation"),
								""
								);
	
								
						SimulationRepository simRep = new SimulationRepository(
								generalParameters,
								arrayOfParties,
								creatorVotingObjects.getListOfCandidates(),
								creatorVotingObjects.getListOfVoters(),
								creatorVotingObjects.getListOfUninominalDistricts(),
								creatorVotingObjects.getListOfPlurinominalDistricts(),
								name,
								description
								);
						listOfSimulations.add(simRep);
						
						ListOfParliaments listOfParliaments = new ListOfParliaments(simRep);
						listOfParliaments.showListOfParliaments();
					} else {
						JOptionPane.showMessageDialog(mainFrame,language.getString("messages","interruptError"));
					}
				} // end of if simRep = null
			}// end of if return = done
		};
		worker.start();

		Action interruptAction = new AbstractAction(language.getString("labels","cancelSimulation")) {
			public void actionPerformed (ActionEvent e) {
				try {
					if (waitForUserConfirmation()) {
System.out.println("\n------------------------\ninterrupted clicked");
						worker.interrupt();
System.out.println("\n------------------------\nworker interrupted");
					}
				} catch (InterruptedException er) {
System.out.println("not interrupted, already done");
				}
			}
		};		

		
		panel.add(new JLabel(language.getString("labels","creationVotingObjects")));
		
		panel.add(progressBar);
		JButton button=new JButton(interruptAction);

		panel.add(button);
		progressFrame.getContentPane().add(panel);
		progressFrame.pack();
		progressFrame.setVisible(true);
		desktop.add(progressFrame);
		desktop.setSelectedFrame(progressFrame);

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

		// call with with SwingUtilities.invokeAndWait to stop the thread
//		try {
//			SwingUtilities.invokeAndWait(doShowDialog);
//		} catch (java.lang.reflect.InvocationTargetException e) {
//			e.printStackTrace();
//		}

		return doShowDialog.proceedConfirmed;
	}// end waitForUserConfirmation method
}


