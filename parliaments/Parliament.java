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
package parliaments;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Toolkit;
import java.text.*;
import java.beans.*; // for PropertyChange stuff


import classesWrittenByOthers.*;
import globals.*;
import votingObjects.*;
import gui.*;
import indexes.*;

public abstract class Parliament {// extends Observable {

	// the observable value in this class is the currentGovernment list, which is updated when
	// the user creates the government.

	public SimulationRepository simulationRepository;
	public MainFrame mainFrame;
	public ArrayList <Party> arrayOfParties;
	public LinkedList <Voter> listOfVoters;
	public LinkedList <DistrictUninominal> listOfUninominalDistricts;
	public LinkedList <DistrictPlurinominal> listOfPlurinominalDistricts;
	public LinkedList <Candidate> listOfCandidates;
	public int sizeOfParliament; // equal to the number of uninominal districts
	public int totalNumberVoters; // equal to the size of the listOfVoters;
	public int numberCandidates;
	public double majorityLevel=50;
	String name="";
	HashMap <Integer,Integer> proportionalAllocationOfSeats = new HashMap<Integer,Integer>();
	public HashMap <Integer,Integer> allocationOfSeats = new HashMap<Integer,Integer>();
	public HashMap<Integer,Integer> allocationOfVotes = new HashMap<Integer,Integer>(); // will be not null for VAP (and the one to be drawn)
	protected LinkedList <Integer> currentGovernment = new LinkedList<Integer>();
	protected HashMap <String,LinkedList <String> > governments = new HashMap<String,LinkedList <String> >();
	protected boolean majorityFound = false;
	protected int sumOfGovernmentVotes = 0;
	protected int valueField = 0; // for parameters
	protected double doubleValueField = 0; // for parameters
	private String valueString = ""; // for rounding methods
	protected Double reprIndex = new Double(0);
	public String returnCode = "done";
	public Language language = Language.getInstance();
	public String rounding;
	public HashMap <String,String[]> roundingMethods = new HashMap<String,String[]>();
	public HashMap <String,String> thresholdLevels = new HashMap<String,String>();
	public HashMap <Integer,Integer> votesPerParty = new HashMap<Integer,Integer>();
	public HashMap <Integer,Integer> votesPerPartyRef = new HashMap<Integer,Integer>();


	public Random generator = new Random();

	public String parliamentKey = new String();

	protected EventListenerList listenerList = new EventListenerList();

	public JProgressBar progressBar = new JProgressBar();
	public HashMap <ArrayList<Integer>,Integer> myersonValueFunction = new HashMap<ArrayList<Integer>,Integer>();
	
	// these variables will be added to the respective panel in the gui, if they contain information
	// used to specify the value of the extra parameter (eg rounding function, threshold) or some other
	// information (nb of cycle in condorcet winner system...)
	public String noteToGraph = new String();
	public String noteToComposition = new String();
	public String noteToGovernment = new String();
	public String noteToIndexes = new String();
	static private Parliament _instance = null;
	
	// constructor
	public Parliament() {
	}
	
	public Parliament(SimulationRepository sr) {
		simulationRepository = sr;
		mainFrame = MainFrame.getInstance();
	
		arrayOfParties = simulationRepository.getArrayOfParties();
		listOfVoters = simulationRepository.getListOfVoters();
		listOfUninominalDistricts = simulationRepository.getListOfUninominalDistricts();
		listOfPlurinominalDistricts = simulationRepository.getListOfPlurinominalDistricts();
		listOfCandidates = simulationRepository.getListOfCandidates();
		sizeOfParliament = ((Integer)simulationRepository.getGeneralParameters().get("numberUninominalDistricts")).intValue();
		numberCandidates = ((Integer)simulationRepository.getGeneralParameters().get("numberCandidates")).intValue();
		totalNumberVoters = listOfVoters.size();

		majorityLevel = setParameterDialog(majorityLevel,language.getString("labels","majorityLevel")+" (%)",100);
		
		progressBar = ListOfParliaments.getProgressBar();
		progressBar.setMinimum(0);

		// initialise the rounding methods, the function they will call and their argument
		roundingMethods.put("None",new String[] {"proportionalNoRounding","0"});
		roundingMethods.put("Hare",new String[] {"roundingHareImperiali","0"});
		roundingMethods.put("Imperiali",new String[] {"roundingHareImperiali","2"});
		roundingMethods.put("Sainte Lague",new String[] {"roundingDHondtSteLague","2"});
		roundingMethods.put("D'Hondt",new String[] {"roundingDHondtSteLague","1"});

		thresholdLevels.put(language.getString("labels","nation"),"nation");
		thresholdLevels.put(language.getString("labels","plurinominalDistricts"),"district");

		
//		if (simulationRepository.containsParliament("OneDistrictProportional")) {
//System.out.println("1 - one district proportional exists in repository - loaded");
//			proportionalAllocationOfSeats = simulationRepository.loadParliament("OneDistrictProportional");
//		} else {
//System.out.println("1 - one district proportional does not exist in repository - created");
			proportionalAllocationOfSeats = findProportionalAllocationOfSeats();
//			simulationRepository.saveParliament("OneDistrictProportional",this);
//		}
	}


	

	// abstract method that must be overridden by all parliaments
	public abstract HashMap <Integer,Integer> findAllocationOfSeats();
	public abstract String getParliamentName();
	public abstract String getParliamentKey();

	public String getReturnCode() {
System.out.println("entered get return code");
System.out.println("return code is : "+returnCode);
		return returnCode;
	}

	// methods for events
	public void addParliamentEventListener(ParliamentEventListener listener) {
		listenerList.add(ParliamentEventListener.class,listener);
	}

	public void removeParliamentEventListener(ParliamentEventListener listener) {
		listenerList.remove(ParliamentEventListener.class,listener);
	}

