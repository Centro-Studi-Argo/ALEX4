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
package inAndOut;

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

import classesWrittenByOthers.*;
import globals.*;
import votingObjects.*;
import parliaments.*;
import gui.*;
import actions.*;
import com.tomtessier.scrollabledesktop.*;

public class RepositoryDataHandler {

	ListOfSimulations listOfSimulations = ListOfSimulations.getInstance();
	MainFrame mainFrame = MainFrame.getInstance();
	Language language = Language.getInstance();
	String defaultDirectory = "savedSimulations";
	String returnCode = "done";

	boolean withinSimulation = true;

	// constructor
	public RepositoryDataHandler() {
		super();
	}



	public boolean save(SimulationRepository rep,boolean sorted) {
		// find the file in which to save: new file, or overwriting existing
		// default directory = savedSimulations
		Object[] arg = new Object[1];
		JFileChooser fc = new JFileChooser(defaultDirectory);
		int result = fc.showSaveDialog(mainFrame);
		if (result == JFileChooser.CANCEL_OPTION) {
			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","savingAbbandonned"));
			return true;
		} else if (result == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file.exists()) {
				arg[0] = file.getName();
				int response = JOptionPane.showConfirmDialog(mainFrame,
					MessageFormat.format(language.getString("messages","fileAlreadyExists"),arg)+"\n"+language.getString("messages","overwriteFile"),
					language.getString("messages","warning"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.CANCEL_OPTION) {
					return false;
				}
			}// end if file_exists
			try {
//System.out.println("full path "+file.getAbsoluteFile());
				FileWriter out = new FileWriter(file.getAbsoluteFile(),false); // false because we do not append to the file
				if (sorted == true) {
					out.write(rep.toStringSorted());
				} else {
					out.write(rep.toString());
				}
				out.close();
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","doneSaving"),arg));
				return true;
			} catch (FileNotFoundException ex) {
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","fileNotFound"),arg));
				return false;
			} catch (IOException ex) {
				ex.printStackTrace();
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","couldNotWriteOnFile"),arg));
				return false;
			}
		}// end if/else on result
		return false;
	}

	public boolean save (SimulationRepository sr,LinkedList <String> whichParl,boolean simData) {
		// which parl can be a list of parliaments or a single parliament
		// if simData is not null, get repository from parliament
		// find the file in which to save: new file, or overwriting existing
		// default directory = savedSimulations
		Object[] arg = new Object[1];
		JFileChooser fc = new JFileChooser(defaultDirectory);
		int result = fc.showSaveDialog(mainFrame);
		if (result == JFileChooser.CANCEL_OPTION) {
			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","savingAbbandonned"));
			return true;
		} else if (result == JFileChooser.APPROVE_OPTION) {
			boolean append = false;
			File file = fc.getSelectedFile();
//System.out.println("file name "+file.getName());
			if (file.exists()) {
				Object[] options = {language.getString("messages","appendFile"),language.getString("messages","overwriteFile")};
				arg[0] = file.getName();
				int response = JOptionPane.showOptionDialog(mainFrame,
					MessageFormat.format(language.getString("messages","fileAlreadyExists"),arg),
					language.getString("messages","warning"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]
					);
				if (response == JOptionPane.YES_OPTION) {
					append=true;
				}
			}// end if file_exists
			try {
//System.out.println("entered try");
				FileWriter out = new FileWriter(file.getAbsoluteFile(),append);
//System.out.println ("gotten fileWriter "+out);
				HashMap <String,Parliament> parliaments = sr.getParliaments();
//System.out.println("DESCRIPTION OF PARLIAMENTS");
//Set keys = parliaments.keySet();
//Iterator k = keys.iterator();
//while (k.hasNext()) {
//	String pk = (String)k.next();
//System.out.println(pk+" - "+((Parliament)parliaments.get(pk)).getParliamentName());
//}
//System.out.println("what is in whichParl");
//for (int i=0; i<whichParl.size(); ++i) {
//System.out.println("parliament "+(String)whichParl.get(i));
//}
				for (int i = 0; i < whichParl.size() ; ++i) {
					Parliament parliament = parliaments.get(whichParl.get(i));
//System.out.println("found parliament in simulation "+parliament.getParliamentName());
//					if ((simData == true) && (i == 0)) {
						out.write(parliament.toString(sr));
//					} else {
//						out.write(parliament.toString(null));
//					}
				}
				out.close();
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","doneSaving"),arg));
				return true;
			} catch (FileNotFoundException ex) {
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","fileNotFound"),arg));
				return false;
			} catch (IOException ex) {
				ex.printStackTrace();
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","couldNotWriteOnFile"),arg));
				return false;
			}
		}// end if/else on result
		return false;
	}
	
