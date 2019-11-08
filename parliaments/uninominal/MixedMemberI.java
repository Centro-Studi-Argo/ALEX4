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
import actions.*;

public class MixedMemberI extends Parliament {
	HashMap <Integer,Integer> vpm = new HashMap <Integer,Integer> ();
	HashMap <Integer,Integer> vpp = new HashMap <Integer,Integer> ();

	int valuePercProp = 5;
	int thisAllocSeats = 1;
	int maxValue = 100;
	boolean withStrategic = false;
	double kForDistance=0;
	String nameOfGroupOfCoalitions;
	LinkedList <Coalition> listOfCoalitions;
	SimulationRepository sr;
	
	// constructor

	public MixedMemberI() {
	}
	public MixedMemberI(SimulationRepository sr) {
		super(sr);
		this.sr = sr;
System.out.println("==============\nCONSTRUCTOR FOR MIXEDMEMBERI - no strategic\n======================");
		// value attributed with the proportional system (max = maxValue)
		valuePercProp = setParameterDialog(valuePercProp,language.getString("messages","mixedMember_percProp"),maxValue);
//System.out.println(language.getString("uninominal","MixedMemberI")+" created");
		if (thisAllocSeats == 1) {
			if (withStrategic==false) {
				String[] vals = {"MixedMemberI",(new Integer(valuePercProp)).toString()};
				parliamentKey = makeKey(vals);	
			} else {
				String[] vals = {"MMI-SV","MixedMemberI",(new Integer(valuePercProp)).toString()};
				parliamentKey = makeKey(vals);	
			}			

			ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
			progressBar.setMaximum(3);
			
//System.out.println("compute parliament");
			allocationOfSeats = findAllocationOfSeats();
			simulationRepository.saveParliament(parliamentKey,this);
			setNoteToGraph(language.getString("messages","mixedMember_percProp")+" "+valuePercProp);
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
		}
	}
	
	public MixedMemberI(SimulationRepository sr, boolean withStrategic) {
		super(sr);
		this.sr = sr;
System.out.println("==============\nCONSTRUCTOR FOR MIXEDMEMBERI - with strategic\n======================");
		this.withStrategic = withStrategic;
		// value attributed with the proportional system (max = maxValue)
		valuePercProp = setParameterDialog(valuePercProp,language.getString("messages","mixedMember_percProp"),maxValue);
//System.out.println("1 - value perc prop "+valuePercProp);
		if (thisAllocSeats == 1) {
//	 		kForDistance = setParameterDialog(kForDistance,language.getString("labels","weightDistance"),100);

			ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
			progressBar.setMaximum(3);

			
//System.out.println("compute parliament:");
			allocationOfSeats = findAllocationOfSeats();
//System.out.println("2 - now valuePercProp is "+valuePercProp+" and kForDistance is "+kForDistance);
//System.out.println("done allocation of seats");
			if (withStrategic==false) {
				String[] vals = {"MixedMemberI",(new Integer(valuePercProp)).toString()};
				parliamentKey = makeKey(vals);	
			} else {
				String[] vals = {"MMI-SV","MixedMemberI",(new Integer(valuePercProp)).toString(),language.getString("labels","weightDistanceShort")+"="+(new Double(kForDistance)).toString(),language.getString("labels","coalitions")+"="+nameOfGroupOfCoalitions};
				parliamentKey = makeKey(vals);	
			}			
			simulationRepository.saveParliament(parliamentKey,this);
			setNoteToGraph(language.getString("messages","mixedMember_percProp")+" "+valuePercProp);
			if (withStrategic==true) {
				setNoteToGraph(language.getString("labels","weightDistanceShort")+" "+kForDistance);
				setNoteToGraph(language.getString("labels","coalitions")+" "+nameOfGroupOfCoalitions);
			}
		}
	}


