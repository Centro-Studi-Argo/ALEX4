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
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.Toolkit;
import java.text.*;
import java.io.*;
import java.lang.reflect.*;
import java.beans.*;

import classesWrittenByOthers.*;
import votingObjects.*;
import parliaments.*;
import actions.*;

public class StrategicVote extends Parliament {
	HashMap <Integer,Integer> vp = new HashMap <Integer,Integer> ();

LinkedList <Coalition> listOfCoalitions = new LinkedList<Coalition>();
String nameOfGroupOfCoalitions = new String();
double kForDistance=0;
	// constructor
	public StrategicVote() {
	}
	
	public StrategicVote(SimulationRepository sr) {
		super(sr);
		kForDistance = setParameterDialog(kForDistance,language.getString("labels","weightDistance"),100);
System.out.println("k for distance: "+kForDistance);
		if (simulationRepository.containsParliament(parliamentKey)) {
			allocationOfSeats = simulationRepository.loadParliament(parliamentKey);
		} else {
			CreatorVotingObjects cvo = new CreatorVotingObjects (simulationRepository.getGeneralParameters(),arrayOfParties,null,simulationRepository);
			// do the coalitions exist?
			HashMap <Integer,LinkedList<Coalition>> mapOfCoalitions = simulationRepository.getMapOfGroupsOfCoalitions();
System.out.println("original from SR");
Iterator j=listOfCoalitions.iterator();
while (j.hasNext()) {
Coalition coal=(Coalition)j.next();
System.out.println(coal);
}
			cvo.makeListOfCoalitions(simulationRepository);
			listOfCoalitions = sr.getCoalitionToUse();
			Coalition coa = (Coalition)listOfCoalitions.getFirst();
			nameOfGroupOfCoalitions = coa.getNameOfGroupOfCoalitions();

			String[] vals = {"StrategicVote",language.getString("labels","weightDistanceShort")+"="+(new Double(kForDistance)).toString(),language.getString("labels","coalitions")+"="+nameOfGroupOfCoalitions};
			parliamentKey = makeKey(vals);	
System.out.println("parliament key: "+parliamentKey);
			// has the user specified the distance between the parties?
			double sumD = 0;
			while (sumD==0) {
				for (int i=0; i<arrayOfParties.size(); ++i) {
					Party party = arrayOfParties.get(i);
					sumD += party.getDistance();
				}
				if (sumD==0) { // ask the user to define the distances
					 cvo.makeDistances(arrayOfParties);
				}
			}
			// weight of the distance between the parties
			progressBar.setMinimum(0);
			progressBar.setMaximum(listOfVoters.size()+listOfUninominalDistricts.size());
System.out.println("find allocation of seats");
			allocationOfSeats = findAllocationOfSeats();
			simulationRepository.saveParliament(parliamentKey,this);
			setNoteToGraph(language.getString("labels","weightDistanceShort")+" "+kForDistance);
			setNoteToGraph(language.getString("labels","coalitions")+" "+nameOfGroupOfCoalitions);
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
		}
System.out.println(language.getString("uninominal","StrategicVote")+" created");

	}
	public String getParliamentName() {
		return (language.getString("uninominal","StrategicVote")+" - "+language.getString("labels","weightDistanceShort")+"="+kForDistance+" - "+language.getString("labels","coalitions")+"="+nameOfGroupOfCoalitions+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}
	public double getWeightDistance() {
		return kForDistance;
	}
	public String getNameOfGroupOfCoalitions() {
		return nameOfGroupOfCoalitions;
	}
	// abstract method that must be overridden by all parliaments
	public HashMap <Integer,Integer> findAllocationOfSeats() {
System.out.println("entering find allocation of seats for strategic vote");
		HashMap <Integer,Integer> mapOfSeats=new HashMap<Integer,Integer>();
		try {
			Thread.sleep(0);
			// for each district
			int rv = 0;
			for (int c=0;c<listOfUninominalDistricts.size();++c) {
//				progressBar.setValue(c);
				// select the current district
				DistrictUninominal district=(DistrictUninominal)listOfUninominalDistricts.get(c);
//System.out.println("current district is "+district.getNameOfDistrictU());
				// get the list of voters	for this district
				LinkedList <Voter> votersInDistrict=district.getVoters();
				// count the number of votes for each party
				HashMap <Integer,Integer> votesOfParties = new HashMap<Integer,Integer>();
				// for each voter: get preferred party, the coalition to which it belongs, and the main party
				// in the district for this coalition (compute it and save it in the coalition if it does not exists)
				// find the distance between preferred party and the main party
				// if the distance is 0, the voter will vote for this party
				// if not 0, compute p, get random number and see whether the voter will vote for its preferred party
				// (random number < p*100) or the principal party otherwise.
				Iterator <Voter> v = votersInDistrict.iterator();
				while (v.hasNext()) {
					Voter voter = v.next();
				
					progressBar.setValue(rv);
					
					int preferredPartyNme = voter.getFirstPartyPreference();
					Party preferredParty = arrayOfParties.get(preferredPartyNme-1);
//System.out.println("the voter prefers "+preferredPartyNme+" corresponds to "+preferredParty.getName()+" in arrayOfParties");
					// get the coalition to which the party belongs
					Coalition coa = getCoalitionForParty(listOfCoalitions,preferredParty);
//System.out.println("current coalition: "+coa);
					// get the main party in this coalition
					Party mainParty = getMainPartyInDistrictForCoalition(coa,district.getNameOfDistrictU(),votersInDistrict,arrayOfParties);
//System.out.println("main party: "+mainParty.getName());
					double absDist = Math.abs(mainParty.getDistance()-preferredParty.getDistance());
//System.out.println("preferred party "+preferredParty.getName());
					// Default preferred and main parties are the same
					Integer key = new Integer(preferredPartyNme);
					if (absDist>0) { // main and preferred party are different
						double valDist = (kForDistance*absDist)/100;
						valDist = (valDist>1) ? 1 : valDist;
						double p = 1 - valDist;
						int benchmark = (int)(p*100);
						int rdm = generator.nextInt(100);
						if (rdm <= benchmark) {// vote for main party
							key = new Integer(mainParty.getName());
						}
//System.out.println("p: "+p+" benchmark "+benchmark+" rdm "+rdm);
					}
//System.out.println("party for which he votes is party is "+key);
					if (votesOfParties.containsKey(key)) {
						Integer val = (Integer)votesOfParties.get(key);
//System.out.print("number of votes was "+val);
						int nb = val.intValue();
						++nb;
						val = new Integer(nb);
//System.out.println(" and becomes "+val);
						votesOfParties.remove(key);
						votesOfParties.put(key,val);
					} else {
						votesOfParties.put(key,new Integer(1));
					}
//System.out.println("--");
					++rv;
				}

				// save the votes for each party
				Set <Integer> votekeys = votesOfParties.keySet();
				Iterator <Integer> iv = votekeys.iterator();
				while (iv.hasNext()) {
					Integer pa = iv.next();
					int val = (votesOfParties.get(pa)).intValue();
					vp = updateMap(vp,pa,val);
				}
				// find the party with the most votes (= partito principale)
				LinkedList <Integer> partyWithMostVotes=findPartyWithMostVotes(votesOfParties);

				
				// in the map mapOfSeats: if the winning party already has an entry
				// add one to the number of seats. If it does not have an entry, add it and
				// put 1 as value for the seats.
				// if there is more than one party....
				Integer partyName = new Integer(0);
				if (partyWithMostVotes.size()==1)
					{
					partyName=partyWithMostVotes.getFirst();
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
					HashMap <Integer,Integer> sharesOfParties = new HashMap<Integer,Integer>();
					Iterator <Integer> s = partyWithMostVotes.iterator();
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
				++rv;
				
			}  // end of loop on districts
			// check the map mapOfSeats, and add to it the parties which have no seats in the parliament
			// with a value of 0 (necessary to compute the indice di rappresentativita)
//System.out.println("allocazioneSeggi contains "+mapOfSeats.size()+" parties");
			votesPerParty = vp;
			mapOfSeats=completeMap(mapOfSeats);
//System.out.println("ready to return");
		} catch (InterruptedException e) {
			returnCode = "interrupted";
//System.out.println("interrupted");
		}
		return(mapOfSeats);
	}


	private Coalition getCoalitionForParty(LinkedList <Coalition> coalitions,Party party) {
		Iterator <Coalition> c = coalitions.iterator();
		while (c.hasNext()) {
			Coalition coa = c.next();
			if (coa.containsParty(party)) {
				return(coa);
			}
		}
		return null;
	}

	private Party getMainPartyInDistrictForCoalition(Coalition coalition,int nameOfDistrict,LinkedList <Voter> voters,ArrayList <Party> parties) {
		// in the mainParties of coalition, is there an entry for the district?
		// if so, return the party
		// otherwise, create the list of most votes for all the parties in the coalition for the voters in list
		// get the party with the most votes (get most votes, keep only parties in the coalitions, get max of that
//System.out.println("looking for the main party for district "+nameOfDistrict);
		Party party = coalition.getMainParty(nameOfDistrict);
//System.out.println("the main party is "+party);
		if (party  != null) {
//System.out.println("\tcoalition already contains main party: "+party.getName());
			return party;
		} else {
//System.out.println("\tfind main party");
			// get parties with most votes in the district
			HashMap <Integer,Integer> partiesWithMostVotes = countVotesParty(coalition.getArrayOfParties(),voters);
			Integer partyWithMostVotes=(findPartyWithMostVotes(partiesWithMostVotes)).getFirst();
			party = (Party)parties.get((partyWithMostVotes.intValue()-1));
			coalition.addMainParty(nameOfDistrict,party);
//System.out.println("\tparty with most votes is "+partyWithMostVotes+" corresponds to "+party.getName()+" in array");
			return(party);
		}
		
	}
	
}// end class definition