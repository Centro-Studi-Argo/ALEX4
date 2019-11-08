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

import classesWrittenByOthers.*;
import votingObjects.*;
import parliaments.*;
import gui.*;
import indexes.*;

public class VAPSystem extends Parliament {

	int defaultPercSeats = 18;
	float valueA = 1;
	int valueX;
	HashMap <String,HashMap <Integer,Integer>> listOfAllocationsOfVotes = new HashMap <String,HashMap <Integer,Integer>> ();
	int maxValue = 100;
	String startNameOfVAParliament = language.getString("uninominal","VAPSystem");
	String nameOfVAParliament;
	// constructor

	public VAPSystem(SimulationRepository sr) {
		super(sr);
		setNoteToGraph(language.getString("labels","VAPnoMajority"));
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
		setNoteToComposition(language.getString("labels","VAPnoMajority"));
		String[] vals = {"VAPSystem"};
		parliamentKey = makeKey(vals);

		nameOfVAParliament = startNameOfVAParliament;
		ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
		progressBar.setMaximum(5);

		if (simulationRepository.containsParliament(parliamentKey)) {
			allocationOfSeats = simulationRepository.loadParliament(parliamentKey);
		} else {
			allocationOfSeats = findAllocationOfSeats();
			simulationRepository.saveParliament(parliamentKey,this);
		}

System.out.println(language.getString("uninominal","VAPSystem")+" created");
	}

