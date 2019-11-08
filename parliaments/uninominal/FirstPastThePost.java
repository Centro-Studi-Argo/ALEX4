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

public class FirstPastThePost extends Parliament {
	HashMap <Integer,Integer> vp = new HashMap <Integer,Integer> ();

	// constructor

	public FirstPastThePost(SimulationRepository sr) {
		super(sr);
		String[] vals = {"FirstPastThePost"};
		parliamentKey = makeKey(vals);	
		ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
		progressBar.setMaximum(((Integer)simulationRepository.getGeneralParameters().get("numberUninominalDistricts")).intValue());
		
		if (simulationRepository.containsParliament(parliamentKey)) {
			allocationOfSeats = simulationRepository.loadParliament(parliamentKey);
		} else {
			allocationOfSeats = findAllocationOfSeats();
			simulationRepository.saveParliament(parliamentKey,this);
		}
System.out.println(language.getString("uninominal","FirstPastThePost")+" created");
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentName() {
		return (language.getString("uninominal","FirstPastThePost")+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}

	// methods

	// method to allocate the seggi (current method)
	// in each district, the winning party is the one who has the most votes
	public HashMap <Integer,Integer> findAllocationOfSeats()
		{
		HashMap  <Integer,Integer> mapOfSeats=new HashMap <Integer,Integer> ();
		try {
			Thread.sleep(0);
			// for each district
			for (int c=0;c<listOfUninominalDistricts.size();++c) {
				progressBar.setValue(c);
				// select the current district
				DistrictUninominal district=listOfUninominalDistricts.get(c);
	System.out.println("current district is "+district.getNameOfDistrictU());
				// get the list of voters	for this district
				LinkedList <Voter> votersInDistrict=district.getVoters();
				// count the number of votes for each party
				HashMap  <Integer,Integer> votesOfParties=countVotesParty(arrayOfParties,votersInDistrict);
				// save the votes for each party
				Set <Integer> votekeys = votesOfParties.keySet();
				Iterator <Integer> iv = votekeys.iterator();
				while (iv.hasNext()) {
					Integer pa = iv.next();
					int val = (votesOfParties.get(pa)).intValue();
					vp = updateMap(vp,pa,val);
				}
				// find the party with the most votes
				LinkedList <Integer> partyWithMostVotes=findPartyWithMostVotes(votesOfParties);
				// in the map mapOfSeats: if the winning party already has an entry
				// add one to the number of seats. If it does not have an entry, add it and
				// put 1 as value for the seats.
				// if there is more than one party....
				Integer partyName = new Integer(0);
				if (partyWithMostVotes.size()==1)
					{
					partyName=(Integer)partyWithMostVotes.getFirst();
					}
				else
					{
//System.out.println("***************");
					// case where more than one party has the same number of votes
					// (unlikely in reality, but possible in simulations with few voters)
					// in this case, give the seat to the party with the highest share
					// in the whole population.
					// If there are more than one, get a number at random between 0 and (nbOfParties)-1
					// and take the party at the corresponding index in the list.
					HashMap  <Integer,Integer> sharesOfParties = new HashMap <Integer,Integer> ();
					Iterator <Integer>s = partyWithMostVotes.iterator();
//System.out.println("there are "+arrayOfParties.size()+" parties in total, and there are "+partyWithMostVotes.size()+" parties with most votes");
					while (s.hasNext()) {
						Integer indexOfParty = s.next();
//System.out.println("index (that is, name) of party is "+indexOfParty);
						Party party = arrayOfParties.get(indexOfParty.intValue()-1);
//System.out.println("partyname "+party+" name of party "+party.getName()+" has share "+party.getShare());
						sharesOfParties.put(indexOfParty,new Integer(party.getVotes()));
					}
//System.out.println("so sharesOfParties is created and has "+sharesOfParties.size()+" elements");
					LinkedList <Integer> bestParties = getListOfMaxElements(sharesOfParties);
//System.out.println("there are "+bestParties.size()+" parties with the highest share");
					if (bestParties.size()==1) {
//System.out.println("return it");
						partyName = bestParties.getFirst();
					} else {
						int rdm = generator.nextInt(bestParties.size());
//System.out.println("choose the "+rdm+"th one at random");
						partyName = bestParties.get(rdm);
					}
					}
				// update mapOfSeats
				mapOfSeats=updateMap(mapOfSeats,partyName,1);
				
			}  // end of loop on districts
			// check the map mapOfSeats, and add to it the parties which have no seats in the parliament
			// with a value of 0 (necessary to compute the indice di rappresentativita)
System.out.println("allocazioneSeggi contains "+mapOfSeats.size()+" parties");
System.out.println("votes per party before setting"+votesPerParty.toString());
System.out.println("vp "+vp.toString());
			votesPerParty = vp;
System.out.println("votes per party after setting"+votesPerParty.toString());
			mapOfSeats=completeMap(mapOfSeats);
		} catch (InterruptedException e) {
			returnCode = "interrupted";
System.out.println("interrupted");
		}
		return(mapOfSeats);
		}

		
	}// end class