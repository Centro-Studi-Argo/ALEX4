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
package parliaments.uninominal;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Toolkit;
import java.text.*;
import java.lang.reflect.*;

import classesWrittenByOthers.*;
import votingObjects.*;
import parliaments.*;
import gui.*;

public class ThresholdProportional extends Parliament {

	private double threshold = 5;
	private int maxValue = 100;

	// constructor

	public ThresholdProportional(SimulationRepository sr) {
		super(sr);

		threshold = setParameterDialog(threshold,language.getString("messages","thresholdProportional"),maxValue);

		Set roundingKeys = roundingMethods.keySet();
		rounding = setMethodDialog(roundingKeys,"multiDistrictPlurinominal");

		
		String[] vals = {"ThresholdProportional",(new Double(threshold)).toString(),rounding}; 
		parliamentKey = makeKey(vals);	
		ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
		progressBar.setMaximum(arrayOfParties.size()*2);
		
//		if (simulationRepository.containsParliament(parliamentKey)) {
//			allocationOfSeats = simulationRepository.loadParliament(parliamentKey);
//		} else {
			allocationOfSeats = findAllocationOfSeats();
			simulationRepository.saveParliament(parliamentKey,this);
			setNoteToGraph(language.getString("messages","thresholdProportional")+" "+threshold + " rounding "+ rounding);
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
//		}
System.out.println(language.getString("uninominal","ThresholdProportional")+" created");
	}