	public void fireParliamentEvent(ParliamentEvent evt) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0 ; i < listeners.length ; i+=2) {
			if (listeners[i] == ParliamentEventListener.class) {
				((ParliamentEventListener)(listeners[i+1])).governmentHasChanged(evt);
			}
		}
	}

	// method for the name that will be saved in files and displayed in title bar of window.
	public String makeKey(String[] parliamentKeyItems) {
		for (int i=0; i<parliamentKeyItems.length;++i) {
			String item = parliamentKeyItems[i];
			if (parliamentKey.length()==0) {
				parliamentKey = item;
			} else {
				parliamentKey += "_"+item;
			}
		}
		return parliamentKey;
	}

	// methods necessary when some parameter is needed by the parliament (mixed member systems, threshold)
	public int setParameterDialog(final int defaultValue,String textToDisplay,final int maxValue) {
		// percentage of proportional and first past the post in this mixed system: joptionpane
		final WholeNumberField field=new WholeNumberField(defaultValue,3);

			// dialog to ask for the value of the parameter
			final JDialog dialog = new JDialog(mainFrame,textToDisplay,true);
			JPanel panel = new JPanel();
			GridBagLayout gridbag=new GridBagLayout();
			GridBagConstraints c=new GridBagConstraints();
			c.fill=GridBagConstraints.HORIZONTAL;
			panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
			panel.setLayout(gridbag);
			c.gridx = 0;
			c.gridy = 0;
			panel.add(new JLabel(textToDisplay),c);
			c.gridy = 1;
			panel.add(field,c);

			Action checkField = new AbstractAction(language.getString("labels","ok")) {
				public void actionPerformed(ActionEvent e) {
System.out.println("in checkfield");
					int newValue = field.getValue();
System.out.println("new value is "+newValue);
					if (newValue > maxValue) {
System.out.println("too big");
						Object options[] = {new Integer(maxValue)};
						JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","maxValue"),options));
						setValueField(defaultValue);
					} else if (newValue < 0) {
System.out.println("too small");
						JOptionPane.showMessageDialog(null,language.getString("labels","minValue"));
						setValueField(defaultValue);
					} else {
System.out.println("OK. set value field");
						setValueField(newValue);
						dialog.setVisible(false); 
					}
				}
			};
			
			c.gridx = 1;
			panel.add(new JButton(checkField),c);

			dialog.getContentPane().add(panel);
			dialog.setLocation(mainFrame.getX()+mainFrame.WIDTH/2,mainFrame.getY()+mainFrame.HEIGHT/2);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

			dialog.pack();
			dialog.setVisible(true);
		return getValueField();
	}
	void setValueField(int value) {
		valueField=value;
System.out.println("value is "+ value+ " so valuefield is "+ valueField);
	}
	int getValueField() {
System.out.println("VALUE IN FIELD: "+valueField);
		return(valueField);
	}


	// methods necessary when some parameter is needed by the parliament (mixed member systems, threshold)
	public double setParameterDialog(final double defaultValue,String textToDisplay,final double maxValue) {
		// percentage of proportional and first past the post in this mixed system: joptionpane
		NumberFormat proportionFormat = NumberFormat.getNumberInstance();
		proportionFormat.setMinimumFractionDigits(0);
		proportionFormat.setMaximumFractionDigits(2);
		final DecimalField field=new DecimalField(defaultValue,3,proportionFormat);

			// dialog to ask for the value of the parameter
			final JDialog dialog = new JDialog(mainFrame,textToDisplay,true);
			JPanel panel = new JPanel();
			GridBagLayout gridbag=new GridBagLayout();
			GridBagConstraints c=new GridBagConstraints();
			c.fill=GridBagConstraints.HORIZONTAL;
			panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
			panel.setLayout(gridbag);
			c.gridx = 0;
			c.gridy = 0;
			panel.add(new JLabel(textToDisplay),c);
			c.gridy = 1;
			panel.add(field,c);

			Action checkField = new AbstractAction(language.getString("labels","ok")) {
				public void actionPerformed(ActionEvent e) {
					double newValue = field.getValue();
					if (newValue > maxValue) {
						Object options[] = {new Double(maxValue)};
						JOptionPane.showMessageDialog(null,MessageFormat.format(language.getString("labels","maxValue"),options));
						setDoubleValueField(defaultValue);
					} else if (newValue < 0) {
						JOptionPane.showMessageDialog(null,language.getString("labels","minValue"));
						setDoubleValueField(defaultValue);
					} else {
						setDoubleValueField(field.getValue());
						dialog.setVisible(false); 
					}
				}
			};
			
			c.gridx = 1;
			panel.add(new JButton(checkField),c);

			dialog.getContentPane().add(panel);
			dialog.setLocation(mainFrame.getX()+mainFrame.WIDTH/2,mainFrame.getY()+mainFrame.HEIGHT/2);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

			dialog.pack();
			dialog.setVisible(true);
		return getDoubleValueField();
	}
	void setDoubleValueField(double value) {
		doubleValueField=value;
	}
	double getDoubleValueField() {
System.out.println("FOR DOUBLES: VALUEFIELD = "+doubleValueField);
		return(doubleValueField);
	}

	

	// method necessary when some parameter is needed by the parliament (mixed member systems, threshold)
	public String setMethodDialog(Set options,String keyToDisplay) {
		Object[] optionsName = options.toArray();
		JPanel panel=new JPanel();
		GridBagLayout gridbag=new GridBagLayout();
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		panel.setLayout(gridbag);
		// text that is diplayed
		c.gridy=0;
		c.gridx=0;
		panel.add(new JLabel(language.getString("messages",keyToDisplay+"_text")),c);
		// button group
//System.out.println("create button group");
		final ButtonGroup buttonGroup=new ButtonGroup();
//System.out.println("label");
		for (int i = 0; i<options.size();++i) {
			c.gridy=i+1;
			// create radio button
			boolean selected = (i==0) ? true : false;
			JRadioButton button=new JRadioButton((String)optionsName[i],selected);
			button.setActionCommand((String)optionsName[i]);
			gridbag.setConstraints(button,c);
			panel.add(button);
			buttonGroup.add(button);
		}
		// dialog to ask for the repartition method
		final JOptionPane optionPane=new JOptionPane(panel,JOptionPane.QUESTION_MESSAGE);
		final JDialog dialog=new JDialog(mainFrame,language.getString("messages",keyToDisplay+"_title"),true);
		dialog.setLocation(mainFrame.getX()+mainFrame.WIDTH/2,mainFrame.getY()+mainFrame.HEIGHT/2);
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String prop=e.getPropertyName();
				if (dialog.isVisible()
					&& (e.getSource()==optionPane)
					&& (prop.equals(JOptionPane.VALUE_PROPERTY) ||
					prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
					String element=buttonGroup.getSelection().getActionCommand();
					setValueString(element);
//System.out.println("element selected "+element);
					dialog.setVisible(false);
				}
			}
		});
		dialog.pack();
		dialog.setVisible(true);
		return getValueString();
	}
	
	void setValueString(String value) {
		valueString=value;
	}
	String getValueString() {
		return(valueString);
	}
	
	// these other methods are common to most parliaments and should not changed (except maybe the creation of indexes)
	protected HashMap <Integer,Integer> findProportionalAllocationOfSeats() {
		HashMap <Integer,Integer> mapOfSeats = new HashMap <Integer,Integer> ();
		HashMap <Integer,Integer> totalVotes = new HashMap <Integer,Integer> ();
		int seatsNotAllocated = sizeOfParliament;
		progressBar.setMaximum(arrayOfParties.size());
		int j = 0;
		try {
			int quota = totalNumberVoters / sizeOfParliament;
			HashMap <Integer,Integer> remainingVotes = new HashMap <Integer,Integer> ();
			// for each party
			Iterator <Party> i = arrayOfParties.iterator();
			while (i.hasNext()) {
				Thread.sleep(0);
				Party party = i.next();
				int nameOfParty = party.getName();
				int totalVotesForParty = findTotalVotesForParty(nameOfParty,listOfVoters);
				totalVotes.put(new Integer(nameOfParty),new Integer(totalVotesForParty));
				// find the number of seats for each party
				// (the last one gets the difference between the total number of seats and the number of allocated seats
				
				int numberSeatsForParty = totalVotesForParty/quota;
				seatsNotAllocated -= numberSeatsForParty;
				int unusedVotes = totalVotesForParty - (numberSeatsForParty * quota);
System.out.println("party "+nameOfParty+" has "+unusedVotes+" unused votes");
				remainingVotes.put(new Integer(nameOfParty), new Integer(unusedVotes));
System.out.println("party "+nameOfParty+"has "+numberSeatsForParty+" seats");
				mapOfSeats.put(new Integer(nameOfParty) , new Integer(numberSeatsForParty));
				progressBar.setValue(j);
				++j;
 			}
			votesPerPartyRef = totalVotes;
			// are there seats not attributed in the parliament?
			if (seatsNotAllocated > 0) {
System.out.println("there are "+seatsNotAllocated+" seats not allocated");
				// create a vector of parties in decreasing order of remaining votes
				LinkedList <Integer> orderedParties = sortMap(remainingVotes,"descending",totalVotes);
				for (int k=0; k<seatsNotAllocated; ++k) {
					Integer party = orderedParties.get(k);
System.out.println("giving an extra vote to party "+party);
					mapOfSeats = updateMap(mapOfSeats,party,1);
				}
			}
		} catch (InterruptedException e) {
			returnCode = "interrupted";
		}
		return mapOfSeats;
	}

	// note: the total votes for party are found with the voters' first preference, rather than the share of votes of
	// the party in the population, because of some rounding that may have taken place when creating the voters
	// (which first preference was decided according to the share of votes of the parties in the population anyways)
	public int findTotalVotesForParty(int nameOfParty,LinkedList <Voter> listOfVoters) {
		int totalVotes = 0;
		// for each voter
		Iterator <Voter> i = listOfVoters.iterator();
		while (i.hasNext()) {
			Voter voter = i.next();
			int preferredParty = voter.getFirstPartyPreference();
			totalVotes += (preferredParty==nameOfParty)? 1: 0;
		}
		return totalVotes;
	}

	public HashMap <Integer,Integer> getProportionalAllocationOfSeats() {
		return proportionalAllocationOfSeats;
	}

	// return allocationOfSeats, unless allocationOfVotes has elements
	public HashMap <Integer,Integer> getAllocationOfSeats() {
			return allocationOfSeats;
	}


	// return allocationOfSeats, unless allocationOfVotes has elements
	public HashMap <Integer,Integer> getAllocationOfVotes() {
			return allocationOfVotes;
	}

	public String getNameDescrSimulation() {
		return ("- "+simulationRepository.getRepositoryName()+" - "+simulationRepository.getRepositoryDescription());
	}

	public int getSizeOfParliament() {
		return sizeOfParliament;
	}

	public int getNumberOfParties() {
		return arrayOfParties.size();
	}

	public int getTotalNumberVoters() {
		return totalNumberVoters;
	}
	
	public SimulationRepository getSimulationRepository() {
		return simulationRepository;
	}

	// update parties in government, set changed and notify the observers
	// if majorityFound is true, save the government in repository?
	public void updateParliament(LinkedList <Integer> government,boolean majority,int sumOfVotes) {
		currentGovernment = government;
		majorityFound = majority;
		sumOfGovernmentVotes = sumOfVotes;
System.out.println("governments"+governments);
		// if a majority is found, add the currentGovernment in the map Governments as key
		// with sumOfgovernmentVotes as values (first element in a list)
		if (majorityFound == true) {
System.out.println("majority is found");
			if (!governments.containsKey(currentGovernment)) {
System.out.println("government "+currentGovernment+" does not exist yet, so save, sumOfGovernmentVotes = "+sumOfGovernmentVotes);
				LinkedList <String> values = new LinkedList <String> ();
				values.add("sumOfGovernmentVotes="+(new Integer(sumOfGovernmentVotes)).toString());
				values.add("RepresentationIndex="+reprIndex.toString());
				governments.put(currentGovernment.toString(),values);
			}
System.out.println("there are "+currentGovernment.size()+" parties in the government, notify changes");
System.out.println("governments (after)"+governments);
		}
		fireParliamentEvent(new ParliamentEvent(this));
//		setChanged();
//		notifyObservers();
	}

	public boolean getMajorityFound() {
		return majorityFound;
	}
	
	public LinkedList <Integer> getPartiesInGovernment() {
		return currentGovernment;
	}

	public void setIndexValue(LinkedList <Integer> government,Double indexValue,String typeOfIndex) {
System.out.println("entered setIndexValue in parliament");
		String key = government.toString();
System.out.println("got key");
		LinkedList <String> values = governments.get(key);
System.out.println("got values");
		if (values == null) {
			values = new LinkedList <String> ();
		}
System.out.println("size of values: "+values.size());
		governments.remove(key);
System.out.println("removed from government");
System.out.println("typeofindex: "+typeOfIndex+" and indexvalue "+indexValue.toString());
System.out.println("size of values: "+values.size());
		values.addLast(typeOfIndex+"="+indexValue.toString());
System.out.println("updated values");
		governments.put(key,values);
System.out.println("updated government");
	}
	
	public void setRepresentationValue(Double indexValue) {
System.out.println("set representation value to "+indexValue);
		reprIndex = indexValue;
	}

	// setters and getters for the notes
	public void setNoteToGraph(String note) {
		if (noteToGraph.length()==0) {
			noteToGraph = "<html>";
		}
System.out.println("entered note to graph, current is "+noteToGraph+"\nadding "+note);
		noteToGraph += note+"<br>";
System.out.println("so the note is "+noteToGraph);
	}
	public void setNoteToComposition(String note) {
		noteToComposition += note+"\n";
	}
	public void setNoteToGovernment(String note) {
		noteToGovernment += note+"\n";
	}
	public void setNoteToIndexes(String note) {
		noteToGraph += note+"\n";
	}
	public String getNoteToGraph() {
		return noteToGraph;
	}
	public String getNoteToComposition() {
		return noteToComposition;
	}
	public String getNoteToGovernment() {
		return noteToGovernment;
	}
	public String getNoteToIndexes() {
		return noteToIndexes;
	}

	public LinkedList <Integer> getCurrentGovernment() {
		return currentGovernment;
	}

	public ArrayList <Party> getArrayOfParties() {
		return arrayOfParties;
	}

	// these functions are used by systems to add elements to the map of seats allocation
	public HashMap <Integer,Integer> updateMap(HashMap <Integer,Integer> map,Integer key,int increment) // increment value if key exists, otherwise, add new entry with val=increment
		{
//System.out.println("entered updateMap");
//System.out.println("unusedVotes (map) has "+map.size()+" elements");
		boolean contains=map.containsKey(key);
//System.out.println("contains = "+contains);
		if (contains==true)
			{
			int value=(map.get(key)).intValue();
			value+=increment;
			map.remove(key);
			map.put(key,new Integer(value));
//System.out.println("already contains the key, increment the value");
			}
		else
			{
			map.put(key,new Integer(increment));
//System.out.println("does not contain the party, add it");
			}
		return (map);
		}

	public HashMap <Integer,Integer> completeMap(HashMap <Integer,Integer> map) // complete the allocation of seats with the parties who have 0 seats
		{
		if (map.size()<arrayOfParties.size())
			{
			for (int p=0;p<arrayOfParties.size();++p)
				{
				Party party=(Party)arrayOfParties.get(p);
				int nameOfParty=party.getName();
//System.out.println("the current party is "+nameOfParty);
				boolean contains=map.containsKey(new Integer(nameOfParty));
				if (contains==false)
					{
//System.out.println("added to the allocazioneSeggi");
					map.put(new Integer(nameOfParty),new Integer(0));
					}
				}// end loop on parties
			}
		return (map);
		}

	// create a list of the keys corresponding to the max in the values of the map (values are Integers, so are keys)
	public LinkedList <Integer> getListOfMaxElements(HashMap <Integer,Integer> map)
		{
//System.out.println("get list of max elements");
		int max=getMax(map);
//System.out.println("maxVotes is "+max);
		Set <Integer> mapKeys=map.keySet();
		Iterator <Integer> i=mapKeys.iterator();
		LinkedList <Integer> listOfMax=new LinkedList <Integer> ();
		while (i.hasNext())
			{
			Integer key = i.next();
			int value =(map.get(key)).intValue();
			if (value==max)
				{
//System.out.println("party "+i+" has the maximum of "+value+" votes");
				listOfMax.add(key);
				}
			} // end of while i.hasNext();
		return (listOfMax);
		}

	// create a list of the keys corresponding to the min in the values of the map (values are Integers, so are keys)
	public LinkedList <Integer> getListOfMinElements(HashMap <Integer,Integer> map)
		{
		int min=getMin(map);
//System.out.println("minimum in map is "+min);
		Set <Integer> mapKeys=map.keySet();
		Iterator <Integer> i=mapKeys.iterator();
		LinkedList <Integer> listOfMin=new LinkedList <Integer> ();
		while (i.hasNext())
			{
			Integer key = i.next();
			int value =(map.get(key)).intValue();
			if (value==min)
				{
//System.out.println("partito "+partitoCodice.intValue()+" has the minimum of votes");
				listOfMin.add(key);
				}
			} // end of while i.hasNext();
		return (listOfMin);
		}


	// get the maximum of (Integer) values in a map
	public int getMax(HashMap <Integer,Integer> map)
		{
		Collection <Integer> mapValues=map.values();
		Iterator <Integer> i=mapValues.iterator();
		int max=0;
		while (i.hasNext())
			{
			int val=(i.next()).intValue();
			max=(val>max)?val:max;
//System.out.println("in getMax: val is "+val+" so max becomes "+max);
			}
		return(max);
		}

	// get the minimum of (Integer) values in a map
	public int getMin(HashMap <Integer,Integer> map)
		{
		// set min to the maximum value in the map, then look for the minimum
		int min = getMax(map);
//System.out.println("initial minimum: "+min);
		Collection <Integer> mapValues=map.values();
		Iterator <Integer> i=mapValues.iterator();
		while (i.hasNext())
			{
			int val=(i.next()).intValue();
			min=(val<min)?val:min;
//System.out.println("in getMin: val is "+val+" so min becomes "+min);
			}
		return(min);
		}


	// create a list of the keys corresponding to the max in the values of the map (keys are integers, values are doubles)
	public LinkedList <Integer> getListOfMaxElementsDouble(HashMap <Integer,Double> map)
		{
//System.out.println("get list of max elements");
		double max=getMaxDouble(map);
//System.out.println("maxVotes is "+max);
		Set <Integer> mapKeys=map.keySet();
		Iterator <Integer> i=mapKeys.iterator();
		LinkedList <Integer> listOfMax=new LinkedList <Integer> ();
		while (i.hasNext())
			{
			Integer key = i.next();
			double value =(map.get(key)).doubleValue();
			if (value==max)
				{
//System.out.println("party "+i+" has the maximum of "+value+" votes");
				listOfMax.add(key);
				}
			} // end of while i.hasNext();
		return (listOfMax);
		}

	// create a list of the keys corresponding to the min in the values of the map (keys are integers, values are doubles)
	public LinkedList <Integer> getListOfMinElementsDouble(HashMap <Integer,Double> map)
		{
		double min=getMinDouble(map);
//System.out.println("maxVotes is "+maxVotes);
		Set <Integer> mapKeys=map.keySet();
		Iterator <Integer> i=mapKeys.iterator();
		LinkedList <Integer> listOfMin=new LinkedList <Integer> ();
		while (i.hasNext())
			{
			Integer key = i.next();
			double value =(map.get(key)).doubleValue();
			if (value==min)
				{
//System.out.println("partito "+partitoCodice.intValue()+" has the minimum of votes");
				listOfMin.add(key);
				}
			} // end of while i.hasNext();
		return (listOfMin);
		}


	// get the maximum of (Double) values in a map
	public double getMaxDouble(HashMap <Integer,Double> map)
		{
		Collection <Double> mapValues=map.values();
		Iterator <Double> i=mapValues.iterator();
		double max=0;
		while (i.hasNext())
			{
			double val=(i.next()).doubleValue();
			max=(val>max)?val:max;
//System.out.println("in getMax: val is "+val+" so max becomes "+max);
			}
		return(max);
		}

	// get the minimum of (Double) values in a map
	public double getMinDouble(HashMap <Integer,Double> map)
		{
		// set min to the maximum value in the map, then look for the minimum
		double min = getMaxDouble(map);
		Collection <Double> mapValues=map.values();
		Iterator <Double> i=mapValues.iterator();
		while (i.hasNext())
			{
			double val=(i.next()).doubleValue();
			min=(val<min)?val:min;
//System.out.println("in getMax: val is "+val+" so min becomes "+min);
			}
		return(min);
		}

		
	public HashMap <Integer,Integer> countVotesParty(ArrayList <Party> arrayOfParties,LinkedList <Voter> voters)
		{
		HashMap <Integer,Integer> votesForParty=new HashMap <Integer,Integer> ();
		for (int p=0;p<arrayOfParties.size();++p)
			{
			Party party=arrayOfParties.get(p);
			int partyName=party.getName();
			// find his votes
			int totalVotes=findTotalVotesForParty(partyName,voters);
//System.out.println("partito "+partitoCodice+" has "+totalVotes+" votes");
			// add them to a temporary map
			votesForParty.put(new Integer(partyName),new Integer(totalVotes));
			}
		return(votesForParty);
		}

	public LinkedList <Integer> findPartyWithMostVotes(HashMap <Integer,Integer> votesOfParties)
		{
		int maxVotes=getMax(votesOfParties);
//System.out.println("maxVotes is "+maxVotes);
		Set <Integer> mapKeys=votesOfParties.keySet();
		Iterator <Integer> i=mapKeys.iterator();
		LinkedList <Integer> partyWithMostVotes=new LinkedList <Integer> ();
		while (i.hasNext())
			{
			Integer partyName=i.next();
			int vote=(votesOfParties.get(partyName)).intValue();
			if (vote==maxVotes)
				{
//System.out.println("aParty "+partyName.intValue()+" has the maximum of votes ");
				partyWithMostVotes.add(partyName);
				}
			} // end of while i.hasNext();
		return (partyWithMostVotes);
		}

		
	// save the simulated parliaments to a string
	// if argument is "all", call the function recursively on all keys of the parliaments map
	// otherwise, extract the required parliament from the parliaments map.
	public String toString(SimulationRepository sr) {
		StringBuffer out = new StringBuffer();
//		out.append("BEGIN PARLIAMENT");
		if (sr != null) {
			out.append(sr.toStringSimulationParameters());
		}
		out.append("\n"+"parliament_name:"+getParliamentName()+"\n");
		out.append("\n"+"majorityLevel:"+majorityLevel+"\n");
//		out.append(language.getString("labels","parties")+" \t| "+language.getString("uninominal","OneDistrictProportional")+" \t| "+this.getParliamentName()+"\n");
		// saving the composition of the parliament: values separated by tabs
		// parties then one district proportional then current system (values, then descr in brackets)
		// get parties in order
		String strRef = new String();
		String strCur = new String();
		String strVRef = new String();
		String strVCur = new String();
		HashMap <Integer,Integer> vp = getVotesForParliament();
		for (int i=0;i<arrayOfParties.size();++i) {
			Party party = (Party)arrayOfParties.get(i);
			Integer partyName = new Integer(party.getName());
			Integer seatsProportional = proportionalAllocationOfSeats.get(partyName);
			Integer seatsCurrent = allocationOfSeats.get(partyName);
			Integer votesProportional = votesPerPartyRef.get(partyName);
			Integer votesCurrent = vp.get(partyName);
			strRef+="refAllocSeats_party_"+partyName+":"+seatsProportional+"\n";
			strCur+="allocSeats_party_"+partyName+":"+seatsCurrent+"\n";
			strVRef+="refVotes_party_"+partyName+":"+votesProportional+"\n";
			strVCur+="votes_party_"+partyName+":"+votesCurrent+"\n";
		}
		out.append(strRef+"\n"+strCur+"\n"+strVRef+"\n"+strVCur+"\n");
		out.append("\n");
////		// saving the governments
////		out.append(language.getString("labels","governments")+":\n");
////		Set <String> keys = governments.keySet();
////		Iterator<String> k = keys.iterator();
////		while (k.hasNext()) {
////			String key = k.next();
////			String values = ((LinkedList)governments.get(key)).toString();
////			out.append(key+" -> "+values+"\n");
////		}
////		out.append("//---------------------\n\n");
////		out.append("//Votes per party (key=party = value=number of votes)\n");
////		Iterator <Party> p = arrayOfParties.iterator();
////		while (p.hasNext()) {
////			Party party = p.next();
////			Integer name = new Integer(party.getName());
////			Integer value = new Integer(0);
////			if (votesPerParty.containsKey(name)) {
////				value = votesPerParty.get(name);
////			}
////			out.append(name+" = "+value+"\n");
////		}
		out.append("END PARLIAMENT\n");
		return (out.toString());
	}

	public HashMap<Integer,Integer> getVotesForParliament() {
		if (votesPerParty.size()==0) {
			votesPerParty = votesPerPartyRef;
		}
		votesPerParty = completeMap(votesPerParty);
		return votesPerParty;
	}
	// function that sorts the values in a map in decreasing or increasing order of the keys
	// returns a list containing the keys.
	// the arguments are the map and the order of the sorting (ascending or descending)
	public LinkedList <Integer> sortMap(HashMap  <Integer,Integer> map,String order,HashMap <Integer,Integer> checkIfTies) {
//System.out.println("entered sortMap of Parliament!");
//		HashMap <Integer,Integer> clone=map.clone();

		HashMap <Integer,Integer> clone = new HashMap <Integer,Integer> ();
		Set <Integer> keys = map.keySet();
		Iterator <Integer> k = keys.iterator();
		while (k.hasNext()) {
			Integer key = k.next();
			Integer val = map.get(key);
			clone.put(key,val);
		}
		LinkedList <Integer> orderedValues=new LinkedList <Integer> ();
		// while the number of elements in orderedKeys is smaller than nbKeysToSort
		while (orderedValues.size()<map.size()) {
			// find the index corresponding to the minimum value in clone
			Integer index=getKeyForMinValue(clone,checkIfTies);
			// add to orderedKeys: if order="ascending", use addLast, else if "descending" use addFirst
			if (order.compareTo("ascending")==0) {
				orderedValues.addLast(index);
			} else if (order.compareTo("descending")==0) {
				orderedValues.addFirst(index);
			} else {
				System.out.println("the order "+order+" does not exist. You can only use \"ascending\" or \"descending");
				System.exit(0);
			}
			// remove the key/value combination from clone
			clone.remove(index);
//System.out.println("elements in map "+map.size()+" and elements in its cloned version "+clone.size());
		}
		return orderedValues;
	}// end of sortMap function

	Integer getKeyForMinValue(HashMap <Integer,Integer> map,HashMap <Integer,Integer> checkTies) {
//System.out.println("Entered getkey for min value");
//System.out.println("checkTies is : "+checkTies);
		Collection <Integer> mapValues=map.values();
		Iterator <Integer> i=mapValues.iterator();
		int min=Integer.MAX_VALUE;
		while(i.hasNext()) {
			int val=(i.next()).intValue();
			min=(val<min)?val:min;
		}
		Set <Integer> keys=map.keySet();
		Iterator <Integer> j=keys.iterator();
		Integer key=new Integer(0);
		LinkedList <Integer> keysWithMinValue = new LinkedList <Integer>();
		while(j.hasNext()) {
			key=j.next();
			int val=(map.get(key)).intValue();
			if (val==min) {
				keysWithMinValue.add(key);
			}
		}
//System.out.println("keys with min value list is created");
		if (keysWithMinValue.size()==1) {
//System.out.println("It has one element");
			return (Integer) keysWithMinValue.getFirst();
		} else {
//System.out.println("It has "+keysWithMinValue.size()+" elements");
			// look for values obtained by these keys in the checkTies map
			// (call the getKeyForMinValue recursively with a null checkties map)
			// if only one minimum, return it, else return one randomly
			if (checkTies != null) {
//System.out.println("There is a matrix in which to check wich element should be chosen");
				HashMap <Integer,Integer> mapToCheck = new HashMap <Integer,Integer> ();
				for (int k=0;k<keysWithMinValue.size();++k) {
					Integer element = keysWithMinValue.get(k);
					Integer value = checkTies.get(element);
					mapToCheck.put(element,value);
//System.out.println("Put element in maptocheck, key="+element+" value="+value);
				}
				LinkedList <Integer> sortedCheck = sortMap(mapToCheck,"descending",null);
//System.out.println("returning first element "+((Integer)sortedCheck.getFirst()));
				return (sortedCheck.getFirst());
			} else {// checkTies is null, choose one of keysWithMinValue at random
				int rdm = generator.nextInt(keysWithMinValue.size());
//System.out.println("returning random "+(keysWithMinValue.get(rdm)));
				return(keysWithMinValue.get(rdm));
			}
		}
	}

