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
import actions.*;

public class MixedMemberII extends Parliament {
	HashMap <Integer,Integer> vpm = new HashMap <Integer,Integer> ();
	HashMap <Integer,Integer> vpp = new HashMap <Integer,Integer> ();

	// global variables for the class (other than those of the super class)
	int valuePercProp = 5;
	int thisAllocSeats = 1;
	int maxValue = 100;
	boolean withStrategic = false;
	double kForDistance=0;
	private double threshold = 5;

	private HashMap <Integer,Integer> votesNotUsed;
	LinkedList <Coalition> listOfCoalitions = new LinkedList<Coalition>();
	String nameOfGroupOfCoalitions = new String();

	// constructor

	public MixedMemberII(SimulationRepository sr) {
		super(sr);
System.out.println("==============\nCONSTRUCTOR FOR MIXEDMEMBERII - no strategic\n======================");
		// value attributed with the proportional system (max = maxValue)
		valuePercProp = setParameterDialog(valuePercProp,language.getString("messages","mixedMember_percProp"),maxValue);

		String[] vals = {"MixedMemberII",(new Integer(valuePercProp)).toString()} ;
		parliamentKey = makeKey(vals);	
		
		ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
		progressBar.setMaximum(4);
		
//		if (simulationRepository.containsParliament(parliamentKey)) {
//			allocationOfSeats = simulationRepository.loadParliament(parliamentKey);
//		} else {
			allocationOfSeats = findAllocationOfSeats();
			simulationRepository.saveParliament(parliamentKey,this);
			setNoteToGraph(language.getString("messages","mixedMember_percProp")+" "+valuePercProp);
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
//		}
System.out.println(language.getString("uninominal","MixedMemberII")+" created");
	}

