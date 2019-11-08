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

public class BordaCriterion extends Parliament {

	// constructor

	public BordaCriterion(SimulationRepository sr) {
		super(sr);
		String[] vals = {"BordaCriterion"};
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
System.out.println(language.getString("uninominal","BordaCriterion")+" created");
	}

	public String getParliamentName() {
		return (language.getString("uninominal","BordaCriterion")+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}

	// methods
	// method to allocate the seats (current method)
	// in each district, the winning party is the one who has the most votes
	public HashMap <Integer,Integer> findAllocationOfSeats()
		{
		HashMap <Integer,Integer> mapOfSeats=new HashMap<Integer,Integer>();
		// for each district
		try {
			for (int c=0;c<listOfUninominalDistricts.size();++c)
				{
				Thread.sleep(0);
				progressBar.setValue(c);
				DistrictUninominal district=(DistrictUninominal)listOfUninominalDistricts.get(c);
System.out.println("DISTRICT "+district.getNameOfDistrictU());
				LinkedList <Voter> votersInDistrict=district.getVoters();
				// for each party, find the ranking for the voters
				HashMap <Integer,Integer> rankParty=new HashMap<Integer,Integer>();
				for (int p=0;p<arrayOfParties.size();++p) {
					Party aParty=(Party)arrayOfParties.get(p);
					int nameOfParty=aParty.getName();
System.out.println("considering party "+nameOfParty);
					int positionParty=0;
					// for each voter
					for (int e=0;e<votersInDistrict.size();++e) {
						Voter aVoter=votersInDistrict.get(e);
						int increment=aVoter.getPositionOfParty(new Integer(nameOfParty));
//System.out.println("\tvoter "+aVoter.getNameVoter()+": position of party: "+increment);
						positionParty+=increment;
					}
System.out.println("\t\thence rank of party is: "+positionParty);
					rankParty.put(new Integer(nameOfParty),new Integer(positionParty));
				}
				// find the party with the minimum ranking in the map
				int minRanking=getMin(rankParty);
System.out.println("the minimum ranking is "+minRanking);
				Set <Integer> mapKeys=rankParty.keySet();
				Iterator <Integer> i=mapKeys.iterator();
				LinkedList <Integer> partiesWithMinimumRank=new LinkedList<Integer>();
				while (i.hasNext()) {
					Integer nameOfParty=i.next();
					int rank=(rankParty.get(nameOfParty)).intValue();
					if (rank==minRanking) {
System.out.println("aParty "+nameOfParty.intValue()+" has the minimum ranking");
						partiesWithMinimumRank.add(nameOfParty);
					}
				}
				// in the map mapOfSeats: if the winning party already has an entry
				// add one to the number of seats. If it does not have an entry, add it and
				// put 1 as value for the seats.
				// if there is more than one party....
				Integer nameOfParty;
				if (partiesWithMinimumRank.size()==1){
					nameOfParty=partiesWithMinimumRank.getFirst();
				} else {
					
////////////					// case where more than one party has the same number of votes
////////////					// (unlikely in reality, but possible in simulations with few voters)
////////////					// give the seat to the party which is ranked last least often in the voters' preferences
////////////					// for this, find the rank of each party under consideration for each voter, keep it if this
////////////					// rank equals the number of parties (means it is last) and finish the loop (the other parties will
////////////					// have higher ranks, so no need to find it).
////////////					// find the party with the smallest number of last rankings. If more than one, choose at random amongst them.
////////////					HashMap lastRankings = new HashMap();
////////////					Iterator e = votersInDistrict.iterator();
////////////					while (e.hasNext()) {
////////////						Voter v = (Voter)e.next();
////////////System.out.println("voter "+v.getNameVoter());
////////////						Iterator p = partiesWithMinimumRank.iterator();
////////////						while (p.hasNext()) {
////////////							Integer partyName = (Integer)p.next();
////////////							int rank = v.getPositionOfParty(partyName);
////////////System.out.println("position of party "+partyName+" is "+rank+" worst is "+(arrayOfParties.size()-1));
////////////							if (rank == arrayOfParties.size()-1) {
////////////								updateMap(lastRankings,partyName,1);
////////////							}
////////////						}
////////////					}// end loop on voters in district
////////////System.out.println("lastRankings has "+lastRankings.size()+" elements");
////////////					LinkedList minRankings = getListOfMinElements(lastRankings);
////////////					if (minRankings.size()==1) {
////////////						nameOfParty = (Integer)minRankings.getFirst();
////////////					} else {
////////////						int rdm = generator.nextInt(minRankings.size());
////////////						nameOfParty=(Integer)minRankings.get(rdm);
////////////					}
					// case where more than one party has the same number of votes
					// (unlikely in reality, but possible in simulations with few voters)
					// for each voter, give 1 to the party which is ranked highier in his order of preferences
					// Choose the party with the highest rank (or at random if still equal after this)
					HashMap <Integer,Integer> lastRankings = new HashMap<Integer,Integer>();
					Iterator <Voter> e = votersInDistrict.iterator();
					while (e.hasNext()) {
						Voter v = e.next();
System.out.println("voter "+v.getNameVoter());
						Iterator <Integer> p = partiesWithMinimumRank.iterator();
						int minRank = arrayOfParties.size()+1;
						Integer minParty = new Integer(0);
						while (p.hasNext()) {
							Integer partyName = p.next();
							int rank = v.getPositionOfParty(partyName);
							if (rank<=minRank) {
								minRank = rank;
								minParty = partyName;
							}
System.out.println("position of party "+partyName+" is "+rank+" worst is "+minRank);
						}
						updateMap(lastRankings,minParty,1);
					}// end loop on voters in district
System.out.println("lastRankings has "+lastRankings.size()+" elements");
					LinkedList <Integer> maxRankings = getListOfMaxElements(lastRankings);
					if (maxRankings.size()==1) {
						nameOfParty = maxRankings.getFirst();
					} else {
						int rdm = generator.nextInt(maxRankings.size());
						nameOfParty=maxRankings.get(rdm);
					}


				}
				// update mapOfSeats
				mapOfSeats=updateMap(mapOfSeats,nameOfParty,1);
				}  // end of loop on colleges
	
				// check the map mapOfSeats, and add to it the parties which have no seats in the parliament
				// with a value of 0 (necessary to compute the indice di rappresentativita)
				mapOfSeats=completeMap(mapOfSeats);
			} catch (InterruptedException e) {
				returnCode = "interrupted";
System.out.println("Borda interrupted");
			}

		return(mapOfSeats);
		}


	}