	public String load() {
		// find the file in which to save: new file, or overwriting existing.
		// default directory = savedSimulations
//System.out.println("defaultdirectory: "+defaultDirectory);
		JFileChooser fc = new JFileChooser(defaultDirectory);
		File file = null;
		Object[] arg = new Object[1];
		int n = fc.showOpenDialog(mainFrame);
		if (n == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
		} else {
			file = null;
		}
//		while (n != JFileChooser.APPROVE_OPTION) {
//			n = fc.showOpenDialog(mainFrame);
//			file = fc.getSelectedFile();
//		}
		// load each row of a file into a map
		HashMap <String,String> dataFromFile = new HashMap<String,String>();
		String s = new String();
		if (file != null) {
			try {
				BufferedReader bf = new BufferedReader(new FileReader(file.getAbsoluteFile()));
				while ((s=bf.readLine())!=null) {
					String[] st = s.split(":");
					if (st.length>1) {
						dataFromFile.put(st[0].trim(),st[1].trim());
					}
				}
				bf.close();
				
			} catch (FileNotFoundException ex) {
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","fileNotFound"),arg));
				returnCode = "interrupted";
			} catch (IOException ex) {
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","couldNotLoadFromFile"),arg));
				returnCode = "interrupted";
			} catch (StackOverflowError e) {
				e.printStackTrace();
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","stackOverflow"),arg));
				returnCode = "interrupted";
			} catch (OutOfMemoryError e) {
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","outOfMemory"),arg));
				returnCode = "interrupted";
			}
//System.out.println("dataFromFile");
//Set keys = dataFromFile.keySet();
//Iterator k = keys.iterator();
//while (k.hasNext()) {
//String key = (String)k.next();
//System.out.println("key: "+key+" - value: "+dataFromFile.get(key));
//}
			JProgressBar progressBar = MenuElectoralSystemsActions.getProgressBar();
			progressBar.setMaximum(4);

			// create the repository from the map (or what can be created from it)
			// check contents of possible repository and decide whether the data are valid, can
			// be created from the furnished information
			// if so, create them,
			// if not, refuse the repository, error message
			try {
//	System.out.println("data from file created, size of map is :"+dataFromFile.size());
				Thread.sleep(0);
				if (mainFrame.checkMemory()!= null){
					returnCode = "interrupted";
					return returnCode;
				}
				MenuElectoralSystemsActions.setDisplayInProgressPanel(language.getString("messages","nameOfRepository"));
				// first thing first:
				// name and possibly a description for the repository
				// if not, get the next default name based on the size of listOfSimulations
				SimulationRepository sr=new SimulationRepository();
				String nameR = (dataFromFile.containsKey("nameRepository")) ? (String)dataFromFile.get("nameRepository") : "sim"+(new Integer(listOfSimulations.size())).toString();
				String descR = (dataFromFile.containsKey("descriptionRepository")) ? (String)dataFromFile.get("descriptionRepository") : "";

				// check whether name already exists in listOfSimulations
				// if so, ask whether to overwrite the simulation with same name, or to give another name to
				// the simulation.
				// if not, just create the repository.
				Iterator <SimulationRepository> it = listOfSimulations.iterator();
				while (it.hasNext()) {
					SimulationRepository sry = it.next();
					while (sry.getRepositoryName().compareTo(nameR)==0) {
						String newNameR = JOptionPane.showInputDialog(
							mainFrame,
							language.getString("labels","changeNameOfRepository")+"\n"+language.getString("labels","addToSimulationMenu"),
							nameR
							);
						nameR = (newNameR!=null) ? newNameR : nameR;
					}
				}// end of while i hasNext
		
		
				
				sr.setName(nameR,descR);

				progressBar.setValue(1);
				Thread.sleep(0);
				if (mainFrame.checkMemory()!= null){
					sr = null;
					returnCode = "interrupted";
					return returnCode;
				}
				MenuElectoralSystemsActions.setDisplayInProgressPanel(language.getString("messages","loadGeneralParameters"));
				
				// then: generalParameters
				// needed are: probPreferred, probSecond, probFirst, numberParties, numberUninominalDistricts, numberVoters
				// totalNumberVoters can be recreated if not present (if present, check that it is consistent with numberVoters and numberUninominalDistricts
				// one of numberCandidates or numberPlurinominalDistricts must be present, if one not present, obtain the other, if
				// both present, check for consistency. Also consistency of probabilities, numberParties....
				StringBuffer missingValues = new StringBuffer();
				StringBuffer checkValues = new StringBuffer();
				Double probPreferred = new Double(0);
				Double probSecond = new Double(0);
				Double probFirst = new Double (0);
				Integer numberParties = new Integer(0);
				Integer numberUninominalDistricts = new Integer(0);
				Integer numberVoters = new Integer(0);
				Integer totalNumberVoters = new Integer(0);
				Integer numberCandidates = new Integer(0);
				Integer numberPlurinominalDistricts = new Integer(0);
				HashMap <String,Object> gp = new HashMap<String,Object>();
				Object[] args = new Object[1];
				if (dataFromFile.containsKey("probPreferred")) {
					probPreferred  = new Double((String)dataFromFile.get("probPreferred"));
					dataFromFile.remove("probPreferred");
					gp.put("probPreferred",probPreferred);
					if ((probPreferred.doubleValue()>1)||(probPreferred.doubleValue()<0)) {
						args[0]="probPreferred";
						checkValues.append(MessageFormat.format(language.getString("labels","rangeProbabilites"),args)+"\n");
					}
				} else {
					missingValues.append("probPreferred\n");
				}
				if (dataFromFile.containsKey("probSecond")) {
					probSecond  = new Double((String)dataFromFile.get("probSecond"));
					dataFromFile.remove("probSecond");
					gp.put("probSecond",probSecond);
					if ((probSecond.doubleValue()>1)||(probSecond.doubleValue()<0)) {
						args[0]="probSecond";
						checkValues.append(MessageFormat.format(language.getString("labels","rangeProbabilites"),args)+"\n");
					}
				} else {
					missingValues.append("probSecond\n");
				}
				if (dataFromFile.containsKey("probFirst")) {
					probFirst  = new Double((String)dataFromFile.get("probFirst"));
					dataFromFile.remove("probFirst");
					gp.put("probFirst",probFirst);
					if ((probFirst.doubleValue()>1)||(probFirst.doubleValue()<0)) {
						args[0]="probFirst";
						checkValues.append(MessageFormat.format(language.getString("labels","rangeProbabilites"),args)+"\n");
					}
					if (probFirst.doubleValue()+probSecond.doubleValue()>1) {
						args[0]="probFirst+probSecond";
						checkValues.append(MessageFormat.format(language.getString("labels","rangeProbabilites"),args)+"\n");
					}
				} else {
					missingValues.append("probFirst\n");
				}
				if (dataFromFile.containsKey("numberParties")) {
					numberParties  = new Integer((String)dataFromFile.get("numberParties"));
					dataFromFile.remove("numberParties");
					gp.put("numberParties",numberParties);
					if (numberParties.intValue()<=0) {
						checkValues.append(language.getString("labels","negativeParties")+"\n");
					}
				} else {
					missingValues.append("numberParties\n");
				}
				if (dataFromFile.containsKey("numberUninominalDistricts")) {
					numberUninominalDistricts  = new Integer((String)dataFromFile.get("numberUninominalDistricts"));
					dataFromFile.remove("numberUninominalDistricts");
					gp.put("numberUninominalDistricts",numberUninominalDistricts);
					if (numberUninominalDistricts.intValue()<=0) {
						checkValues.append(language.getString("labels","negativeDistricts")+"\n");
					}
				} else {
					missingValues.append("numberUninominalDistricts\n");
				}
				if (dataFromFile.containsKey("numberVoters")) {
					numberVoters  = new Integer((String)dataFromFile.get("numberVoters"));
					dataFromFile.remove("numberVoters");
					gp.put("numberVoters",numberVoters);
					gp.put("totalNumberVoters",new Integer(numberVoters.intValue()*numberUninominalDistricts.intValue()));
					if (numberVoters.intValue()<=0) {
						checkValues.append(language.getString("labels","negativeVoters")+"\n");
					}
				} else if (dataFromFile.containsKey("totalNumberVoters")) {
					totalNumberVoters = new Integer((String)dataFromFile.get("totalNumberVoters"));
					dataFromFile.remove("totalNumberVoters");
					gp.put("totalNumberVoters",totalNumberVoters);
					int nbVoters = totalNumberVoters.intValue()/numberUninominalDistricts.intValue();
					if ((nbVoters*numberUninominalDistricts.intValue())!=totalNumberVoters.intValue()) {
						checkValues.append(language.getString("labels","messageNumberVoters")+"\n");
					}
					gp.put("numberVoters",new Integer(nbVoters));
				} else {
					missingValues.append("numberVoters\ntotalNumberVoters\n");
				}
				if (dataFromFile.containsKey("numberCandidates")) {
					numberCandidates  = new Integer((String)dataFromFile.get("numberCandidates"));
					dataFromFile.remove("numberCandidates");
					gp.put("numberCandidates",numberCandidates);
					if (numberCandidates.intValue()<=0) {
						checkValues.append(language.getString("labels","negativeCandidates")+"\n");
					}
					int nbDistrictsP = numberUninominalDistricts.intValue()/numberCandidates.intValue();
	
					if ((nbDistrictsP*numberCandidates.intValue())!=numberUninominalDistricts.intValue()) {
						checkValues.append(language.getString("labels","messageNumberCandidates")+"\n");
					} else {
						numberPlurinominalDistricts = new Integer(nbDistrictsP);
						gp.put("numberPlurinominalDistricts",numberPlurinominalDistricts);
					}
				} else if (dataFromFile.containsKey("numberPlurinominalDistricts")) {
					numberPlurinominalDistricts = new Integer((String)dataFromFile.get("numberPlurinominalDistricts"));
					dataFromFile.remove("numberPlurinominalDistricts");
					gp.put("numberPlurinominalDistricts",numberPlurinominalDistricts);
					int nbCand = numberUninominalDistricts.intValue()/numberPlurinominalDistricts.intValue();
					if ((nbCand*numberPlurinominalDistricts.intValue())!=numberUninominalDistricts.intValue()) {
						checkValues.append(language.getString("labels","messageNumberCandidates")+"\n");
					} else {
						gp.put("numberCandidates",new Integer(nbCand));
					}
				} else {
					missingValues.append("numberCandidates\nnumberPlurinominalDistricts\n");
				}
				if (missingValues.length()>0) {
					JTextArea textArea = new JTextArea(language.getString("messages","fileContent")+missingValues.toString()+language.getString("messages","loadingInterrupted"),30,30);
					JScrollPane scrollPane = new JScrollPane(textArea);
			        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			        scrollPane.setPreferredSize(new Dimension(800, 300));
					JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
					JPanel panel = new JPanel();
					panel.add(scrollPane);
					desktop.add(language.getString("messages","errorFileContent"), panel);

					sr = null;
					returnCode = "interrupted";
				} else {
					// if there are some values which are not correct, display message and stop loading
					if (checkValues.length()>0) {
						JTextArea textArea = new JTextArea(language.getString("messages","fileContent")+checkValues.toString()+language.getString("messages","loadingInterrupted"),30,30);
						JScrollPane scrollPane = new JScrollPane(textArea);
						scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
						scrollPane.setPreferredSize(new Dimension(800, 300));
						JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
						JPanel panel = new JPanel();
						panel.add(scrollPane);
						desktop.add(language.getString("messages","errorFileContent"), panel);
						sr = null;
						returnCode = "interrupted";
					} else { // add generalParameters to repository
				progressBar.setValue(2);
				Thread.sleep(0);
				if (mainFrame.checkMemory()!= null){
					sr = null;
					returnCode = "interrupted";
					return returnCode;
				}
				MenuElectoralSystemsActions.setDisplayInProgressPanel(language.getString("messages","loadPartyParameters"));
						sr.setGeneralParameters(gp);
						missingValues = new StringBuffer();
						checkValues = new StringBuffer();
						// are there parties defined in file?
						if (dataFromFile.containsKey("party_1_name")) {
							ArrayList ap = new ArrayList();
							double sumOfShares = 0;
							for (int i=1;i<=numberParties.intValue();++i) {
								String key = "party_"+i+"_";
								Integer name = new Integer(0);
								String nameParty = new String();
								Double share = new Double(0);
								Double distance = new Double(0);
								Boolean major = new Boolean(false);
								Integer concentration = new Integer(0);
								Double coefficient = new Double(0);
								// name (int) and string
								if (dataFromFile.containsKey(key+"name")) {
									name = new Integer(i);
									nameParty = (String)dataFromFile.get(key+"name");
									dataFromFile.remove(key+"name");
								} else {
									missingValues.append(key+"name\n");
								}
								// share (double)
								if (dataFromFile.containsKey(key+"share")) {
									share = new Double((String)dataFromFile.get(key+"share"));
System.out.println("share for party - from file"+name+" = "+share);
									DecimalFormat df = new DecimalFormat("##.##");
									share = new Double(df.format(share.doubleValue()));		
System.out.println("share for party - formatted"+name+" = "+share);
									dataFromFile.remove(key+"share");
									if (share.doubleValue()<0) {
										checkValues.append(key+"share: "+language.getString("labels","negativeShare"));
									}
System.out.println("share for party - double value "+share.doubleValue());
									sumOfShares += share.doubleValue();
System.out.println("sum of shares "+sumOfShares);
								} else {
									missingValues.append(key+"share\n");
								}
								// distance (double)
								if (dataFromFile.containsKey(key+"distance")) {
									distance = new Double((String)dataFromFile.get(key+"distance"));
									dataFromFile.remove(key+"distance");
									if (distance.doubleValue()<0) {
										checkValues.append(key+"distance: "+language.getString("labels","negativeDistance"));
									}
								} else {
									distance = new Double(0);
								}
								// major (boolean)
								if (dataFromFile.containsKey(key+"major")) {
									major = new Boolean((String)dataFromFile.get(key+"major"));
									dataFromFile.remove(key+"major");
								} else {
									missingValues.append(key+"major\n");
								}
								// concentration (int)
								if (dataFromFile.containsKey(key+"concentration")) {
									concentration = new Integer((String)dataFromFile.get(key+"concentration"));
									if (concentration.intValue()<0) {
										checkValues.append(key+"concentration: "+language.getString("labels","negativeConcentration"));
									}
									if (concentration.intValue()>numberUninominalDistricts.intValue()) {
										args = new Object[] {numberUninominalDistricts};
										checkValues.append(key+"concentration: "+MessageFormat.format(language.getString("labels","numberDistrictsConcentration"),args));
									}
								} else {
									missingValues.append(key+"concentration");
								}
								// coefficient (double)
								if (dataFromFile.containsKey(key+"coefficient")) {
									coefficient = new Double((String)dataFromFile.get(key+"coefficient"));
									if (coefficient.doubleValue()<0) {
										checkValues.append(key+"coefficient: "+language.getString("labels","negativeCoefficient"));
									}
									if ((concentration.intValue()==0) && (coefficient.doubleValue()!=1)) {
										checkValues.append(key+"coefficient: "+language.getString("labels","coefficientNoConcentration"));
									}
									if ((concentration.intValue()>0) && (coefficient.doubleValue()<=1)) {
										checkValues.append(key+"coefficient: "+language.getString("labels","coefficientConcentration"));
									}
									double val = (double)100/share.doubleValue();
									if (coefficient.doubleValue()>val) {
										args = new Object[] {new Double(val)};
										checkValues.append(key+"coefficient: "+MessageFormat.format(language.getString("labels","maximumCoefficient"),args));
									}
								} else {
									missingValues.append(key+"coefficient");
								}
								ap.add(new Party(name.intValue(),nameParty,gp,share.doubleValue(),concentration.intValue(),coefficient.doubleValue(),major.booleanValue(),distance.doubleValue()));
							}// end for loop on parties
							// check for sumOfShares
							if (Math.round(sumOfShares)!=100) {
								args = new Object[] {new Double(sumOfShares)};
								checkValues.append(MessageFormat.format(language.getString("labels","generalSumOfShares"),args));
							}
							if (missingValues.length()>0) {
								JTextArea textArea = new JTextArea(language.getString("messages","fileContent")+missingValues.toString()+language.getString("messages","loadingInterrupted"),30,30);
								JScrollPane scrollPane = new JScrollPane(textArea);
						        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
						        scrollPane.setPreferredSize(new Dimension(800, 300));
								JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
								JPanel panel = new JPanel();
								panel.add(scrollPane);
								desktop.add(language.getString("messages","errorFileContent"), panel);
								sr = null;
								returnCode = "interrupted";
							} else {
								// if there are some values which are not correct, display message and stop loading
								if (checkValues.length()>0) {
									JTextArea textArea = new JTextArea(language.getString("messages","fileContent")+checkValues.toString()+language.getString("messages","loadingInterrupted"),30,30);
									JScrollPane scrollPane = new JScrollPane(textArea);
							        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
							        scrollPane.setPreferredSize(new Dimension(800, 300));
									JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
									JPanel panel = new JPanel();
									panel.add(scrollPane);
									desktop.add(language.getString("messages","errorFileContent"), panel);
									sr = null;
									returnCode = "interrupted";
								} else {
									// add arrayOfParties to repository
									sr.setParties(ap);
								}
							}
						}
//	System.out.println("done general parameters");	
						boolean thereAreCandidates = (numberCandidates.intValue()>1);
						// if thereAreCandidates is true, see whether the candidates are defined. Create a list and a map of candidates
						// (list saved in repository, map used to create the preferences of voters for candidates)
						HashMap <String,Candidate> mc = new HashMap<String,Candidate>(); // map of candidates, used to retrieve candidates by their key when creating the voter's preferences for candidates
						LinkedList <Candidate>lk = new LinkedList<Candidate>(); // list of candidates;
						if (dataFromFile.containsKey("candidate1_1")) {
							missingValues = new StringBuffer();
							checkValues = new StringBuffer();
							for (int i=1;i<=numberParties.intValue();++i) {
								for (int j=1;j<=numberCandidates.intValue();++j) {
									String key = "candidate"+i+"_"+j;
									if (dataFromFile.containsKey(key)) {
										String[] vals = ((String)dataFromFile.get(key)).split("_");
										dataFromFile.remove(key);
										Candidate cand = new Candidate(i,j);
										lk.add(cand);
										mc.put(key,cand);
									} else {
										args = new Object[] {new Integer(j),new Integer(i)};
										missingValues.append(key + ": "+ MessageFormat.format(language.getString("labels","missingCandidates"),args)+"\n");
									}
								}
							}
							if (missingValues.length()>0) {
	//System.out.println("missing values for candidates - set repository to null");
	//							JOptionPane.showMessageDialog(mainFrame,
	//								language.getString("messages","fileContent")+checkValues.toString()+language.getString("messages","loadingInterrupted"),
	//								language.getString("messages","errorFileContent"),
	//								JOptionPane.ERROR_MESSAGE);
	//							sr = null;
							} else {
								sr.setCandidates(lk);
							}
						}
		
//	System.out.println("done candidates");
				progressBar.setValue(3);
				Thread.sleep(0);
				if (mainFrame.checkMemory()!= null){
					sr = null;
					returnCode = "interrupted";
					return returnCode;
				}
				MenuElectoralSystemsActions.setDisplayInProgressPanel(language.getString("messages","loadGeneralParameters"));
						// are there voters with preferences for parties, for candidates (if there are candidates)?
						HashMap <Integer,Voter> mv = new HashMap<Integer,Voter>(); // map of voters, created along the list, used to retrieve the voters for creation of districts
						if (dataFromFile.containsKey("voter_1_partyPreferences")) {
							int totVoters = ((Integer)gp.get("totalNumberVoters")).intValue();
				MenuElectoralSystemsActions.setDisplayInProgressPanel(language.getString("messages","loadVoters"));
				progressBar.setValue(0);
				progressBar.setMaximum(totVoters);
							missingValues = new StringBuffer();
							checkValues = new StringBuffer();
							LinkedList <Voter> lv = new LinkedList<Voter>();
							for (int i=1;i<=totVoters;++i) {
				Thread.sleep(0);
				progressBar.setValue(i);
				if (mainFrame.checkMemory()!= null){
					sr = null;
					returnCode = "interrupted";
					return returnCode;
				}
								Integer name = new Integer(0);
								LinkedList <Integer> lp = new LinkedList<Integer>();
								LinkedList <Candidate> lc = new LinkedList<Candidate>();
								String keyParty = "voter_"+i+"_partyPreferences";
								String keyCandidates = "voter_"+i+"_candidatePreferences";
								name = new Integer(i);
								if (dataFromFile.containsKey(keyParty)) {
									String p = (String)dataFromFile.get(keyParty);
									dataFromFile.remove(keyParty);
									p = (p.startsWith("[")) ? p.substring(1) : p;
									p = (p.endsWith("]")) ? p.substring(0,p.length()-1) : p;
									String[] sp = p.split(",");
									if (sp.length < numberParties.intValue()) {
										args = new Object[] {numberParties};
										missingValues.append(keyParty + ": " + MessageFormat.format(language.getString("labels","preferenceOrderParties"),args)+"\n");
									} else {
										for (int j = 0; j<sp.length; ++j) {
											lp.addLast(new Integer((sp[j]).trim()));
										}
									}
									// now candidates
									if (thereAreCandidates) {
	//System.out.println("there are candidates, keyCandidates is "+keyCandidates);
										if (dataFromFile.containsKey(keyCandidates)) {
	//System.out.println("dataFromFile contains keyCandidates");
											// load preferences for candidates
											// if the list of candidates (lc) is empty, the candidates were not defined: find them from the preferences?
											String c = (String)dataFromFile.get(keyCandidates);
											dataFromFile.remove(keyCandidates);
											c = (c.startsWith("[")) ? c.substring(1) : c;
											c = (c.endsWith("]")) ? c.substring(0,c.length()-1) : p;
											String[] sc = c.split(",");
											if (sc.length < numberParties.intValue()*numberCandidates.intValue()) {
												args = new Object[] {numberParties, numberCandidates, new Integer(numberParties.intValue()*numberCandidates.intValue())};
												missingValues.append(keyCandidates + ": " + MessageFormat.format(language.getString("labels","preferenceOrderCandidates"),args)+"\n");
											} else {
												// see whether candidates must be re-created from preferences (lk and mc)
												for (int j=0; j<sc.length; ++j) {
													String[] pp = (sc[j]).split("_");
													Candidate candidate = new Candidate((new Integer((pp[0]).trim())).intValue(),(new Integer((pp[1]).trim())).intValue());
													if (lk.size() < numberParties.intValue()*numberCandidates.intValue()) {
														lk.add(candidate);
													}
													lc.add(candidate);
												}
	//System.out.println("list of candidates = "+lk.toString());
	//System.out.println("prefs for candidates = "+lc.toString());
	//System.out.println("simRep sr ");
	//System.out.println(sr.toString());
												if (sr.getListOfCandidates().size()<((Integer)sr.getGeneralParameters().get("numberCandidates")).intValue()) {
													sr.setCandidates(lk);
												}
											}
											
	//									} else {
	//									missingValues.append(keyCandidates+"\n");
										}
									}
								} else {
									missingValues.append(keyParty+"\n");
								}
								Voter v = new Voter(name.intValue(),lp,lc,gp);
								lv.add(v);
								mv.put(name,v);
							}// end for voters
//	System.out.println("done voters");
	
							
							//show errors if there are some, if not, update sr
							if (missingValues.length()>0) {
	////System.out.println("there are missing values for voters");
	//System.out.println(missingValues.toString());
								JTextArea textArea = new JTextArea(language.getString("messages","fileContent")+missingValues.toString()+language.getString("messages","loadingInterrupted"),30,30);
								JScrollPane scrollPane = new JScrollPane(textArea);
						        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
						        scrollPane.setPreferredSize(new Dimension(800, 300));
								JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
								JPanel panel = new JPanel();
								panel.add(scrollPane);
								desktop.add(language.getString("messages","errorFileContent"), panel);
								sr = null;
								returnCode = "interrupted";
							} else {
								// if there are some values which are not correct, display message and stop loading
								if (checkValues.length()>0) {
	//System.out.println("check values for voters");
									JTextArea textArea = new JTextArea(language.getString("messages","fileContent")+checkValues.toString()+language.getString("messages","loadingInterrupted"),30,30);
									JScrollPane scrollPane = new JScrollPane(textArea);
							        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
							        scrollPane.setPreferredSize(new Dimension(800, 300));
									JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
									JPanel panel = new JPanel();
									panel.add(scrollPane);
									desktop.add(language.getString("messages","errorFileContent"), panel);
									sr = null;
									returnCode = "interrupted";
								} else {
	////System.out.println("add voters to repository");
									// add voters to repository
									if (sr != null) {
										sr.setVoters(lv);
									}
	//System.out.println("done");
								}
							}
						}
						
						// are there uninominal districts?
						HashMap <Integer,DistrictUninominal> mu = new HashMap<Integer,DistrictUninominal>();
						if (dataFromFile.containsKey("districtUninominal_1_nameOfDistrict")) {
//	System.out.println("start plurinominal districts");
				Thread.sleep(0);
				if (mainFrame.checkMemory()!= null){
					sr = null;
					returnCode = "interrupted";
					return returnCode;
				}
				MenuElectoralSystemsActions.setDisplayInProgressPanel(language.getString("messages","loadUninominalDistricts"));
				progressBar.setValue(0);
				progressBar.setMaximum(numberUninominalDistricts.intValue());
							missingValues = new StringBuffer();
							checkValues = new StringBuffer();
							LinkedList <DistrictUninominal>lu = new LinkedList<DistrictUninominal>();
							for (int i = 1 ; i <= numberUninominalDistricts.intValue(); ++i) {
				Thread.sleep(0);
				progressBar.setValue(i);
				if (mainFrame.checkMemory()!= null){
					sr = null;
					returnCode = "interrupted";
					return returnCode;
				}
								String key = "districtUninominal_"+i+"_";
								Integer nameOfDistrict = new Integer(0);
								Boolean concentratedMajorParty = new Boolean(false);
								Integer nameMajorParty = new Integer(0);
								// name
								if (dataFromFile.containsKey(key+"nameOfDistrict")) {
									nameOfDistrict = new Integer((String)dataFromFile.get(key+"nameOfDistrict"));
									dataFromFile.remove(key+"nameOfDistrict");
								} else {
									missingValues.append(key+"nameOfDistrict");
								}
	//System.out.println("done name of district " +nameOfDistrict);
								// concentrated major party
								if (dataFromFile.containsKey(key+"concentratedMajorParty")) {
									concentratedMajorParty = new Boolean((String)dataFromFile.get(key+"concentratedMajorParty"));
									dataFromFile.remove(key+"concentratedMajorParty");
								} else {
									missingValues.append(key+"concentratedMajorParty");
								}
	//System.out.println("done concentrated party");
								// name Major Party
								if (dataFromFile.containsKey(key+"nameMajorParty")) {
									nameMajorParty = new Integer((String)dataFromFile.get(key+"nameMajorParty"));
									dataFromFile.remove(key+"nameMajorParty");
								} else {
									missingValues.append(key+"nameMajorParty");
								}
	////System.out.println("done name concentrated party");
								DistrictUninominal du = new DistrictUninominal(gp,nameOfDistrict.intValue(),concentratedMajorParty.booleanValue(),nameMajorParty.intValue());
								if (dataFromFile.containsKey(key+"listOfVoters")) {
	//System.out.println("start list of voters");							
									String p = (String)dataFromFile.get(key+"listOfVoters");
									dataFromFile.remove(key+"listOfVoters");
									p = (p.startsWith("[")) ? p.substring(1) : p;
									p = (p.endsWith("]")) ? p.substring(0,p.length()-1) : p;
									String[] sp = p.split(",");
									if (sp.length != numberVoters.intValue()) {
										args = new Object[] {numberVoters,new Integer(sp.length)};
										missingValues.append(key+"listOfVoters : " + MessageFormat.format(language.getString("labels","wrongNumberOfVoters"),args)+"\n");
									} else {
										for (int j = 0 ; j < sp.length ; ++j) {
											du.addVoter((Voter)mv.get(new Integer((sp[j]).trim())));
										}
									}
	//System.out.println("done list of voters");
								}
							lu.add(du);
	//System.out.println("add district to list of districts");
							mu.put(nameOfDistrict,du);
	//System.out.println("add district to map of districts");
							}// end loop on uninominal districts
//	System.out.println("done uninominal districts");
							if (missingValues.length()>0) {
								JTextArea textArea = new JTextArea(language.getString("messages","fileContent")+missingValues.toString()+language.getString("messages","loadingInterrupted"));
								JScrollPane scrollPane = new JScrollPane(textArea);
						        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
						        scrollPane.setPreferredSize(new Dimension(800, 300));
								JScrollableDesktopPane desktop = mainFrame.getScrollableDesktop();
								JPanel panel = new JPanel();
								panel.add(scrollPane);
								desktop.add(language.getString("messages","errorFileContent"), panel);
								sr = null;
								returnCode = "interrupted";
							} else {
								if (sr != null) {
									sr.setUninominalDistricts(lu);
								}
							}
						}
		
						// are there plurinominalDistricts?
						if (thereAreCandidates) {
//	System.out.println("there are candidates: do plurinominal districts");
							missingValues = new StringBuffer();
							checkValues = new StringBuffer();
							LinkedList <DistrictPlurinominal>lpr = new LinkedList<DistrictPlurinominal>();
							if (dataFromFile.containsKey("districtPlurinominal_1_name")) {
//	System.out.println("start plurinominal districts");
				MenuElectoralSystemsActions.setDisplayInProgressPanel(language.getString("messages","loadPlurinominalDistricts"));
				progressBar.setValue(0);
				progressBar.setMaximum(numberPlurinominalDistricts.intValue());
								for (int i = 1; i <= numberPlurinominalDistricts.intValue(); ++i ) {
//	System.out.println("i="+i+" out of "+numberPlurinominalDistricts);
				Thread.sleep(0);
				progressBar.setValue(i);
				if (mainFrame.checkMemory()!= null){
					sr = null;
					returnCode = "interrupted";
					return returnCode;
				}
									String key = "districtPlurinominal_"+i+"_";
									Integer nameOfDistrict = new Integer(i);
									LinkedList <DistrictUninominal>listOfDistricts = new LinkedList<DistrictUninominal>();
									// name
									if (dataFromFile.containsKey(key+"name")) {
										nameOfDistrict = new Integer((String)dataFromFile.get(key+"name"));
										dataFromFile.remove(key+"name");
//	System.out.println("done name");
									} else {
										missingValues.append(key+"name");
									}
									// list of districts
									if (dataFromFile.containsKey(key+"listOfDistricts")){
//	System.out.println("start listOfDistricts need to get: "+key+"listOfDistricts");
										String p = (String)dataFromFile.get(key+"listOfDistricts");
										dataFromFile.remove(key+"listOfDistricts");
										p = (p.startsWith("[")) ? p.substring(1) : p;
										p = (p.endsWith("]")) ? p.substring(0,p.length()-1) : p;
										String[] sp = p.split(",");
//	System.out.println("sp has "+sp.length+" elements");
										if (sp.length != numberCandidates.intValue()) {
//	System.out.println("uninominal districts are "+numberUninominalDistricts+" candidates are "+numberCandidates+" not enough!");
											args = new Object[] {numberUninominalDistricts,new Integer(sp.length)};
											missingValues.append(key+"listOfDistricts : " + MessageFormat.format(language.getString("labels","wrongNumberOfVoters"),args)+"\n");
										} else {
//	System.out.println("add to listOfDistricts");
											for (int j = 0 ; j < sp.length ; ++j) {
												listOfDistricts.add((DistrictUninominal)mu.get(new Integer((sp[j]).trim())));
											}
										}
										DistrictPlurinominal districtP = new DistrictPlurinominal(nameOfDistrict.intValue(),listOfDistricts);
										lpr.add(districtP);
									} else {
										missingValues.append(key+"listOfDistricts");
									}
								}
								if (sr != null) {
									sr.setPlurinominalDistricts(lpr);
								}
							}
						} // end of if plurinominal districts
//	System.out.println("done plurinominal districts");
						// are there parliaments
//	System.out.println("at this time, look for parliaments, size of map is :"+dataFromFile.size());
	//					// iterate through remaining keys: there are few of them now
	//					// if a key is found starting with "parliament_" break loop and ask whether
	//					// existing parliaments must be loaded
	//					
	//					Set keys = dataFromFile.keySet();
	//					Iterator k = keys.iterator();
	//					while (k.hasNext()) {
	//						String key = (String)k.next();
	//					}
					}
				}
				
				if (sr != null) {
				progressBar.setValue(0);
				progressBar.setMaximum(1);
					sr.checkRepository();
					boolean repositoryIsComplete = sr.getRepositoryComplete();
					boolean simulationPossible = sr.getSimulationPossible();
//System.out.println("repository is complete: "+repositoryIsComplete);
//System.out.println("simulationPossible: "+simulationPossible);
					if (repositoryIsComplete) {
	//					JOptionPane.showMessageDialog(mainFrame,
	//						language.getString("messages","loadingSuccess"),language.getString("messages","loadingSuccess"),JOptionPane.INFORMATION_MESSAGE);
						listOfSimulations.add(sr);
						if (withinSimulation == true) {
							ListOfParliaments listOfParliaments = new ListOfParliaments(sr);
							listOfParliaments.showListOfParliaments();
						}
					} else if (simulationPossible) {
				MenuElectoralSystemsActions.setDisplayInProgressPanel(language.getString("messages","simulateMissing"));
						String done = sr.simulateMissing();
						if (done.compareTo("OK")==0) {
	//						JOptionPane.showMessageDialog(mainFrame,
	//							language.getString("messages","loadingSuccess"),language.getString("messages","loadingSuccess"),JOptionPane.INFORMATION_MESSAGE);
							listOfSimulations.add(sr);
							if (withinSimulation == true) {
								ListOfParliaments listOfParliaments = new ListOfParliaments(sr);
								listOfParliaments.showListOfParliaments();
							}
		//				} else {
		//					JOptionPane.showMessageDialog(mainFrame,
		//						language.getString("messages","loadingFailure"),language.getString("messages","loadingFailure"),JOptionPane.INFORMATION_MESSAGE);
		//				}
						}
				Thread.sleep(0);
				if (mainFrame.checkMemory()!= null){
					sr = null;
					returnCode = "interrupted";
					return returnCode;
				}
				progressBar.setValue(1);
					} else {
						returnCode = "interrupted";
						JOptionPane.showMessageDialog(mainFrame,
							language.getString("messages","loadingFailure"),language.getString("messages","loadingFailure"),JOptionPane.INFORMATION_MESSAGE);
					}
				}  else {
					String rc = loadParliament();
					if (returnCode.compareTo("OK")!=0) {
						returnCode = "interrupted";
						JOptionPane.showMessageDialog(mainFrame,
							language.getString("messages","loadingFailure"),language.getString("messages","loadingFailure"),JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} // end try
			catch (InterruptedException e) {
				returnCode = "interrupted";
			}
		}// end if file != null
		else {
			returnCode="interrupted";
			JOptionPane.showMessageDialog(mainFrame,
				language.getString("messages","loadingFailure"),language.getString("messages","loadingFailure"),JOptionPane.INFORMATION_MESSAGE);
		}
		return returnCode;
	}

	public void loadOutsideSimulation() {
		withinSimulation = false;
		load();
	}

	public String getReturnCode() {
		return returnCode;
	}

	public String loadParliament() {
		// get file, default directory: savedSimulations
		JFileChooser fc = new JFileChooser(defaultDirectory);
		File file = null;
		Object[] arg = new Object[1];
		int n = fc.showOpenDialog(mainFrame);
		if (n == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
		} else {
			file = null;
		}
		// read contents of the file into a String
		String s = new String();
		if (file != null) {
			try {
				BufferedReader bf = new BufferedReader(new FileReader(file.getAbsoluteFile()));
				HashMap <String,String> dataFromFile = new HashMap<String,String>();
				String st = new String();
				while ((st = bf.readLine())!=null) {
					if (st.length()>1) {
						if (st.compareTo("END PARLIAMENT")==0) {
//System.out.println("found end parliament");
							// create new gui
							GenericParliament parl = new GenericParliament(dataFromFile);
//System.out.println("done creating parliament");
							GUIParliament guiparl = new GUIParliament(parl);
//System.out.println("now reinitialise dataFromFile");
							dataFromFile = new HashMap<String,String>();
//System.out.println("done");
						} else {
//System.out.println("put value in map");
							String[] sts = st.split(":");
							if (sts.length>1) {
								dataFromFile.put(sts[0].trim(),sts[1].trim());
							}
						}
					}
				}
				bf.close();
				returnCode="done";
				
			} catch (FileNotFoundException ex) {
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","fileNotFound"),arg));
				returnCode = "interrupted";
			} catch (IOException ex) {
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","couldNotLoadFromFile"),arg));
				returnCode = "interrupted";
			} catch (StackOverflowError e) {
				e.printStackTrace();
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","stackOverflow"),arg));
				returnCode = "interrupted";
			} catch (OutOfMemoryError e) {
				arg[0] = file.getName();
				JOptionPane.showMessageDialog(mainFrame,MessageFormat.format(language.getString("messages","outOfMemory"),arg));
				returnCode = "interrupted";
			}
		}
		return (returnCode);
	}
}// end class definition