	public MixedMemberII(SimulationRepository sr, boolean withStrategic) {
		super(sr);
System.out.println("==============\nCONSTRUCTOR FOR MIXEDMEMBERII - with strategic\n======================");
		this.withStrategic = withStrategic;
		// value attributed with the proportional system (max = maxValue)
		valuePercProp = setParameterDialog(valuePercProp,language.getString("messages","mixedMember_percProp"),maxValue);

		if (thisAllocSeats == 1) {
	 		kForDistance = setParameterDialog(kForDistance,language.getString("labels","weightDistance"),100);

			ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
			progressBar.setMaximum(3);
			
System.out.println("compute parliament:");
			if (withStrategic==true) {
				CreatorVotingObjects cvo = new CreatorVotingObjects (simulationRepository.getGeneralParameters(),arrayOfParties,null,simulationRepository);
				
				// do the coalitions exist?
				HashMap <Integer,LinkedList<Coalition>> mapOfCoalitions = simulationRepository.getMapOfGroupsOfCoalitions();
	//System.out.println("original from SR");
	//Iterator j=listOfCoalitions.iterator();
	//while (j.hasNext()) {
	//Coalition coal=(Coalition)j.next();
	//System.out.println(coal);
	//}
				cvo.makeListOfCoalitions(simulationRepository);
				listOfCoalitions = sr.getCoalitionToUse();
				Coalition coa = (Coalition)listOfCoalitions.getFirst();
				nameOfGroupOfCoalitions = coa.getNameOfGroupOfCoalitions();
	
				// has the user specified the distance between the parties?
				double sumD = 0;
				while (sumD==0) {
					for (int i=0; i<arrayOfParties.size(); ++i) {
						Party party = (Party)arrayOfParties.get(i);
						sumD += party.getDistance();
					}
					if (sumD==0) { // ask the user to define the distances
						 cvo.makeDistances(arrayOfParties);
					}
				}
		}

			allocationOfSeats = findAllocationOfSeats();
System.out.println("3 - now valuePercProp is "+valuePercProp+" and kForDistance is "+kForDistance);
System.out.println("done allocation of seats");
			if (withStrategic==false) {
				String[] vals = {"MixedMemberII",(new Integer(valuePercProp)).toString()};
				parliamentKey = makeKey(vals);	
			} else {
				String[] vals = {"MMII-SV","MixedMemberII",(new Integer(valuePercProp)).toString(),language.getString("labels","weightDistanceShort")+"="+(new Double(kForDistance)).toString(),language.getString("labels","coalitions")+"="+nameOfGroupOfCoalitions};
				parliamentKey = makeKey(vals);	
			}			
			simulationRepository.saveParliament(parliamentKey,this);
			if (withStrategic==true) {
				setNoteToGraph(language.getString("labels","weightDistanceShort")+" "+kForDistance);
				setNoteToGraph(language.getString("labels","coalitions")+" "+nameOfGroupOfCoalitions);
			}
		}
	}

	
	public String getParliamentName() {
		if (withStrategic==false) {
			return (language.getString("uninominal","MixedMemberII")+" - "+valuePercProp+"%"+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
		} else {
			return (language.getString("uninominal","MixedMemberII")+" - "+valuePercProp+"%"+language.getString("uninominal","StrageticVote")+" - "+kForDistance+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
		}
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}

	// methods

	// method to allocate the seats (current method)
	// in each district, the winning party is the one who has the most votes
	public HashMap <Integer,Integer> findAllocationOfSeats()
		{
System.out.println("MixedMember II find allocation of seats");
		threshold = setParameterDialog(threshold,language.getString("messages","thresholdProportional"),maxValue);
		String[] vals = {"ThresholdProportional",(new Double(threshold)).toString()}; 
		
		HashMap <Integer,Integer>mapOfSeats=new HashMap<Integer,Integer>();
		try {
			votesNotUsed = new HashMap<Integer,Integer>();
			// to find the allocazioneSeggi in this mixed system, multiply the allocazioneSeggi with
			// proportional system by the value of percProp, and the allocazioneSeggi with the maggioritario
			// by 100-value of percProp; then sum the two, making sure the total is nbSeggi.
			int nbSeatsProportional=Math.round(((float)sizeOfParliament*valuePercProp)/100);
//	System.out.println("there are "+nbSeatsProportional+" seats allocated by proportional system");
			int nbSeatsFirstPastThePost=sizeOfParliament-nbSeatsProportional;
//	System.out.println("there are "+nbSeatsFirstPastThePost+" seats allocated by majority system");
			Thread.sleep(0);
			progressBar.setValue(1);
			// do first the parliament with the maggioriatario system, which will fill in the votesNotUsed map
//	System.out.println("Misto con scorporo: get a parliament with the allocationOfSeats_FirstPastThePost ");
//	System.out.println("votesNotUsed has "+votesNotUsed.size()+" elements");
			HashMap <Integer,Integer>allocationOfSeats_FirstPastThePost = new HashMap<Integer,Integer>();
			if (withStrategic==false) {
System.out.println("strategic is false: get first past the post");
				allocationOfSeats_FirstPastThePost = allocationOfSeats_FirstPastThePost ();
			} else {
System.out.println("strategic is true: get first past the post with strategic");
				allocationOfSeats_FirstPastThePost = allocationOfSeats_FirstPastThePost_withStrategic ();
			}
System.out.println("vp after maj"+vpm.toString());
			Thread.sleep(0);
			progressBar.setValue(2);
			// then do the parliament with the proportional system, using the votesNotUsed map
System.out.println("Misto con scorporo: get a parliament with the allocationOfSeats_OneDistrictProportional ");
System.out.println("votes not used has "+votesNotUsed.size()+" elements");
			// remove from votes not used the parties which fall under the threshold
			Set <Integer> keysNotUsed = votesNotUsed.keySet();
			Iterator <Integer> iknu = keysNotUsed.iterator();
			LinkedList <Integer> notConsidered = new LinkedList <Integer> ();
			while (iknu.hasNext()) {
				Integer pname = iknu.next();
				Party aparty = arrayOfParties.get(pname-1);
				double quota = aparty.getShare();
				if (quota<threshold) {
					notConsidered.add(pname);
				}
			}
			if (notConsidered.size()>0) {
System.out.println("set threshold votes to 0");
				Iterator <Integer> inc = notConsidered.iterator();
				while (inc.hasNext()) {
					Integer pname = inc.next();
					votesNotUsed.remove(pname);
					votesNotUsed.put(pname,new Integer(0));
				}
			}
			// now get proportional allocation of seats
			HashMap <Integer,Integer>allocationOfSeats_OneDistrictProportional = allocationOfSeats_OneDistrictProportional (votesNotUsed);
			Thread.sleep(0);
			progressBar.setValue(3);
			// then weigh the two parliaments so that the proportion of each system is respected
			allocationOfSeats_OneDistrictProportional = getProportion(allocationOfSeats_OneDistrictProportional ,nbSeatsProportional,sizeOfParliament);
			allocationOfSeats_FirstPastThePost = getProportion(allocationOfSeats_FirstPastThePost ,nbSeatsFirstPastThePost,sizeOfParliament);
			Thread.sleep(0);
			progressBar.setValue(4);
			mapOfSeats  = sumMaps(allocationOfSeats_OneDistrictProportional ,allocationOfSeats_FirstPastThePost );
		} catch (InterruptedException e) {
			returnCode = "interrupted";
		}
		return(mapOfSeats);
		}
		
	public HashMap <Integer,Integer>allocationOfSeats_FirstPastThePost ()
		{
System.out.println("allocation of seats without strategic");
		HashMap <Integer,Integer>mapOfSeats=new HashMap<Integer,Integer>();
		// for each district
		for (int c=0;c<listOfUninominalDistricts.size();++c)
			{
//System.out.println("c is "+c);
			// select the current district
			DistrictUninominal district=(DistrictUninominal)listOfUninominalDistricts.get(c);
			// get the list of electors	for this district
			LinkedList<Voter> votersInDistrict=district.getVoters();
			// count the number of votes for each party
			HashMap <Integer,Integer>votesOfParties=countVotesParty(arrayOfParties,votersInDistrict);
			// save the votes for each party
			Set <Integer> votekeys = votesOfParties.keySet();
			Iterator <Integer> iv = votekeys.iterator();
			while (iv.hasNext()) {
				Integer pa = iv.next();
				int val = (votesOfParties.get(pa)).intValue();
				vpm = updateMap(vpm,pa,val);
			}
			// find the party with the most votes
			int maxVotes=getMax(votesOfParties);
//System.out.println("maxVotes "+maxVotes);
			LinkedList <Integer>partiesWithMaxVotes=findPartyWithMostVotes(votesOfParties);
			// in the map mapOfSeats: if the winning party already has an entry
			// add one to the number of seats. If it does not have an entry, add it and
			// put 1 as value for the seats.
			// if there is more than one party....
			Integer nameOfParty;
			if (partiesWithMaxVotes.size()==1)
				{
				nameOfParty=partiesWithMaxVotes.getFirst();
				}
			else
				{
				// case where more than one party has the same number of votes
				// (unlikely in reality, but possible in simulations with few electors)
				// in this case, get a number at random between 0 and (nbOfParties)-1
				// and take the party at the corresponding index in the list.
				int index=generator.nextInt(partiesWithMaxVotes.size());
				nameOfParty=partiesWithMaxVotes.get(index);
//System.out.println("more than one party with max number of votes.");
//System.out.println("random number is "+index+" aParty chosen is "+nameOfParty.intValue());
				}
//System.out.println("nameOfParty "+nameOfParty);
			// update mapOfSeats
			mapOfSeats=updateMap(mapOfSeats,nameOfParty,1);
//System.out.println("map of seats is updated");
			// save the votes obtained by the first party
			int votesFirstParty=(votesOfParties.get(nameOfParty)).intValue();
//System.out.println("the first party had "+votesFirstParty+" votes");
			// remove first party from votesOfParties and from partiesWithMaxVotes
			votesOfParties.remove(nameOfParty);
			partiesWithMaxVotes.remove(nameOfParty);
//System.out.println("the party is removed from votesOfParties and partiesWithMaxVotes");
			// find the unused votes for the first party: if there are still elements in the map partiesWithMaxVotes,
			// some parties had equal votes, hence the unused votes for the first party are 0
			// else, find the second party (the first in the new votesOfParties), and find its votes
			// unused votes for first party are: votesFirstParty-(votesSecondParty+1)
			int unusedVotesFirstParty=0;
			if (partiesWithMaxVotes.size()==0)
				{
				int votesSecondParty=getMax(votesOfParties);
//System.out.println("the second party has "+votesSecondParty+" votes");
				unusedVotesFirstParty=votesFirstParty-(votesSecondParty);
//System.out.println("Unused votes of first party are "+unusedVotesFirstParty);
				}
//System.out.println("end of if parties with max votes");
			// update the votesNotUsed map with the unused votes of the first party
//System.out.println("update votesNotUsed, party is "+nameOfParty+" and unusedVotesFirstParty is "+unusedVotesFirstParty);
//System.out.println("votesNotUsed (map) has "+votesNotUsed.size()+" elements");
			votesNotUsed=updateMap(votesNotUsed,nameOfParty,unusedVotesFirstParty);
//System.out.println("votesNotUsed is updated for first party");
			// update the votesNotUsed map with the votes of the remaining parties in votesOfParties
			votesNotUsed=updateMap(votesNotUsed,votesOfParties);
//System.out.println("votesNotUsed is updated for other parties");
			}  // end of loop on colleges
		votesNotUsed = completeMap(votesNotUsed);
		// check the map mapOfSeats, and add to it the parties which have no seats in the parliament
		// with a value of 0 (necessary to compute the indice di rappresentativita)
//System.out.println("allocationOfSeats_FirstPastThePost  contains "+mapOfSeats.size()+" parties");
System.out.println("vpm "+vpm.toString());
		votesPerParty = vpm;
		mapOfSeats=completeMap(mapOfSeats);
		return(mapOfSeats);
		}

	public HashMap<Integer,Integer> allocationOfSeats_FirstPastThePost_withStrategic ()
		{
System.out.println("allocation of seats with strategic");
		HashMap<Integer,Integer> mapOfSeats=new HashMap<Integer,Integer>();
		// for each district
		int rv = 0;
		for (int c=0;c<listOfUninominalDistricts.size();++c)
			{
//System.out.println("c is "+c);
			// select the current district
			DistrictUninominal district=(DistrictUninominal)listOfUninominalDistricts.get(c);
			// get the list of electors	for this district
			LinkedList <Voter>votersInDistrict=district.getVoters();
//System.out.print("votersInDistrict has "+votersInDistrict.size()+" elements");
			// count the number of votes for each party
			HashMap<Integer,Integer> votesOfParties = new HashMap<Integer,Integer>();
			// for each voter: get preferred party, the coalition to which it belongs, and the main party
			// in the district for this coalition (compute it and save it in the coalition if it does not exists)
			// find the distance between preferred party and the main party
			// if the distance is 0, the voter will vote for this party
			// if not 0, compute p, get random number and see whether the voter will vote for its preferred party
			// (random number < p*100) or the principal party otherwise.
			Iterator<Voter> v = votersInDistrict.iterator();
			while (v.hasNext()) {
				Voter voter = v.next();
			
				progressBar.setValue(rv);
				
				int preferredPartyNme = voter.getFirstPartyPreference();
				Party preferredParty = arrayOfParties.get(preferredPartyNme-1);
////System.out.println("the voter prefers "+preferredPartyNme+" corresponds to "+preferredParty.getName()+" in arrayOfParties");
				// get the coalition to which the party belongs
				Coalition coa = getCoalitionForParty(listOfCoalitions,preferredParty);
//System.out.println("current coalition: "+coa);
				// get the main party in this coalition
				Party mainParty = getMainPartyInDistrictForCoalition(coa,district.getNameOfDistrictU(),votersInDistrict,arrayOfParties);
//System.out.println("main party: "+mainParty.getName());
				double absDist = Math.abs(mainParty.getDistance()-preferredParty.getDistance());
//System.out.println("absolute distance "+absDist);
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

			// find the party with the most votes
			int maxVotes=getMax(votesOfParties);
//System.out.println("maxVotes "+maxVotes);
			LinkedList <Integer> partiesWithMaxVotes=findPartyWithMostVotes(votesOfParties);
			// in the map mapOfSeats: if the winning party already has an entry
			// add one to the number of seats. If it does not have an entry, add it and
			// put 1 as value for the seats.
			// if there is more than one party....
			Integer nameOfParty;
			if (partiesWithMaxVotes.size()==1)
				{
				nameOfParty=(Integer)partiesWithMaxVotes.getFirst();
				}
			else
				{
				// case where more than one party has the same number of votes
				// (unlikely in reality, but possible in simulations with few electors)
				// in this case, get a number at random between 0 and (nbOfParties)-1
				// and take the party at the corresponding index in the list.
				int index=generator.nextInt(partiesWithMaxVotes.size());
				nameOfParty=(Integer)partiesWithMaxVotes.get(index);
//System.out.println("more than one party with max number of votes.");
//System.out.println("random number is "+index+" aParty chosen is "+nameOfParty.intValue());
				}
//System.out.println("nameOfParty "+nameOfParty);
			// update mapOfSeats
			mapOfSeats=updateMap(mapOfSeats,nameOfParty,1);
//System.out.println("map of seats is updated");
			// save the votes obtained by the first party
			int votesFirstParty=(votesOfParties.get(nameOfParty)).intValue();
//System.out.println("the first party had "+votesFirstParty+" votes");
			// remove first party from votesOfParties and from partiesWithMaxVotes
			votesOfParties.remove(nameOfParty);
			partiesWithMaxVotes.remove(nameOfParty);
//System.out.println("the party is removed from votesOfParties and partiesWithMaxVotes");
			// find the unused votes for the first party: if there are still elements in the map partiesWithMaxVotes,
			// some parties had equal votes, hence the unused votes for the first party are 0
			// else, find the second party (the first in the new votesOfParties), and find its votes
			// unused votes for first party are: votesFirstParty-(votesSecondParty+1)
			int unusedVotesFirstParty=0;
			if (partiesWithMaxVotes.size()==0)
				{
				int votesSecondParty=getMax(votesOfParties);
//System.out.println("the second party has "+votesSecondParty+" votes");
				unusedVotesFirstParty=votesFirstParty-(votesSecondParty);
//System.out.println("Unused votes of first party are "+unusedVotesFirstParty);
				}
//System.out.println("end of if parties with max votes");
			// update the votesNotUsed map with the unused votes of the first party
System.out.println("update votesNotUsed, party is "+nameOfParty+" and unusedVotesFirstParty is "+unusedVotesFirstParty);
System.out.println("votesNotUsed (map) has "+votesNotUsed.size()+" elements");
			votesNotUsed=updateMap(votesNotUsed,nameOfParty,unusedVotesFirstParty);
System.out.println("votesNotUsed is updated for first party");
			// update the votesNotUsed map with the votes of the remaining parties in votesOfParties
			votesNotUsed=updateMap(votesNotUsed,votesOfParties);
System.out.println("votesNotUsed is updated for other parties");
			}  // end of loop on colleges
		votesNotUsed = completeMap(votesNotUsed);
		// check the map mapOfSeats, and add to it the parties which have no seats in the parliament
		// with a value of 0 (necessary to compute the indice di rappresentativita)
//System.out.println("allocationOfSeats_FirstPastThePost  contains "+mapOfSeats.size()+" parties");
		mapOfSeats=completeMap(mapOfSeats);
		return(mapOfSeats);
		}


		
	HashMap<Integer,Integer>allocationOfSeats_OneDistrictProportional (HashMap <Integer,Integer>votesNotUsed)
		{

		// from votesNotUsed, get votersNotUsed: a list of voters with that first party preference
		// then call the rounding method with the threshold

		LinkedList <Voter> votersNotUsed = new LinkedList <Voter> ();
		Set <Integer> keys = votesNotUsed.keySet();
		Iterator <Integer> i = keys.iterator();
		while (i.hasNext()) {
			Integer key = i.next();
			int votes = (votesNotUsed.get(key)).intValue();
			int r=0;
			Iterator <Voter> v = listOfVoters.iterator();
			while ((v.hasNext()) && (r<votes)) {
				Voter aVoter = v.next();
				if (aVoter.getFirstPartyPreference() == key.intValue()) {
					votersNotUsed.add(aVoter);
					++r;
				}
			}
		}
		Set roundingKeys = roundingMethods.keySet();
		rounding = setMethodDialog(roundingKeys,"multiDistrictPlurinominal");

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

			// list of parameters to invoke the method to find the seats in the district
			Object[] paraList = new Object[]{votersNotUsed,arrayOfParties,paramOfMethod,new Double(threshold),new Integer(sizeOfParliament),"nation"};
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
		return mapOfSeats;


		
////		HashMap <Integer,Integer>mapOfSeats=new HashMap<Integer,Integer>();
////		// one district proportional allocation of seats using votesNotUsed hashMap: find how many votes in total in the map
////		// (sum of all the values)
////		int totalVotes=0;
////		Set <Integer> keys=votesNotUsed.keySet();
////		Iterator <Integer>i=keys.iterator();
////		while (i.hasNext())
////			{
////			Integer key=i.next();
////			int value=(votesNotUsed.get(key)).intValue();
////			totalVotes+=value;
////			}
//////System.out.println("total unused votes "+totalVotes);
////		// then for each party but the last, compute the proportion of seats:
////		// votesParty*nbSeggi/totalVotes
////		int allocatedSeats=0;
////		for (int p=0;p<arrayOfParties.size()-1;++p)
////			{
////			Party aParty=arrayOfParties.get(p);
////			Integer nameOfParty=new Integer(aParty.getName());
////			int votesOfParty=(votesNotUsed.get(nameOfParty)).intValue();
////			int propSeats=Math.round((float)votesOfParty*sizeOfParliament/totalVotes);
////			mapOfSeats.put(nameOfParty,new Integer(propSeats));
////			allocatedSeats+=propSeats;
//////System.out.println("in the proportional parliament, party "+nameOfParty.intValue()+" has "+votesOfParty+" votes, hence "+propSeats+" seats");
////			}
////		Party aParty=arrayOfParties.get(arrayOfParties.size()-1);
////		Integer nameOfParty=new Integer(aParty.getName());
////		int propSeats=sizeOfParliament-allocatedSeats;
//////System.out.println("in the proportional parliament, party "+nameOfParty.intValue()+" has "+propSeats+" seats");
////		mapOfSeats.put(nameOfParty,new Integer(propSeats));
////		return mapOfSeats;
		}

	// function to update a map using all the entries from another map: iterate through the entries
	// to find the value of key and increment, and use the function updateMap(mapToUpdate,key,increment)
	HashMap <Integer,Integer>updateMap(HashMap <Integer,Integer>mapToUpdate,HashMap <Integer,Integer>source)
		{
		Set <Integer>keys=source.keySet();
		Iterator <Integer> i=keys.iterator();
		while (i.hasNext())
			{
			Integer key=i.next();
			int increment=(source.get(key)).intValue();
//System.out.println("update the map of unused votes with the "+increment+" votes of party "+key.intValue());
			mapToUpdate=updateMap(mapToUpdate,key,increment);
			}
		return(mapToUpdate);
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
			HashMap<Integer,Integer> partiesWithMostVotes = countVotesParty(coalition.getArrayOfParties(),voters);
			Integer partyWithMostVotes=(Integer)(findPartyWithMostVotes(partiesWithMostVotes)).getFirst();
			party = parties.get((partyWithMostVotes.intValue()-1));
			coalition.addMainParty(nameOfDistrict,party);
//System.out.println("\tparty with most votes is "+partyWithMostVotes+" corresponds to "+party.getName()+" in array");
			return(party);
		}
		
	}

	HashMap <Integer,Integer>sumMaps(HashMap<Integer,Integer> map1,HashMap<Integer,Integer> map2)
		{
//System.out.println("contents of map1");
//Set keys1=map1.keySet();
//Iterator i1=keys1.iterator();
//while (i1.hasNext())
//	{
//	Integer key=(Integer)i1.next();
//	int val=((Integer)map1.get(key)).intValue();
//System.out.println("at key "+key.intValue()+"map1 contains "+val);
//	}
//System.out.println("contents of map2");
//Set keys2=map2.keySet();
//Iterator i2=keys2.iterator();
//while (i2.hasNext())
//	{
//	Integer key=(Integer)i2.next();
//	int val=((Integer)map2.get(key)).intValue();
//System.out.println("at key "+key.intValue()+"map2 contains "+val);
//	}
		// NOTE: THE TWO MAPS TO ADD SHOULD HAVE THE SAME KEYS!!!
		HashMap<Integer,Integer> map=new HashMap<Integer,Integer>();
		Set <Integer>keys=map1.keySet();
		Iterator <Integer> it=keys.iterator();
		while(it.hasNext())
			{
			Integer key=it.next();
			Integer val1=map1.get(key);
			Integer val2=map2.get(key);
			int newValue=val1.intValue()+val2.intValue();
//System.out.println("for party "+key.intValue()+" value proportional is "+val1.intValue()+" value maggioritario is "+val2.intValue()+" and the sum is "+newValue);
			map.put(key,new Integer(newValue));
			}
		return (map);
		}

	HashMap <Integer,Integer>getProportion(HashMap<Integer,Integer> map,int seats,int totalSeats)
		{
		HashMap <Integer,Integer>newMap=new HashMap<Integer,Integer>();
//System.out.println("seats to attribute is "+seats+" out of a total of "+totalSeats);
		// iterate through the map and multiply the value by perc/100, round the result to the nearest integer
		Set <Integer>keys=map.keySet();
		Iterator <Integer> it=keys.iterator();
		int sumOfSeats=0;
		while (it.hasNext())
			{
			Integer key=it.next();
			Integer value=map.get(key);
			int newValue=Math.round(((float)value.intValue()*seats)/totalSeats);
			sumOfSeats+=newValue;
//System.out.println("for party "+key.intValue()+" the number of seats was "+value.intValue()+" and becomes "+newValue);
//System.out.println("the seats attributed are "+sumOfSeats+" out of "+seats);
			newMap.put(key,new Integer(newValue));
			}
		newMap=checkSeats(newMap,sumOfSeats,seats);
		return (newMap);
		}
	// checks that the number of seats attributed (sumOfSeats) is equal to the number of seats to attribute
	// (seats). if smaller, add seats one by one to the parties starting with the ones with less seats
	// if larger, take seats one by one from the parties starting with the ones with more seats
	public HashMap <Integer,Integer> checkSeats(HashMap <Integer,Integer>map,int sumOfSeats,int seats)
		{
		if (sumOfSeats<seats)
			{
//			HashMap trialMap=(HashMap)map.clone();
			HashMap <Integer,Integer> trialMap = new HashMap <Integer,Integer> ();
			Set <Integer> keys = map.keySet();
			Iterator <Integer> k = keys.iterator();
			while (k.hasNext()) {
				Integer key = k.next();
				Integer val = map.get(key);
				trialMap.put(key,val);
			}
			LinkedList <Integer> minList=new LinkedList<Integer>();
			int nbSeatsExtra=seats-sumOfSeats;
//System.out.println("there are "+nbSeatsExtra+" seats left to attribute");
			while (nbSeatsExtra!=0)
				{
				// find the party(ies) with the minimum seats (from trialMap)and put then in minMap
				int min=getMin(trialMap);
				keys=map.keySet();
				Iterator <Integer> it=keys.iterator();
				int value=-1;
				while (it.hasNext())
					{
					Integer key=it.next();
					value=(map.get(key)).intValue();
					if (value==min)
						{
						minList.add(key);
						}
					}
				// get at random the index of an element from minList
				// the element in minList at this position defines the party from which seats
				// will be taken
				int index=generator.nextInt(minList.size());
				Integer key=minList.get(index);
//System.out.println("one seat is added for party "+key.intValue());
				value=(map.get(key)).intValue();
				// add 1 to the value in map
				map.put(key,new Integer(++value));
				// there is one less seat to attribute
				--nbSeatsExtra;
//System.out.println("There are "+nbSeatsExtra+" seats left to attribute");
				// as soon as nbSeatsExtra reaches 0, break!
				if (nbSeatsExtra==0)
					{
//System.out.println("all done, break");
					break;
					}
				// remove the current party from trialMap (so another one would be chosen
				// if  there are more seats to attribute)
				trialMap.remove(key);
				}// end while nbSeatsExtra
			}
		else if (sumOfSeats>seats)
			{
//			HashMap trialMap=(HashMap)map.clone();
			HashMap <Integer,Integer> trialMap = new HashMap <Integer,Integer> ();
			Set <Integer> keys = map.keySet();
			Iterator <Integer> k = keys.iterator();
			while (k.hasNext()) {
				Integer key = k.next();
				Integer val = map.get(key);
				trialMap.put(key,val);
			}
			LinkedList <Integer> maxList=new LinkedList<Integer>();
			int nbSeatsExtra=sumOfSeats-seats;
//System.out.println("there are "+nbSeatsExtra+" excess seats");
			while (nbSeatsExtra!=0)
				{
				// find the party(ies) with the maximum seats (from trialMap)and put then in minMap
				int max=getMax(trialMap);
				keys=map.keySet();
				Iterator <Integer> it=keys.iterator();
				int value=1;
				while (it.hasNext())
					{
					Integer key=it.next();
					value=(map.get(key)).intValue();
					if (value==max)
						{
						maxList.add(key);
						}
					}
				// get at random the index of an element from minList
				// the element in minList at this position defines the party from which seats
				// will be taken
				int index=generator.nextInt(maxList.size());
//System.out.println("the random index is between 0 and "+maxList.size()+" and is "+index);
				Integer key=maxList.get(index);
				value=(map.get(key)).intValue();
				// delete 1 to the value in map
				map.put(key,new Integer(value-1));
				// there is one less seat to attribute
				--nbSeatsExtra;
//System.out.println("There are "+nbSeatsExtra+" excess seats");
				// as soon as nbSeatsExtra reaches 0, break!
				if (nbSeatsExtra==0)
					{
//System.out.println("all done, break");
					break;
					}
				// remove the current party from trialMap (so another one would be chosen
				// if  there are more seats to attribute)
				trialMap.remove(key);
				}// end while nbSeatsExtra
			}
//System.out.println("contents of map in checkSeats");
//Set keysmap=map.keySet();
//Iterator imap=keysmap.iterator();
//while (imap.hasNext())
//	{
//	Integer key=(Integer)imap.next();
//	int val=((Integer)map.get(key)).intValue();
//System.out.println("at key "+key.intValue()+"map1 contains "+val);
//	}
		return (map);
		}



	}



	