	public String getParliamentName() {
		if (withStrategic==false) {
			return (language.getString("uninominal","MixedMemberI")+" - "+valuePercProp+"%"+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
		} else {
			return (language.getString("uninominal","MixedMemberI")+" - "+valuePercProp+"%"+language.getString("uninominal","StrageticVote")+" - "+kForDistance+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
		}
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}


	
	// method to allocate the seats (current method)
	// in each district, the winning party is the one who has the most votes
	public HashMap <Integer,Integer>  findAllocationOfSeats()
		{
System.out.println("Mixed member I: find allocation of seats");
		HashMap <Integer,Integer>  mapOfSeats = new HashMap <Integer,Integer> ();
		// first past the post allocation of seats
		Parliament firstPastThePost;
		if (withStrategic==false) {
//System.out.println("load normal first past the post");
			firstPastThePost = new FirstPastThePost(simulationRepository);
		} else {
//System.out.println("load first past the post with strategic vote");
			firstPastThePost = new StrategicVote(simulationRepository);
			kForDistance = ((StrategicVote)firstPastThePost).getWeightDistance();
			nameOfGroupOfCoalitions = ((StrategicVote)firstPastThePost).getNameOfGroupOfCoalitions();
		}
		try {
			Thread.sleep(0);
			progressBar.setValue(1);
//System.out.println("get allocation of seats of first past the post");
			HashMap  <Integer,Integer> allocationOfSeats_FirstPastThePost = firstPastThePost.getAllocationOfSeats();
			if (allocationOfSeats_FirstPastThePost.size()==0) {
//System.out.println("does not exist yet, find it");
				allocationOfSeats_FirstPastThePost = firstPastThePost.findAllocationOfSeats();
			}
			vpm = firstPastThePost.getVotesForParliament();
//System.out.println("done");
			// to find the allocationOfSeats in this mixed system, multiply the allocationOfSeats with
			// proportional system by the value of percProp, and the allocationOfSeats with the maggioritario
			// by 100-value of percProp; then sum the two, making sure the total is nbSeggi.
			int seatsOneDistrictProportional = Math.round(((float)sizeOfParliament*valuePercProp)/100);
	
			Thread.sleep(0);
			progressBar.setValue(2);
	
			int seatsFirstPastThePost = sizeOfParliament - seatsOneDistrictProportional;
//System.out.println("find proportion for proportional");
			ThresholdProportional thresholdProportional = new ThresholdProportional(sr);
			
			HashMap <Integer,Integer>  allocationOfSeats_OneDistrictProportional = getProportion(thresholdProportional.findAllocationOfSeats(),seatsOneDistrictProportional,sizeOfParliament);
//System.out.println("find proportion for first past the post");
			allocationOfSeats_FirstPastThePost = getProportion(allocationOfSeats_FirstPastThePost,seatsFirstPastThePost,sizeOfParliament);
//System.out.println("sum up");
			mapOfSeats = sumMaps(allocationOfSeats_OneDistrictProportional , allocationOfSeats_FirstPastThePost);
	
			Thread.sleep(0);
			progressBar.setValue(3);
		} catch (InterruptedException e) {
			returnCode = "interrupted";
//System.out.println("interrupted");
		}
		votesPerParty = vpm;
		return(mapOfSeats);
		}



	public Integer getIntPercProp()
		{
		return(new Integer(valuePercProp));
		}

		
	public HashMap  <Integer,Integer> getProportion(HashMap <Integer,Integer>  map,int seats,int totalSeats)
		{
		HashMap <Integer,Integer> newMap=new HashMap<Integer,Integer>();
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
	public HashMap <Integer,Integer>  checkSeats(HashMap <Integer,Integer>  map,int sumOfSeats,int seats)
		{
		if (sumOfSeats<seats)
			{
//			HashMap <Integer,Integer> trialMap=(HashMap)map.clone();
			HashMap <Integer,Integer> trialMap=new HashMap <Integer,Integer> ();
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
			HashMap <Integer,Integer> trialMap=new HashMap <Integer,Integer> ();
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
		
	HashMap <Integer,Integer> sumMaps(HashMap <Integer,Integer> map1,HashMap <Integer,Integer> map2)
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
		HashMap <Integer,Integer> map=new HashMap<Integer,Integer>();
		Set <Integer>keys=map1.keySet();
		Iterator <Integer>it=keys.iterator();
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
	}