	public String getParliamentName() {
		return (language.getString("uninominal","ThresholdProportional")+" - "+threshold+" - "+rounding+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}


	// method to allocate the seggi (current method)
	public HashMap <Integer,Integer> findAllocationOfSeats()
		{

//		return getProportionalAllocationOfSeats();
		// get the name and parameter of method from the map roundingMethods.
		String[] methodChar = (String[])roundingMethods.get(rounding);
		String nameOfMethod = methodChar[0];
System.out.println("nameOfMethod "+nameOfMethod);
		Integer paramOfMethod = new Integer(methodChar[1]);
System.out.println("paramOfMethod "+paramOfMethod+" is of type "+(paramOfMethod.getClass()).getName());
		// prepare the method to be invoked
		HashMap <Integer,Integer> mapOfSeats=new HashMap<Integer,Integer>();
		try {
			Class thisClass = this.getClass();
			Class[] typeOfParameters = new Class[] {LinkedList.class,ArrayList.class,Integer.class,Double.class,Integer.class,String.class};
			Method method = (this.getClass()).getMethod(nameOfMethod,typeOfParameters);
System.out.println("sizeOfParliament "+sizeOfParliament);
			// list of parameters to invoke the method to find the seats in the district
			Object[] paraList = new Object[]{listOfVoters,arrayOfParties,paramOfMethod,new Double(threshold),new Integer(sizeOfParliament),"nation"};
			mapOfSeats = (HashMap<Integer,Integer>)method.invoke(this,paraList);
			// complete mapOfSeats with missing parties (those who got no seats at all)
			mapOfSeats = completeMap(mapOfSeats);
		} catch (NoSuchMethodException er) {
System.out.println("---- no such method ----");
er.printStackTrace();
System.out.println("--------");
					returnCode = "interrupted";
			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","noSuchMethod")+" "+nameOfMethod);
		} catch (InvocationTargetException er) {
System.out.println("--- invocation -----");
er.printStackTrace();
System.out.println("--------");
					returnCode = "interrupted";
			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","invocationTarget")+" "+nameOfMethod+". "+er.getCause());
		} catch (IllegalAccessException er) {
System.out.println("---- illegal access ----");
er.printStackTrace();
System.out.println("--------");
					returnCode = "interrupted";
			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","illegalAccess")+" "+nameOfMethod);
//		} catch (ClassNotFoundException er) {
//			JOptionPane.showMessageDialog(mainFrame,language.getString("messages","classNotFound")+" "+nameOfMethod);
//				} catch (InterruptedException e) {
//					returnCode = "interrupted";
//	System.out.println("interrupted");
		}
		votesPerParty = votesPerPartyRef;
		return mapOfSeats;
	}
		
//		HashMap <Integer,Integer> mapOfSeats = new HashMap <Integer,Integer> ();
//		HashMap <Integer,Integer> totalVotes = new HashMap <Integer,Integer> ();
//System.out.println("Soglia di threshold(from allocazioneSeggi): "+threshold);
//		int newNumberVoters=listOfVoters.size();
//		HashMap <Integer,Integer> newMapOfParties=new HashMap <Integer,Integer> ();
//		try {
//			// create a newMapOfParties which contains only the parties to consider in this case
//			// with their total votes (so it is a map, not a list)
//			for (int p=0;p<arrayOfParties.size();++p)
//				{
//				Thread.sleep(0);
//				progressBar.setValue(p);
//				// get the current party
//				Party aParty=arrayOfParties.get(p);
//				int nameOfParty=aParty.getName();
//				// find his percentage of votes
//				int totalVotesForParty=findTotalVotesForParty(nameOfParty,listOfVoters);
//				totalVotes.put(new Integer(nameOfParty),new Integer(totalVotesForParty));
//				double percVotes=aParty.getShare();
//System.out.println("aParty "+nameOfParty+" had "+totalVotesForParty+" votes, so "+percVotes+"%");
//				// if this percentage is (strictly) smaller than threshold, eliminate the
//				// party from the list to consider, and remove its electors (totalVotes) from the
//				// total of electors to consider when creating the parliament
//				if (percVotes<threshold)
//					{
//System.out.println("under "+threshold+"%");
//					newNumberVoters-=totalVotesForParty;
//System.out.println("newNumberVoters becomes "+newNumberVoters);
//					// add to mapOfSeats the parties that were excluded from the vote, with 0 seats
//					mapOfSeats.put(new Integer(nameOfParty),new Integer(0));
//					}
//				else
//					{
//System.out.println("over "+threshold+"%");
//					newMapOfParties.put(new Integer(nameOfParty),new Integer(totalVotesForParty));
//					}
//				}
//			// allocate the seats proportionally amongst the remaining parties (so using newNumberVoters
//			// instead of the original number of electors)
//	
//			int seatsNotAllocated = sizeOfParliament;
//			progressBar.setMaximum(arrayOfParties.size());
//			int j = 0;
//			try {
//				int quota = newNumberVoters / sizeOfParliament;
//				HashMap  <Integer,Integer> remainingVotes = new HashMap <Integer,Integer> ();
//				// for each party
//				Iterator <Integer> i = (newMapOfParties.keySet()).iterator();
//				while (i.hasNext()) {
//					Thread.sleep(0);
//					Integer nameOfParty = i.next();
//					int totalVotesForParty = (newMapOfParties.get(nameOfParty)).intValue();
//					// find the number of seats for each party
//					// (the last one gets the difference between the total number of seats and the number of allocated seats
//					
//					int numberSeatsForParty = totalVotesForParty/quota;
//					seatsNotAllocated -= numberSeatsForParty;
//					int unusedVotes = totalVotesForParty - (numberSeatsForParty * quota);
//	System.out.println("party "+nameOfParty+" has "+unusedVotes+" unused votes");
//					remainingVotes.put(nameOfParty, new Integer(unusedVotes));
//	System.out.println("party "+nameOfParty+"has "+numberSeatsForParty+" seats");
//					mapOfSeats.put(nameOfParty , new Integer(numberSeatsForParty));
//					progressBar.setValue(j);
//					++j;
//	 			}
//				// are there seats not attributed in the parliament?
//				if (seatsNotAllocated > 0) {
//	System.out.println("there are "+seatsNotAllocated+" seats not allocated");
//					// create a vector of parties in decreasing order of remaining votes
//					LinkedList <Integer> orderedParties = sortMap(remainingVotes,"descending",totalVotes);
//					for (int k=0; k<seatsNotAllocated; ++k) {
//						Integer party = orderedParties.get(k);
//	System.out.println("giving an extra vote to party "+party);
//						mapOfSeats = updateMap(mapOfSeats,party,1);
//					}
//				}
//		} catch (InterruptedException e) {
//			returnCode = "interrupted";
//		}
//
//
//		} catch (InterruptedException e) {
//			returnCode = "interrupted";
//		}
//		return(mapOfSeats);
//		}
		
	}