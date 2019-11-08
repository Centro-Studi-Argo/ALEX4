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

public class CondorcetWinner extends Parliament {

	private int nbDistrictsWithCycles = 0;
	
	// constructor
	public CondorcetWinner(SimulationRepository sr) {
		super(sr);
		String[] vals = {"CondorcetWinner"};
		parliamentKey = makeKey(vals);	

		ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
		progressBar.setMaximum(((Integer)simulationRepository.getGeneralParameters().get("numberUninominalDistricts")).intValue());
		
		if (simulationRepository.containsParliament(parliamentKey)) {
			allocationOfSeats = simulationRepository.loadParliament(parliamentKey);
		} else {
			allocationOfSeats = findAllocationOfSeats();
			simulationRepository.saveParliament(parliamentKey,this);
		}
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);

System.out.println(language.getString("uninominal","CondorcetWinner")+" created");
	}

	// abstract method that must be overridden by all parliaments
	public HashMap <Integer,Integer> findAllocationOfSeats() {
		HashMap <Integer,Integer> allocSeats = new HashMap<Integer,Integer>();

		LinkedList listOfUninominalDistricts = simulationRepository.getListOfUninominalDistricts();
		int numberOfUninominalDistricts = listOfUninominalDistricts.size();

		try {
			Thread.sleep(0);

			// for each district
			for (int c=0;c<numberOfUninominalDistricts;++c)
				{
				progressBar.setValue(c);
				DistrictUninominal aDistrict=(DistrictUninominal)listOfUninominalDistricts.get(c);
				LinkedList <Voter> votersInDistrict=aDistrict.getVoters();
	System.out.println("aDistrict "+aDistrict.getNameOfDistrictU());			
				ArrayList <Party> remainingParties=new ArrayList<Party>();
				Iterator <Party> pc = arrayOfParties.iterator();
				while (pc.hasNext()) {
					Party aParty = pc.next();
					remainingParties.add(aParty);
				}
//				remainingParties = (ArrayList<Party>)arrayOfParties.clone();

				
				Iterator <Party> pi = remainingParties.iterator();
				boolean winnerWasFound=false;
				while (pi.hasNext()) //(remainingParties.size()>0))
					{
					// winner is incremented each time a party has won a "duel" with another
					int winnerFound=0;
					// start with the first party
					Party referenceParty=pi.next();//remainingParties.getFirst();
					Integer referencePartyName=new Integer(referenceParty.getName());
					// and remove it from remainingParties
					pi.remove();//remainingParties.remove(referenceParty);
	System.out.println("arrayOfParties has "+arrayOfParties.size()+" elements while remainingParties has "+remainingParties.size()+" elements");
					// for each party in arrayOfParties
					for (int p=0;p<arrayOfParties.size();++p)
						{
	System.out.println("Entered loop on parties, p is "+p);
						Party aParty=arrayOfParties.get(p);
						Integer aPartyName=new Integer(aParty.getName());
	System.out.println("the reference party is "+referencePartyName.intValue()+" and the current party is "+aPartyName.intValue());
						// if the name of the current party is the same as the reference
						// party, go to the next round of the loop
						if (referencePartyName.intValue()==aPartyName.intValue())
							{
							continue;
							}
						int preferredBy=0;
						// for each elector
						for (int e=0;e<votersInDistrict.size();++e)
							{
							Voter aVoter=votersInDistrict.get(e);
	System.out.println("Voter "+aVoter.getNameVoter());
							// create the linkedList with the two parties to compare
							LinkedList <Integer> partiesToCompare=new LinkedList<Integer>();
							partiesToCompare.add(referencePartyName);
							partiesToCompare.add(aPartyName);
							// find the preferred
							Integer winner=aVoter.getPreferredParty(partiesToCompare);
	System.out.println("the winner is "+winner.intValue());
							// if the winner is the referenceParty, continue to the next elector
							// after incrementing preferredBy
							if (winner.intValue()==referencePartyName.intValue())
								{
								++preferredBy;
	System.out.println("same as refParty, preferredBy is "+preferredBy+" continue");
								continue;
								}
							}// end loop on electors
						// check the value of preferredBy: if it is the majority of the electors,
						// it means the party has won this "dual". Go to the next party
	System.out.println("preferredBy is "+preferredBy+" and the majority is "+(votersInDistrict.size()/2));
						if (preferredBy>(votersInDistrict.size()/2))
							{
	System.out.println("So a winner is found, increment winnerFound");
							++winnerFound;
							}
						}// end loop on parties
					// the reference party has had "duels" with all the other parties
					// look at the value of winnerFound. If it is equal to the number of duels (nb of parties minus 1)
					// then a winner has been found. Otherwise, reset winnerFound, take
					// reference party from remainingParties and re-do the tests
					// the winner is the current referenceParty, add it to allocSeats
					// (add 1 to nbSeggi if it exists, add new value otherwise)
					// else, reset winnerFound and go to the next duel
					winnerWasFound=false;
					int nbDuels=arrayOfParties.size()-1;
	System.out.println("the aParty "+referencePartyName.intValue()+" has won "+winnerFound+" duels out of "+nbDuels);
					if (winnerFound==nbDuels)
						{
	System.out.println("so we have a winner, go to the next college");
						// we have a winner! update allocSeats and go to another college
						allocSeats=updateMap(allocSeats,referencePartyName,1);
						winnerWasFound=true;
						break;
						}
					}  // end while remainingParties
	System.out.println("winner was found ? "+winnerWasFound);
				// if we arrived here without having a winner, need to signal a cycle
				// in this case, the winner is attributed at the relative majority. we need
				// to signal this on the result window, and also on the printed output.
				if (winnerWasFound==false)
					{
					String message="Si e' verificato un ciclo in aDistrict "+aDistrict.getNameOfDistrictU();
	System.out.println(message);
					// increase the counter for the number of colleges for which the winner is attributed
					// at the relative majority
					++nbDistrictsWithCycles;
					// attribute the winner at the relative majority
					// count the number of votes for each party
					HashMap <Integer,Integer> votesForParties=countVotesParty(arrayOfParties,votersInDistrict);
					// find the party with the most votes
					LinkedList <Integer> partiesWithMostVotes=getListOfMaxElements(votesForParties);
					// in the map allocSeats: if the winning party already has an entry
					// add one to the number of seats. If it does not have an entry, add it and
					// put 1 as value for the seats.
					// if there is more than one party....
					Integer indexOfParty=new Integer(0);
					if (partiesWithMostVotes.size()==1) {
						indexOfParty=partiesWithMostVotes.getFirst();
					} else {
						// case where more than one party has the same number of votes
						// (unlikely in reality, but possible in simulations with few electors)
						// in this case, give the seat to the party with the highest share
						// in the whole population.
						// If there are more than one, get a number at random between 0 and (nbOfParties)-1
						// and take the party at the corresponding index in the list.
						HashMap <Integer,Integer>sharesOfParties = new HashMap<Integer,Integer>();
						Iterator <Integer> s = partiesWithMostVotes.iterator();
						while (s.hasNext()) {
							Integer partyname = s.next();
							Party party = arrayOfParties.get(partyname.intValue()-1);
System.out.println("partyname "+party+" name of party "+party.getName()+" has share "+party.getShare());
							sharesOfParties.put(partyname,new Integer(party.getVotes()));
						}
						LinkedList <Integer> bestParties = getListOfMaxElements(sharesOfParties);
System.out.println("there are "+bestParties.size()+" parties with the highest share");
						if (bestParties.size()==1) {
System.out.println("return it");
							indexOfParty = bestParties.getFirst();
						} else {
							int rdm = generator.nextInt(bestParties.size());
System.out.println("choose the "+rdm+"th one at random");
							indexOfParty = bestParties.get(rdm);
						}
					}
					// update allocSeats
					allocSeats=updateMap(allocSeats,indexOfParty,1);
					}
				}// end loop on colleges

			// check that allocSeats contains all parties (add missing parties with 0 seats)
			allocSeats=completeMap(allocSeats);
		
			// set the note to composition indicating the nb of districts with cycles
			setNoteToComposition(language.getString("labels","condorcetCycles")+" "+nbDistrictsWithCycles);

		
		} catch (InterruptedException e) {

			returnCode = "interrupted";
System.out.println("interrupted");
		}
		
		return allocSeats;
	}

	public String getParliamentName() {
		return (language.getString("uninominal","CondorcetWinner")+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}

	
}// end class definition