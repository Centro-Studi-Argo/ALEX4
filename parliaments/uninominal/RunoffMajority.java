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

public class RunoffMajority extends Parliament {

	HashMap <Integer,Integer> vp = new HashMap <Integer,Integer> ();

	// constructor

	public RunoffMajority(SimulationRepository sr) {
		super(sr);
		String[] vals = {"RunoffMajority"};
		parliamentKey = makeKey(vals);	
		ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
		progressBar.setMaximum(((Integer)simulationRepository.getGeneralParameters().get("numberUninominalDistricts")).intValue());
		
		if (simulationRepository.containsParliament(parliamentKey)) {
			allocationOfSeats = simulationRepository.loadParliament(parliamentKey);
		} else {
			allocationOfSeats = findAllocationOfSeats();
			simulationRepository.saveParliament(parliamentKey,this);
		}
System.out.println(language.getString("uninominal","RunoffMajority")+" created");
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
	}

	public String getParliamentName() {
		return (language.getString("uninominal","RunoffMajority")+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}

	
	// methods

	// method to allocate the seats (current method)
	// in each district, the winning party is the one who has the most votes
	public HashMap <Integer,Integer> findAllocationOfSeats() {
		HashMap <Integer,Integer> mapOfSeats=new HashMap<Integer,Integer>();
		try {
			// for each district
			for (int c=0;c<listOfUninominalDistricts.size();++c) {
				Thread.sleep(0);
				progressBar.setValue(c);
				// select the current district
				DistrictUninominal district=(DistrictUninominal)listOfUninominalDistricts.get(c);
	System.out.println("current district is "+district.getNameOfDistrictU());
				// get the list of voters	for this district
				LinkedList <Voter> votersInDistrict=district.getVoters();
				// count the number of votes for each party
				HashMap <Integer,Integer> votesOfParties=countVotesParty(arrayOfParties,votersInDistrict);
				// save the votes for each party
				Set <Integer> votekeys = votesOfParties.keySet();
				Iterator <Integer> iv = votekeys.iterator();
				while (iv.hasNext()) {
					Integer pa = iv.next();
					int val = (votesOfParties.get(pa)).intValue();
					vp = updateMap(vp,pa,val);
				}
				// find the highest vote
				int maxVotes=getMax(votesOfParties);
	System.out.println("maxVotes is "+maxVotes);
				int absMaj=(votersInDistrict.size()/2)+1;
	System.out.println("absolute majority is "+absMaj);
				// if maxVotes is larger than absolute majority ((votersInDistrict.size()/2)+1)
				// then the party with this value is elected (choose at random if there is more
				// than one)
				// else, look at second round
				LinkedList <Integer> partiesWithAbsoluteMajority=new LinkedList<Integer>();
				if (maxVotes>=absMaj) {
System.out.println("maxVotes is larger than the absolute majority");
					// get all the parties with the maximum vote
					for (int p=0;p<arrayOfParties.size();++p) {
System.out.println("p "+p);
						Party party=arrayOfParties.get(p);
						int nameOfParty=party.getName();
						int votesOfParty=(votesOfParties.get(new Integer(nameOfParty))).intValue();
						if (votesOfParty>=absMaj) {
System.out.println("add party "+nameOfParty+" to partiesWithAbsoluteMajority");
							partiesWithAbsoluteMajority.add(new Integer(nameOfParty));
						}
					}
					// add one seat for the winning party in mapOfSeats
					// (choose at random if there are more than one)
					Integer nameOfParty = new Integer(0);
					if (partiesWithAbsoluteMajority.size()==1) {
						nameOfParty=partiesWithAbsoluteMajority.getFirst();
System.out.println("only one party, party "+nameOfParty.intValue()+" is elected");
					} else {

						HashMap <Integer,Integer> sharesOfParties = new HashMap<Integer,Integer>();
						Iterator s = partiesWithAbsoluteMajority.iterator();
						while (s.hasNext()) {
							Integer indexOfParty = (Integer)s.next();
							Party party = arrayOfParties.get(indexOfParty.intValue()-1);
System.out.println("partyname "+party+" name of party "+party.getName()+" has share "+party.getShare());
							sharesOfParties.put(indexOfParty,new Integer(party.getVotes()));
							LinkedList <Integer> bestParties = getListOfMaxElements(sharesOfParties);
System.out.println("there are "+bestParties.size()+" parties with the highest share");
							if (bestParties.size()==1) {
System.out.println("return it");
								nameOfParty = bestParties.getFirst();
							} else {
								int rdm = generator.nextInt(bestParties.size());
System.out.println("choose the "+rdm+"th one at random");
								nameOfParty = bestParties.get(rdm);
							}
						}

					}
					mapOfSeats=updateMap(mapOfSeats,nameOfParty,1);
				} else {
System.out.println("SECONDO TURNO");
					// second round: get the two parties with the highest votes (choose at random
					// if there are more than 2 parties with highest vote, or more than one party
					// with second highest vote).
					// Check again the highest vote for all the voters for these two parties
					// return the one party with the highest vote and update allocation of seats
					// (again choosing at random if both parties are equal).
					// first thing, create partiesWithMostVotes with all the parties having the highest vote
					// (and remove them from votesOfParties)
					LinkedList <Integer>partiesWithMostVotes = new LinkedList<Integer>();
					for (int p=0;p<arrayOfParties.size();++p) {
						Party party=arrayOfParties.get(p);
						int nameOfParty=party.getName();
						int votesOfParty=(votesOfParties.get(new Integer(nameOfParty))).intValue();
						if (votesOfParty==maxVotes) {
							partiesWithMostVotes.add(new Integer(nameOfParty));
							votesOfParties.remove(new Integer(nameOfParty));
System.out.println("partiesWithMostVotes contains party "+nameOfParty);
						}
					}
					// if there are more than 2, eliminate the surplus
					if (partiesWithMostVotes.size()>=2) {
System.out.println("at least 2 parties with highest vote");
						if (partiesWithMostVotes.size()>2) {
System.out.println("more than 2 parties with highest vote, remove those with smallest share in population ");
							HashMap <Integer,Integer> sharesOfParties = new HashMap<Integer,Integer>();
							Iterator <Integer> s = partiesWithMostVotes.iterator();
							while (s.hasNext()) {
								Integer indexOfParty = s.next();
								Party party = arrayOfParties.get(indexOfParty.intValue()-1);
System.out.println("partyname "+party+" name of party "+party.getName()+" has share "+party.getShare());
								sharesOfParties.put(indexOfParty,new Integer(party.getVotes()));
							}
							LinkedList <Integer> smallestParties = sortMap(sharesOfParties,"ascending",null);
							int partyIndex = 0;
							while (partiesWithMostVotes.size()>2) {
								partiesWithMostVotes.remove(new Integer(partyIndex));
								++partyIndex;
							}
						}
					} else {
						// get the next highest votes: the maximum of the values in the remaining votesOfParties
						maxVotes=getMax(votesOfParties);
System.out.println("second highest vote is "+maxVotes);
						Set <Integer> namesOfParties=votesOfParties.keySet();
						Iterator <Integer> i=namesOfParties.iterator();
						LinkedList <Integer> partiesInSecondPlace=new LinkedList<Integer>();
						while (i.hasNext()) {
							Integer nameOfParty=i.next();
							int votesOfParty=(votesOfParties.get(nameOfParty)).intValue();
							if (votesOfParty==maxVotes) {
System.out.println("party "+nameOfParty.intValue()+" is considered for secound round");
								partiesInSecondPlace.add(nameOfParty);
							}
						}
						// if there is more than 1, choose one at random
						// and add it to partiesWithMostVotes
						if (partiesInSecondPlace.size()==1) {
System.out.println("1 party to consider");
							partiesWithMostVotes.add(partiesInSecondPlace.getFirst());
						} else {

							HashMap <Integer,Integer> sharesOfParties = new HashMap<Integer,Integer>();
							Iterator <Integer> s = partiesInSecondPlace.iterator();
							while (s.hasNext()) {
								Integer indexOfParty = s.next();
								Party party = arrayOfParties.get(indexOfParty.intValue()-1);
System.out.println("partyname "+party+" name of party "+party.getName()+" has share "+party.getShare());
								sharesOfParties.put(indexOfParty,new Integer(party.getVotes()));
							}
							LinkedList <Integer> smallestParties = sortMap(sharesOfParties,"ascending",null);
							int partyIndex = 0;
							while (partiesInSecondPlace.size()>1) {
								partiesInSecondPlace.remove(new Integer(partyIndex));
								++partyIndex;
							}
							partiesWithMostVotes.add(partiesInSecondPlace.getFirst());
						}
					}
System.out.println("NOW, CHOOSE THE BEST BETWEEN PARTIES WITH MOST VOTES");
					// now, we have the two main parties to consider. We need to find which
					// are their respective rankings in the voters preferences.
					// use the function getPreferredParty from Voter
					votesOfParties=countVotesSecondRound(partiesWithMostVotes,votersInDistrict);
					// find the maximum of the votes
					maxVotes=getMax(votesOfParties);
System.out.println("at the second turn, the maximum of votes is "+maxVotes);
					Set <Integer> mapKeys=votesOfParties.keySet();
					Iterator <Integer> it=mapKeys.iterator();
					LinkedList <Integer> partiesWithMaxOfVotes=new LinkedList<Integer>();
					while (it.hasNext()) {
						Integer nameOfParty=it.next();
						int vote=(votesOfParties.get(nameOfParty)).intValue();
						if (vote==maxVotes) {
System.out.println("party "+nameOfParty.intValue()+" has the maximum of votes");
							partiesWithMaxOfVotes.add(nameOfParty);
						}
					}
					// in the map mapOfSeats: if the winning party already has an entry
					// add one to the number of seats. If it does not have an entry, add it and
					// put 1 as value for the seats.
					// if there is more than one party....
					Integer nameOfParty;
					if (partiesWithMaxOfVotes.size()==1) {
						nameOfParty=partiesWithMaxOfVotes.getFirst();
System.out.println("only one party with max of votes: "+nameOfParty);
					} else {
System.out.println("more than one party with max of votes");
						// case where more than one party has the same number of votes
						// (unlikely in reality, but possible in simulations with few voters)
						// in this case, get a number at random between 0 and (nbOfParties)-1
						// and take the party at the corresponding index in the list.
						HashMap <Integer,Integer> votesFirstRound = new HashMap<Integer,Integer>();
						Iterator <Integer> s = partiesWithMaxOfVotes.iterator();
						while (s.hasNext()) {
							Integer indexOfParty = s.next();
							votesFirstRound.put(indexOfParty,(Integer)votesOfParties.get(indexOfParty));
						}
						LinkedList <Integer> bestParties = getListOfMaxElements(votesFirstRound);
						if (votesFirstRound.size()==1) {
							nameOfParty = bestParties.getFirst();
						} else {
							HashMap <Integer,Integer> sharesOfParties = new HashMap<Integer,Integer>();
							s = bestParties.iterator();
							while (s.hasNext()) {
								Integer indexOfParty = s.next();
								Party party = arrayOfParties.get(indexOfParty.intValue()-1);
System.out.println("partyname "+party+" name of party "+party.getName()+" has share "+party.getShare());
								sharesOfParties.put(indexOfParty,new Integer(party.getVotes()));
							}
							bestParties = getListOfMaxElements(sharesOfParties);
System.out.println("there are "+bestParties.size()+" parties with the highest share");
							if (bestParties.size()==1) {
System.out.println("return it");
								nameOfParty = bestParties.getFirst();
							} else {
								int rdm = generator.nextInt(bestParties.size());
System.out.println("choose the "+rdm+"th one at random");
								nameOfParty = bestParties.get(rdm);
							}
						}
					}// end else parties with most votes has more than 2 elements
				// update mapOfSeats
System.out.println("Eventually, name of party is " + nameOfParty);
				mapOfSeats=updateMap(mapOfSeats,nameOfParty,1);
				}// end if maxVotes else = end secondo turno
System.out.println("mapOfSeats contains "+mapOfSeats.size()+" parties");
				
			}// end loop on colleges
			votesPerParty = vp;
			mapOfSeats=completeMap(mapOfSeats);
		} catch (InterruptedException e) {
			returnCode = "interrupted";
		}

		return(mapOfSeats);
		}

	HashMap <Integer,Integer> countVotesSecondRound(LinkedList <Integer> partiesWithMostVotes, LinkedList <Voter> votersInDistrict)
		{
		HashMap <Integer,Integer> votesOfParties=new HashMap<Integer,Integer>();
		// per ogni aVoter
		for (int e=0;e<votersInDistrict.size();++e)
			{
			Voter aVoter=votersInDistrict.get(e);
			Integer preferredParty=aVoter.getPreferredParty(partiesWithMostVotes);
			boolean contains=votesOfParties.containsKey(preferredParty);
			if (contains==true)
				{
				int numberOfVotes=(votesOfParties.get(preferredParty)).intValue();
				++numberOfVotes;
				votesOfParties.remove(preferredParty);
				votesOfParties.put(preferredParty,new Integer(numberOfVotes));
				}
			else
				{
				votesOfParties.put(preferredParty,new Integer(1));
				}
			}
		return (votesOfParties);
		}
	}