// rounding methods

	public HashMap <Integer,Integer> proportionalNoRounding(LinkedList <Voter> listOfVoters,ArrayList <Party> arrayOfParties,
			Integer incr,Double threshold,Integer numberToElect,String thresholdLevel) {

		HashMap <Integer,Integer> mapOfSeats = new HashMap <Integer,Integer> ();
		HashMap <Integer,Integer> totalVotes = new HashMap <Integer,Integer> ();
		int seatsNotAllocated = numberToElect.intValue();
		progressBar.setMaximum(arrayOfParties.size());
		int j = 0;
		try {
			HashMap <Integer,Integer> remainingVotes = new HashMap <Integer,Integer> ();
			// for each party
			Iterator <Party> i = arrayOfParties.iterator();
			int sumVotes = 0;
			while (i.hasNext()) {
				Thread.sleep(0);
				Party party = i.next();
				int nameOfParty = party.getName();
				int totalVotesForParty = findTotalVotesForParty(nameOfParty,listOfVoters);
				double share = party.getShare();
				int votes=findTotalVotesForParty(nameOfParty,listOfVoters);
				double percVotes = (double)votes*100/listOfVoters.size();
System.out.println("share "+share+" threshold "+threshold+" votes "+ votes+" percVotes "+percVotes+" thresholdlevel "+thresholdLevel);
				if ((thresholdLevel.compareTo("nation")==0) && (share<threshold.doubleValue())) {
System.out.println("nation and share smaller than threshold");
					totalVotes.put(new Integer(nameOfParty),new Integer(0));
				} else if ((thresholdLevel.compareTo("district")==0) && ((percVotes<threshold.doubleValue()))) {
System.out.println("district and % votes smaller than threshold");
					totalVotes.put(new Integer(nameOfParty),new Integer(0));
				} else {
System.out.println("greater than threshold");
					sumVotes+=votes;
					totalVotes.put(new Integer(nameOfParty),new Integer(votes));
				}
			}
System.out.println("numberToElect "+numberToElect);
System.out.println("sumVotes "+sumVotes+ " size of list of voters " + listOfVoters.size());


			int quota = sumVotes / numberToElect.intValue();

				// find the number of seats for each party
				// (the last one gets the difference between the total number of seats and the number of allocated seats
			i = arrayOfParties.iterator();
			while (i.hasNext()) {
				Party party = i.next();
				int nameOfParty = party.getName();
				int totalVotesForParty = totalVotes.get(new Integer(nameOfParty)).intValue();
				int numberSeatsForParty = totalVotesForParty/quota;
				seatsNotAllocated -= numberSeatsForParty;
				int unusedVotes = totalVotesForParty - (numberSeatsForParty * quota);
//System.out.println("party "+nameOfParty+" has "+unusedVotes+" unused votes");
				remainingVotes.put(new Integer(nameOfParty), new Integer(unusedVotes));
//System.out.println("party "+nameOfParty+"has "+numberSeatsForParty+" seats");
				mapOfSeats.put(new Integer(nameOfParty) , new Integer(numberSeatsForParty));
				progressBar.setValue(j);
				++j;
 			}
			// are there seats not attributed in the parliament?
			if (seatsNotAllocated > 0) {
//System.out.println("there are "+seatsNotAllocated+" seats not allocated");
				// create a vector of parties in decreasing order of remaining votes
				LinkedList <Integer> orderedParties = sortMap(remainingVotes,"descending",totalVotes);
				for (int k=0; k<seatsNotAllocated; ++k) {
					Integer party = orderedParties.get(k);
//System.out.println("giving an extra vote to party "+party);
					mapOfSeats = updateMap(mapOfSeats,party,1);
				}
			}
		} catch (InterruptedException e) {
			returnCode = "interrupted";
		}
		return mapOfSeats;
		

	}
	public HashMap <Integer,Integer> roundingDHondtSteLague(LinkedList <Voter> listOfVoters,ArrayList <Party> arrayOfParties,
			Integer incr,Double threshold,Integer numberToElect,String thresholdLevel) {
		int increment = incr.intValue();
		int limit = numberToElect.intValue() * 2;
		HashMap <Integer,Integer> seatsOfDistrict=new HashMap<Integer,Integer>();
		// theMatrix maps contain the parties names and their relative values from the divisor
		// NOTE: since a map cannot have duplicate keys (and there is no reason why to values
		// cannot be the same, we use 2 maps and an index. Then we order the map of values, retrieve the
		// index of the highest value and get the corresponding party from the map of parties
		HashMap <Integer,Integer> theMatrixParties=new HashMap <Integer,Integer> ();
		HashMap <Integer,Integer> theMatrixValues=new HashMap <Integer,Integer> ();
		// get the total of the votes for each party, using the voters first preferences
		int n=arrayOfParties.size();
		HashMap <Integer,Integer> totalVotes=new HashMap <Integer,Integer> ();
		for (int i=0;i<n;++i) {
			Party party=(Party)arrayOfParties.get(i);
			int nomeP=party.getName();
			int votes=findTotalVotesForParty(nomeP,listOfVoters);
			double share = party.getShare();
			double percVotes = (double)votes*100/listOfVoters.size();
			if ((thresholdLevel.compareTo("nation")==0) && (share<threshold.doubleValue())) {
				totalVotes.put(new Integer(nomeP),new Integer(0));
			} else if ((thresholdLevel.compareTo("district")==0) && (percVotes<threshold.doubleValue())) {
				totalVotes.put(new Integer(nomeP),new Integer(0));
			} else {
				totalVotes.put(new Integer(nomeP),new Integer(votes));
			}
		}
System.out.println("finished totalVotes");
		
		// loop from 1 to numberToElect, by increments of "increment"
		int index=1;
		for (int i=1;i<=limit;i+=increment) {
//System.out.println("creating TheMatrix, iterating : "+i+" of "+numberToElect.intValue());
			// divide all values in totalVotes by i, then put the result into theMatrixValues and theMatrixParties
			// taking totalVotes from the smallest to the largest party
			LinkedList <Integer> orderedVotesKeys=sortMap(totalVotes,"ascending",null);
			for (int j=0;j<orderedVotesKeys.size();++j) {
				Integer nome=orderedVotesKeys.get(j);
				int votes=(totalVotes.get(nome)).intValue();
				votes/=i;
				theMatrixParties.put(new Integer(index),nome);
				theMatrixValues.put(new Integer(index),new Integer(votes));
				++index;
			}
		}// end of for, theMatrix is created
System.out.println("finished creating the matrix");
System.out.println("theMatrix for parties: "+theMatrixParties.size());
System.out.println("theMatrix for values: "+theMatrixValues.size());
		// sort the entries in theMatrix by value until enough parties have been found for the district
System.out.println("sort the matrix");
		LinkedList <Integer> sortedIndexes=sortTheMatrix(theMatrixValues,theMatrixParties,"descending",getProportionalAllocationOfSeats());
System.out.println("done");
//System.out.println("sortedIndexes: "+sortedIndexes);
		// create seatsOfDistrict from the parties (count the number of times each party appears in the list)
System.out.println("allocate seats");
		for (int i=0;i<numberToElect.intValue();++i) {
			Integer ind=sortedIndexes.get(i);
			Integer party=theMatrixParties.get(ind);
//System.out.println("index: "+ind+" corresponds to party "+party);
			if (seatsOfDistrict.containsKey(party)) {
				int seats=(seatsOfDistrict.get(party)).intValue();
				++seats;
				seatsOfDistrict.remove(party);
				seatsOfDistrict.put(party,new Integer(seats));
			} else {
				seatsOfDistrict.put(party,new Integer(1));
			}
		}
//System.out.println("seatsOfDistrict after this: "+seatsOfDistrict);
		// fill up seatsOfDistrict with the unused party names with 0 seats if seatsOfDistrict
		// has less entries than number of parties
System.out.println("deal with unused votes");
		for (int i=0;i<arrayOfParties.size();++i) {
			Party party=arrayOfParties.get(i);
			Integer nameOfParty=new Integer(party.getName());
			if (!seatsOfDistrict.containsKey(nameOfParty)) {
				seatsOfDistrict.put(nameOfParty,new Integer(0));
			}
		}
//System.out.println("seatsOfDistrict finally: "+seatsOfDistrict);
		return seatsOfDistrict;
	}// end of roundingDHondtSteLague method

	// Hare and imperiali methods to compute the seats in the plurinominal colleges
	public HashMap  <Integer,Integer> roundingHareImperiali(LinkedList <Voter> listOfVoters,ArrayList <Party> arrayOfParties,
			Integer incr,Double threshold,Integer numberToElect,String thresholdLevel) {
		int increment = incr.intValue();
		HashMap <Integer,Integer> seatsOfDistrict=new HashMap <Integer,Integer> ();
		// numero di elettori nel district
		// get the total votes
		int numVoters = 0;
		int n=arrayOfParties.size();
		HashMap <Integer,Integer> totalVotes=new HashMap <Integer,Integer> ();
		for (int i=0;i<n;++i) {
			Party party=arrayOfParties.get(i);
			int nomeP=party.getName();
			int votes=findTotalVotesForParty(nomeP,listOfVoters);
			double share = party.getShare();
			double percVotes = (double)votes*100/listOfVoters.size();
			if ((thresholdLevel.compareTo("nation")==0) && (share<threshold.doubleValue())) {
				totalVotes.put(new Integer(nomeP),new Integer(0));
			} else if ((thresholdLevel.compareTo("district")==0) && (percVotes<threshold.doubleValue())) {
				totalVotes.put(new Integer(nomeP),new Integer(0));
			} else {
				totalVotes.put(new Integer(nomeP),new Integer(votes));
				numVoters+=votes;
			}
		}
System.out.println("hare total votes "+totalVotes);

		// quota (numero di elettori nel district/(numero di candidati per district plurinominale+increment)
//System.out.println("numVoters "+numVoters+" numberToElect "+numberToElect.intValue()+" increment "+increment);
		int quota=numVoters/(numberToElect.intValue()+increment);


		// find the number of seats for each party in totalVotes, and put the party name and number
		// in seatsOfDistrict; update the counter nbSeatsAlreadyAttributed; create the map with the remaining seats
		int nbSeatsAlreadyAttributed=0;
		HashMap <Integer,Integer> remainingVotes=new HashMap <Integer,Integer> ();
		LinkedList <Integer> orderedVotesKeys=sortMap(totalVotes,"ascending",null);
		for (int i=0;i<orderedVotesKeys.size();++i) {
			if (nbSeatsAlreadyAttributed<numberToElect.intValue()) {
				Integer partyName=orderedVotesKeys.get(i);
				int votes=(totalVotes.get(partyName)).intValue();
				int nbSeats=votes/quota;
				// check that the number of seats is smaller than the number of seats left to attribute
				// (can happen with Imperiali where the quota is smaller, for small colleges)
				int nbSeatsToAttribute=numberToElect.intValue()-nbSeatsAlreadyAttributed;
				if (nbSeats>nbSeatsToAttribute) {
					nbSeats=nbSeatsToAttribute;
				}
				seatsOfDistrict.put(partyName,new Integer(nbSeats));
				nbSeatsAlreadyAttributed+=nbSeats;
				int lastVotes=votes-(quota*nbSeats);
				remainingVotes.put(partyName,new Integer(lastVotes));
			}
		}
System.out.println("hare seats of district "+seatsOfDistrict);
System.out.println("hare remaining votes "+remainingVotes);
System.out.println("nbSeatsAlreadyAttributed "+nbSeatsAlreadyAttributed+" number to elect "+numberToElect);
		// complete seatsOfDistrict with the parties having the largest number of remaining seats
		while (nbSeatsAlreadyAttributed<numberToElect.intValue()) {
			// get key for max value of remainingVotes
			Integer maxKey=getKeyForMaxValue(remainingVotes);
System.out.println("maxKey "+maxKey);
			// add 1 to the number of seats of this party, increment nbSeatsAlreadyAttributed
			int nbSeats = 0;
			if (seatsOfDistrict.containsKey(maxKey)) {
				nbSeats=(seatsOfDistrict.get(maxKey)).intValue();
				seatsOfDistrict.remove(maxKey);
			}
			++nbSeats;
			seatsOfDistrict.put(maxKey,new Integer(nbSeats));
			remainingVotes.remove(maxKey);
			++nbSeatsAlreadyAttributed;
		}
System.out.println("hare seats of district (final)"+seatsOfDistrict);
		return seatsOfDistrict;
	}// end of roundingHareImperiali




	int getMaxKey(HashMap <Integer,Integer>  map) {
		Set <Integer> mapKeys=map.keySet();
		Iterator <Integer> i=mapKeys.iterator();
		int max=0;
		while (i.hasNext()) {
			int val=(i.next()).intValue();
			max=(val>max)?val:max;
		}
		return max;
	}

	Integer getKeyForMinValue(HashMap <Integer,Integer> map) {
		Collection <Integer> mapValues=map.values();
		Iterator <Integer> i=mapValues.iterator();
		int min=Integer.MAX_VALUE;
		while(i.hasNext()) {
			int val=(i.next()).intValue();
			min=(val<min)?val:min;
		}
		Set <Integer> keys=map.keySet();
		Iterator <Integer> j=keys.iterator();
		Integer key=new Integer(0);
		while(j.hasNext()) {
			key=j.next();
			int val=((Integer)map.get(key)).intValue();
			if (val==min) {
				break;
			}
		}
		return key;
	}

	Integer getKeyForMaxValue(HashMap <Integer,Integer> map) {
		Collection <Integer> mapValues=map.values();
		Iterator <Integer> i=mapValues.iterator();
		int max=Integer.MIN_VALUE;
		while(i.hasNext()) {
			int val=(i.next()).intValue();
			max=(val>max)?val:max;
		}
		Set <Integer> keys=map.keySet();
		Iterator <Integer> j=keys.iterator();
		Integer key=new Integer(0);
		while(j.hasNext()) {
			key=j.next();
			int val=(map.get(key)).intValue();
			if (val==max) {
				break;
			}
		}
		return key;
	}

	public String getRounding() {
		return rounding;
	}

	public double getMajorityLevel() {
		return majorityLevel;
	}

	public HashMap<ArrayList<Integer>,Integer>getMyersonValueFunction() {
		return myersonValueFunction;
	}

	
	public LinkedList <Integer> sortTheMatrix(HashMap <Integer,Integer> mapValues,HashMap <Integer,Integer> mapKeys,String order,HashMap <Integer,Integer> checkIfTies) {
//System.out.println("entered sortTheMatrix!");
//		HashMap <Integer,Integer> clone=(HashMap <Integer,Integer>)mapValues.clone();
		HashMap <Integer,Integer> clone= new HashMap <Integer,Integer>();
		Set <Integer> keysm = mapValues.keySet();
		Iterator <Integer> km = keysm.iterator();
		while (km.hasNext()) {
			Integer key = km.next();
			Integer val = mapValues.get(key);
			clone.put(key,val);
		}
		
		LinkedList <Integer> orderedValues=new LinkedList<Integer>();
		// while the number of elements in orderedKeys is smaller than nbKeysToSort
		while (orderedValues.size()<mapValues.size()) {
System.out.print(orderedValues.size()+"-");
//System.out.println("clone at beginning of loop"+clone);
			// find the index corresponding to the minimum value in clone
			Integer index = new Integer(0);
			Collection <Integer> col=clone.values();
			Iterator <Integer> i=col.iterator();
			int min=Integer.MAX_VALUE;
			while(i.hasNext()) {
				int val=(i.next()).intValue();
				min=(val<min)?val:min;
			}
			Set <Integer> keys=clone.keySet();
			Iterator <Integer> j=keys.iterator();
			Integer key=new Integer(0);
			LinkedList <Integer> keysWithMinValue = new LinkedList<Integer>();
			while(j.hasNext()) {
				key=j.next();
				int val=(clone.get(key)).intValue();
				if (val==min) {
					keysWithMinValue.add(key);
				}
			}
//System.out.println("keys with min value list is created: "+keysWithMinValue);
			if (keysWithMinValue.size()==1) {
//System.out.println("It has one element");
				index = keysWithMinValue.getFirst();
//System.out.println("Index is "+index);
			} else {
//System.out.println("It has "+keysWithMinValue.size()+" elements");
				// look for values obtained by these keys in the checkTies map
				// (call the getKeyForMinValue recursively with a null checkties map)
				// if only one minimum, return it, else return one randomly
				if (checkIfTies != null) {
//System.out.println("There is a matrix in which to check wich element should be chosen");
					HashMap <Integer,Integer>  mapToCheck = new HashMap <Integer,Integer> ();
					for (int k=0;k<keysWithMinValue.size();++k) {
						Integer el = keysWithMinValue.get(k);
						Integer element = mapKeys.get(el);
						Integer value = checkIfTies.get(element);
						mapToCheck.put(el,value);
//System.out.println("Put element in maptocheck, key="+el+" value="+value+" (party is )"+element);
					}
					LinkedList <Integer> sortedCheck = sortMap(mapToCheck,"ascending",null);
//System.out.println("returning first element "+((Integer)sortedCheck.getFirst()));
					index = sortedCheck.getFirst(); 
//System.out.println("Index is "+index);
				} else {// checkTies is null, choose one of keysWithMinValue at random
					int rdm = generator.nextInt(keysWithMinValue.size());
//System.out.println("returning random "+((Integer)keysWithMinValue.get(rdm)));
					index = keysWithMinValue.get(rdm);
//System.out.println("Index is "+index);
				}
			}
			
			// add to orderedKeys: if order="ascending", use addLast, else if "descending" use addFirst
			if (order.compareTo("ascending")==0) {
				orderedValues.addLast(index);
			} else if (order.compareTo("descending")==0) {
				orderedValues.addFirst(index);
			} else {
				System.out.println("the order "+order+" does not exist. You can only use \"ascending\" or \"descending");
				System.exit(0);
			}
			// remove the key/value combination from clone
			clone.remove(index);
		}
		return orderedValues;
	}// end of sortTheMatrix function


	

}// end class definition