	public String getParliamentName() {
		return (nameOfVAParliament+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}

	// method to allocate the seggi (current method)
	public HashMap  <Integer,Integer> findAllocationOfSeats() {
		return getProportionalAllocationOfSeats();
	}	

	public HashMap  <Integer,Integer> findAllocationOfVotes() {
		HashMap <Integer,Integer> allocVotes = new HashMap <Integer,Integer> ();
		sumOfGovernmentVotes = 0;
System.out.println("vap: find allocation of votes");
			try {
				if (getMajorityFound() == true) {
System.out.println("VAP: majority is found");
				// values of the parameters
					int percX = -1;
					while (( percX < 0 ) || ( percX > 100 )) {
						percX = setParameterDialog(defaultPercSeats,language.getString("labels","VAPcrucialParties"),maxValue);
					}
					// valueX = sizeOfParliament * percX / 100
					valueX = (int) (sizeOfParliament * percX / 100);
	System.out.println("VAP: valueX "+valueX);
					Thread.sleep(0);
					progressBar.setValue(1);
					// valueA = (T - sum(X_i) + 2y) / (sum(X_i)
					// where T = size of parliament in terms of seats
					// sum(X_i) = sum of seats of the crucial parties (those in the government
					// with more than valueX seats in the parliament)
					// y = 1 that is the government keeps a majority of 1 if the small parties leave
				
					// first get the list of crucial parties and sum(X_i)
					LinkedList <Integer> crucialParties = new LinkedList<Integer>();
					int sumXi = 0;
					for (int p = 0 ; p < currentGovernment.size() ; ++p) {
						Integer nameOfParty = new Integer(currentGovernment.get(p));
						int nbSeats = (allocationOfSeats.get(nameOfParty)).intValue();
	System.out.println("VAP: partyName "+nameOfParty.toString()+" has "+nbSeats+" seats");
						if (nbSeats >= valueX) {
	System.out.println("\ttherefore is crucial");
							crucialParties.add(nameOfParty);
							sumXi += nbSeats;
						}
					}// end if
					Thread.sleep(0);
					progressBar.setValue(2);
					// compute valueA
					int valueY = 1;
					valueA = (float)(sizeOfParliament - sumXi + 2*valueY) / sumXi;
	System.out.println("VAP: valueA is "+valueA+" size of parliament is "+sizeOfParliament+" sumxi is "+sumXi);
					// if valueA is smaller than 1, set it to 1 so that the parliament in terms of votes is at least
					// as large as size of parliament in terms of seats
					valueA = (valueA < 1) ? 1 : valueA;
	System.out.println("VAP: valueA is "+valueA);
					Thread.sleep(0);
					progressBar.setValue(3);
					// create the allocation of votes: if party is in the list of crucial parties, multiply
					// its seats by valueA, otherwise, leave as they are
					Set <Integer> keys = allocationOfSeats.keySet();
					Iterator <Integer> k = keys.iterator();
					while (k.hasNext()) {
						Integer nameOfParty = k.next();
						Integer seats = allocationOfSeats.get(nameOfParty);
						if (crucialParties.contains(nameOfParty)) {
							int votes = Math.round((seats.intValue())*valueA);
	System.out.println("crucial party "+nameOfParty+" has now "+votes+" votes");
							allocVotes.put(nameOfParty,new Integer(votes));
							if (currentGovernment.contains(nameOfParty)) {
								sumOfGovernmentVotes += votes;
							}
						} else {
	System.out.println("non crucial party "+nameOfParty+" has now "+seats+" votes");
							allocVotes.put(nameOfParty,seats);
							if (currentGovernment.contains(nameOfParty)) {
								sumOfGovernmentVotes += seats.intValue();
							}
						}
						Thread.sleep(0);
						progressBar.setValue(4);
					}
				}
			// update representation index...
			// save allocVotes and valueA in listOfAllocationsOfVotes map (key = currentGovernment.toString() - x = valueX - a = valueA
			String key = currentGovernment.toString()+" x = "+valueX+" - a = "+valueA;
System.out.println("update list of allocation of votes ");
			if (!listOfAllocationsOfVotes.containsKey(key)) {
				listOfAllocationsOfVotes.put(key,allocVotes);
			}
			Thread.sleep(0);
			progressBar.setValue(5);
		} catch (InterruptedException e) {
			returnCode = "interrupted";
		}
		return allocVotes;
	}


	public void updateParliament(LinkedList <Integer> government,boolean majority,int sumOfVotes) {
		currentGovernment = government;
		majorityFound = majority;
		sumOfGovernmentVotes = sumOfVotes;
System.out.println("governments"+governments);
		// if a majority is found, add the currentGovernment in the map Governments as key
		// with sumOfgovernmentVotes as values (first element in a list)
		if (majorityFound == true) {
System.out.println("majority is found");
			allocationOfVotes = findAllocationOfVotes();
System.out.println("New index");
			RepresentationIndex newInd = new RepresentationIndex(this);
			if (!governments.containsKey(currentGovernment)) {
System.out.println("government "+currentGovernment+" does not exist yet, so save, sumOfGovernmentVotes = "+sumOfGovernmentVotes);
				LinkedList <String> values = new LinkedList<String>();
				values.add("sumOfGovernmentVotes="+(new Integer(sumOfGovernmentVotes)).toString());
				values.add("RepresentationIndex="+newInd.getIndexValue());
				governments.put(currentGovernment.toString(),values);
			}
System.out.println("there are "+currentGovernment.size()+" parties in the government, notify changes");
System.out.println("governments (after)"+governments);
			Object[] args = {new Float(valueA)};
			setNoteToGraph(MessageFormat.format(language.getString("labels","VAPmajority"),args));
			setNoteToComposition(MessageFormat.format(language.getString("labels","VAPmajority"),args));
			nameOfVAParliament = startNameOfVAParliament+currentGovernment.toString()+valueX;
			String[] vals = {"VAPSystem",currentGovernment.toString(),(new Integer(valueX)).toString()};
			parliamentKey = makeKey(vals);
System.out.println("name VAP "+nameOfVAParliament);
System.out.println("parliamentKey "+parliamentKey);

			simulationRepository.saveParliament(parliamentKey,this);


		} else {
			allocationOfVotes = new HashMap<Integer,Integer>();
			setNoteToGraph(language.getString("labels","VAPnoMajority"));
			setNoteToComposition(language.getString("labels","VAPnoMajority"));
		}
		
		fireParliamentEvent(new ParliamentEvent(this));
	}


	// save the simulated parliaments to a string
	// if argument is "all", call the function recursively on all keys of the parliaments map
	// otherwise, extract the required parliament from the parliaments map.
	public String toString(SimulationRepository sr) {
		StringBuffer out = new StringBuffer();
		out.append(super.toString(sr));
		// saving the allocation of votes and value of A for each government
		out.append(language.getString("labels","allocationOfVotesTitle")+":\n");
		Set keys = listOfAllocationsOfVotes.keySet();
		Iterator k = keys.iterator();
		while (k.hasNext()) {
			String key = (String)k.next();
			HashMap <Integer,Integer> values = listOfAllocationsOfVotes.get(key);
			out.append(key+" : \n");
			out.append(language.getString("labels","parties")+" \t| "+language.getString("labels","allocationOfVotes")+"\n");
			for (int i=0;i<arrayOfParties.size();++i) {
				Party party = arrayOfParties.get(i);
				Integer partyName = new Integer(party.getName());
				Integer votesCurrent = values.get(partyName);
				out.append(partyName+"\t\t|\t\t"+votesCurrent+"\n");
			}
		}
		
		out.append("---------------------\n\n");
		return (out.toString());
	}


	
	}// end of class sistemaVAP


