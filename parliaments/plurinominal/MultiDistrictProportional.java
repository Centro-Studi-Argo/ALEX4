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
package parliaments.plurinominal;

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

public class MultiDistrictProportional extends Parliament {

//	private String rounding;
//	private HashMap <String,String[]> roundingMethods = new HashMap<String,String[]>();
	private double threshold = 0;
	private int maxValue = 100;
	String thresholdLevel = new String();
	

	// constructor

	public MultiDistrictProportional(SimulationRepository sr) {
		super(sr);
		int limSTL = numberCandidates*2;
//		// initialise the rounding methods, the function they will call and their argument
//		roundingMethods.put("Hare",new String[] {"roundingHareImperiali","0"});
//		roundingMethods.put("Imperiali",new String[] {"roundingHareImperiali","2"});
//		roundingMethods.put("Sainte Lague",new String[] {"roundingDHondtSteLague","2"});
//		roundingMethods.put("D'Hondt",new String[] {"roundingDHondtSteLague","1"});
		// dialog to choose the rounding method
		Set roundingKeys = roundingMethods.keySet();
		rounding = setMethodDialog(roundingKeys,"multiDistrictPlurinominal");
		String[] vals = {"MultiDistrictProportional",rounding};
		String key = makeKey(vals);	

		threshold = setParameterDialog(threshold,language.getString("messages","thresholdProportional"),maxValue);

		String thresholdLevelString = setMethodDialog(thresholdLevels.keySet(),"thresholdLevels");
		thresholdLevel = thresholdLevels.get(thresholdLevelString);
		
		ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
		progressBar.setMaximum(((Integer)simulationRepository.getGeneralParameters().get("numberUninominalDistricts")).intValue());


//		if (simulationRepository.containsParliament(key)) {
//			allocationOfSeats = simulationRepository.loadParliament(key);
//		} else {
			allocationOfSeats = findAllocationOfSeats();
System.out.println("saving parliament with key "+key);
			simulationRepository.saveParliament(key,this);
			setNoteToGraph(language.getString("messages","multiDistrictPlurinominal_title")+" "+rounding);
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
//		}
System.out.println(language.getString("plurinominal","MultiDistrictProportional")+" created");
	}

	public String getParliamentName() {
		return (language.getString("plurinominal","MultiDistrictProportional")+" - "+rounding+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return ("MultiDistrictProportional_"+rounding);
	}

	// methods
	// for plurinominal colleges, then aggregate for the parliament
	// for each plurinominal district, get the allocazioneSeggi from the list of voters, save it in each district
	// then add each element to the allocazioneSeggi for the parliament
	public HashMap <Integer,Integer> findAllocationOfSeats() {
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
			int n=listOfPlurinominalDistricts.size();
			// for each plurinominal district
			
			for (int i=0;i<n;++i) {

				Thread.sleep(0);
				progressBar.setValue(n);			

				DistrictPlurinominal districtP=listOfPlurinominalDistricts.get(i);
System.out.println("--------------\nconsidering district "+districtP.getNameOfDistrictP());
				// get the proportionalAllocationOfSeats on the voters of this plurinominal district
				LinkedList <Voter> votersInDistrictP=districtP.getListOfVoters();
System.out.println("votersInDistrictP has "+votersInDistrictP.size()+" elements");
				// list of parameters to invoke the method to find the seats in the district
				Object[] paraList = new Object[]{votersInDistrictP,arrayOfParties,paramOfMethod,new Double(threshold),new Integer(numberCandidates),thresholdLevel};
				HashMap<Integer,Integer> seatsOfDistrict = new HashMap<Integer,Integer>();
				seatsOfDistrict = (HashMap<Integer,Integer>)method.invoke(this,paraList);
				districtP.setAllocationOfSeats(seatsOfDistrict);
				// now add the contents of seatsOfDistrict to mapOfSeats: iterate and for each party (key) see whether
				// it exists in mapOfSeats. If so, add the seats (values) to the existing values in mapOfSeats.
				// If not add a new entry in mapOfSeats
				Set <Integer> parties=seatsOfDistrict.keySet();
				Iterator <Integer> k=parties.iterator();
				while (k.hasNext()) {
					Integer party=k.next();
					int seats=(seatsOfDistrict.get(party)).intValue();
					mapOfSeats = updateMap(mapOfSeats,party,seats);
				}
				// complete mapOfSeats with missing parties (those who got no seats at all)
				mapOfSeats = completeMap(mapOfSeats);
			}// end of for loop on plurinominal colleges
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
				} catch (InterruptedException e) {
					returnCode = "interrupted";
	System.out.println("interrupted");
		} 
		return mapOfSeats;
	}// end of findAllocationOfSeats method
		
//	public HashMap <Integer,Integer> roundingDHondtSteLague(LinkedList <Voter> listOfVoters,ArrayList <Party> arrayOfParties,Integer incr) {
//		int increment = incr.intValue();
//		int limit = numberCandidates * 2;
//		HashMap <Integer,Integer> seatsOfDistrict=new HashMap<Integer,Integer>();
//		// theMatrix maps contain the parties names and their relative values from the divisor
//		// NOTE: since a map cannot have duplicate keys (and there is no reason why to values
//		// cannot be the same, we use 2 maps and an index. Then we order the map of values, retrieve the
//		// index of the highest value and get the corresponding party from the map of parties
//		HashMap <Integer,Integer> theMatrixParties=new HashMap <Integer,Integer> ();
//		HashMap <Integer,Integer> theMatrixValues=new HashMap <Integer,Integer> ();
//		// get the total of the votes for each party, using the voters first preferences
//		int n=arrayOfParties.size();
//		HashMap <Integer,Integer> totalVotes=new HashMap <Integer,Integer> ();
//		for (int i=0;i<n;++i) {
//			Party party=(Party)arrayOfParties.get(i);
//			int nomeP=party.getName();
//			double share = party.getShare();
//			if (share<threshold) {
//				totalVotes.put(new Integer(nomeP),new Integer(0));
//			} else {
//				int votes=findTotalVotesForParty(nomeP,listOfVoters);
//				totalVotes.put(new Integer(nomeP),new Integer(votes));
//			}
//		}
//
//		
//		// loop from 1 to numberCandidates, by increments of "increment"
//		int index=1;
//		for (int i=1;i<=limit;i+=increment) {
//System.out.println("creating TheMatrix, iterating : "+i+" of "+numberCandidates);
//			// divide all values in totalVotes by i, then put the result into theMatrixValues and theMatrixParties
//			// taking totalVotes from the smallest to the largest party
//			LinkedList <Integer> orderedVotesKeys=sortMap(totalVotes,"ascending",null);
//			for (int j=0;j<orderedVotesKeys.size();++j) {
//				Integer nome=orderedVotesKeys.get(j);
//				int votes=(totalVotes.get(nome)).intValue();
//				votes/=i;
//				theMatrixParties.put(new Integer(index),nome);
//				theMatrixValues.put(new Integer(index),new Integer(votes));
//				++index;
//			}
//		}// end of for, theMatrix is created
//System.out.println("finished creating the matrix");
//System.out.println("theMatrix for parties: "+theMatrixParties);
//System.out.println("theMatrix for values: "+theMatrixValues);
//		// sort the entries in theMatrix by value until enough parties have been found for the district
//System.out.println("sort the matrix");
//		LinkedList <Integer> sortedIndexes=sortTheMatrix(theMatrixValues,theMatrixParties,"descending",getProportionalAllocationOfSeats());
//System.out.println("done");
//System.out.println("sortedIndexes: "+sortedIndexes);
//		// create seatsOfDistrict from the parties (count the number of times each party appears in the list)
//System.out.println("allocate seats");
//		for (int i=0;i<numberCandidates;++i) {
//			Integer ind=sortedIndexes.get(i);
//			Integer party=theMatrixParties.get(ind);
//System.out.println("index: "+ind+" corresponds to party "+party);
//			if (seatsOfDistrict.containsKey(party)) {
//				int seats=(seatsOfDistrict.get(party)).intValue();
//				++seats;
//				seatsOfDistrict.remove(party);
//				seatsOfDistrict.put(party,new Integer(seats));
//			} else {
//				seatsOfDistrict.put(party,new Integer(1));
//			}
//		}
//System.out.println("seatsOfDistrict after this: "+seatsOfDistrict);
//		// fill up seatsOfDistrict with the unused party names with 0 seats if seatsOfDistrict
//		// has less entries than number of parties
//System.out.println("deal with unused votes");
//		for (int i=0;i<arrayOfParties.size();++i) {
//			Party party=arrayOfParties.get(i);
//			Integer nameOfParty=new Integer(party.getName());
//			if (!seatsOfDistrict.containsKey(nameOfParty)) {
//				seatsOfDistrict.put(nameOfParty,new Integer(0));
//			}
//		}
//System.out.println("seatsOfDistrict finally: "+seatsOfDistrict);
//		return seatsOfDistrict;
//	}// end of roundingDHondtSteLague method
//
//	// Hare and imperiali methods to compute the seats in the plurinominal colleges
//	public HashMap  <Integer,Integer> roundingHareImperiali(LinkedList <Voter> listOfVoters,ArrayList <Party> arrayOfParties, Integer incr) {
//		int increment = incr.intValue();
//		HashMap <Integer,Integer> seatsOfDistrict=new HashMap <Integer,Integer> ();
//		// numero di elettori nel district
//		int numVoters=listOfVoters.size();
//		// quota (numero di elettori nel district/(numero di candidati per district plurinominale+increment)
//System.out.println("numVoters "+numVoters+" numberCandidates "+numberCandidates+" increment "+increment);
//		int quota=numVoters/(numberCandidates+increment);
//		// get the total votes
//		int n=arrayOfParties.size();
//		HashMap <Integer,Integer> totalVotes=new HashMap <Integer,Integer> ();
//		for (int i=0;i<n;++i) {
//			Party party=arrayOfParties.get(i);
//			int nomeP=party.getName();
//			double share = party.getShare();
//			if (share<threshold) {
//				totalVotes.put(new Integer(nomeP),new Integer(0));
//			} else {
//				int votes=findTotalVotesForParty(nomeP,listOfVoters);
//				totalVotes.put(new Integer(nomeP),new Integer(votes));
//			}
//		}
//		// find the number of seats for each party in totalVotes, and put the party name and number
//		// in seatsOfDistrict; update the counter nbSeatsAlreadyAttributed; create the map with the remaining seats
//		int nbSeatsAlreadyAttributed=0;
//		HashMap <Integer,Integer> remainingVotes=new HashMap <Integer,Integer> ();
//		LinkedList <Integer> orderedVotesKeys=sortMap(totalVotes,"ascending",null);
//		for (int i=0;i<orderedVotesKeys.size();++i) {
//			if (nbSeatsAlreadyAttributed<numberCandidates) {
//				Integer partyName=orderedVotesKeys.get(i);
//				int votes=(totalVotes.get(partyName)).intValue();
//				int nbSeats=votes/quota;
//				// check that the number of seats is smaller than the number of seats left to attribute
//				// (can happen with Imperiali where the quota is smaller, for small colleges)
//				int nbSeatsToAttribute=numberCandidates-nbSeatsAlreadyAttributed;
//				if (nbSeats>nbSeatsToAttribute) {
//					nbSeats=nbSeatsToAttribute;
//				}
//				seatsOfDistrict.put(partyName,new Integer(nbSeats));
//				nbSeatsAlreadyAttributed+=nbSeats;
//				int lastVotes=votes-(quota*nbSeats);
//				remainingVotes.put(partyName,new Integer(lastVotes));
//			}
//		}
//		// complete seatsOfDistrict with the parties having the largest number of remaining seats
//		while (nbSeatsAlreadyAttributed<numberCandidates) {
//			// get key for max value of remainingVotes
//			Integer maxKey=getKeyForMaxValue(remainingVotes);
//			// add 1 to the number of seats of this party, increment nbSeatsAlreadyAttributed
//			int nbSeats=(seatsOfDistrict.get(maxKey)).intValue();
//			seatsOfDistrict.remove(maxKey);
//			++nbSeats;
//			seatsOfDistrict.put(maxKey,new Integer(nbSeats));
//			remainingVotes.remove(maxKey);
//			++nbSeatsAlreadyAttributed;
//		}
//		return seatsOfDistrict;
//	}// end of roundingHareImperiali
//
//
//
//
//	int getMaxKey(HashMap <Integer,Integer>  map) {
//		Set <Integer> mapKeys=map.keySet();
//		Iterator <Integer> i=mapKeys.iterator();
//		int max=0;
//		while (i.hasNext()) {
//			int val=(i.next()).intValue();
//			max=(val>max)?val:max;
//		}
//		return max;
//	}
//
//	Integer getKeyForMinValue(HashMap <Integer,Integer> map) {
//		Collection <Integer> mapValues=map.values();
//		Iterator <Integer> i=mapValues.iterator();
//		int min=Integer.MAX_VALUE;
//		while(i.hasNext()) {
//			int val=(i.next()).intValue();
//			min=(val<min)?val:min;
//		}
//		Set <Integer> keys=map.keySet();
//		Iterator <Integer> j=keys.iterator();
//		Integer key=new Integer(0);
//		while(j.hasNext()) {
//			key=j.next();
//			int val=((Integer)map.get(key)).intValue();
//			if (val==min) {
//				break;
//			}
//		}
//		return key;
//	}
//
//	Integer getKeyForMaxValue(HashMap <Integer,Integer> map) {
//		Collection <Integer> mapValues=map.values();
//		Iterator <Integer> i=mapValues.iterator();
//		int max=Integer.MIN_VALUE;
//		while(i.hasNext()) {
//			int val=(i.next()).intValue();
//			max=(val>max)?val:max;
//		}
//		Set <Integer> keys=map.keySet();
//		Iterator <Integer> j=keys.iterator();
//		Integer key=new Integer(0);
//		while(j.hasNext()) {
//			key=j.next();
//			int val=(map.get(key)).intValue();
//			if (val==max) {
//				break;
//			}
//		}
//		return key;
//	}
//
//	public String getRounding() {
//		return rounding;
//	}
//
//
//	public LinkedList <Integer> sortTheMatrix(HashMap <Integer,Integer> mapValues,HashMap <Integer,Integer> mapKeys,String order,HashMap <Integer,Integer> checkIfTies) {
//System.out.println("entered sortTheMatrix!");
////		HashMap <Integer,Integer> clone=(HashMap <Integer,Integer>)mapValues.clone();
//		HashMap <Integer,Integer> clone= new HashMap <Integer,Integer>();
//		Set <Integer> keysm = mapValues.keySet();
//		Iterator <Integer> km = keysm.iterator();
//		while (km.hasNext()) {
//			Integer key = km.next();
//			Integer val = mapValues.get(key);
//			clone.put(key,val);
//		}
//		
//		LinkedList <Integer> orderedValues=new LinkedList<Integer>();
//		// while the number of elements in orderedKeys is smaller than nbKeysToSort
//		while (orderedValues.size()<mapValues.size()) {
//System.out.println("clone at beginning of loop"+clone);
//			// find the index corresponding to the minimum value in clone
//			Integer index = new Integer(0);
//			Collection <Integer> col=clone.values();
//			Iterator <Integer> i=col.iterator();
//			int min=Integer.MAX_VALUE;
//			while(i.hasNext()) {
//				int val=(i.next()).intValue();
//				min=(val<min)?val:min;
//			}
//			Set <Integer> keys=clone.keySet();
//			Iterator <Integer> j=keys.iterator();
//			Integer key=new Integer(0);
//			LinkedList <Integer> keysWithMinValue = new LinkedList<Integer>();
//			while(j.hasNext()) {
//				key=j.next();
//				int val=(clone.get(key)).intValue();
//				if (val==min) {
//					keysWithMinValue.add(key);
//				}
//			}
//System.out.println("keys with min value list is created: "+keysWithMinValue);
//			if (keysWithMinValue.size()==1) {
//System.out.println("It has one element");
//				index = keysWithMinValue.getFirst();
//System.out.println("Index is "+index);
//			} else {
//System.out.println("It has "+keysWithMinValue.size()+" elements");
//				// look for values obtained by these keys in the checkTies map
//				// (call the getKeyForMinValue recursively with a null checkties map)
//				// if only one minimum, return it, else return one randomly
//				if (checkIfTies != null) {
//System.out.println("There is a matrix in which to check wich element should be chosen");
//					HashMap <Integer,Integer>  mapToCheck = new HashMap <Integer,Integer> ();
//					for (int k=0;k<keysWithMinValue.size();++k) {
//						Integer el = keysWithMinValue.get(k);
//						Integer element = mapKeys.get(el);
//						Integer value = checkIfTies.get(element);
//						mapToCheck.put(el,value);
//System.out.println("Put element in maptocheck, key="+el+" value="+value+" (party is )"+element);
//					}
//					LinkedList <Integer> sortedCheck = sortMap(mapToCheck,"ascending",null);
//System.out.println("returning first element "+((Integer)sortedCheck.getFirst()));
//					index = sortedCheck.getFirst(); 
//System.out.println("Index is "+index);
//				} else {// checkTies is null, choose one of keysWithMinValue at random
//					int rdm = generator.nextInt(keysWithMinValue.size());
//System.out.println("returning random "+((Integer)keysWithMinValue.get(rdm)));
//					index = keysWithMinValue.get(rdm);
//System.out.println("Index is "+index);
//				}
//			}
//			
//			// add to orderedKeys: if order="ascending", use addLast, else if "descending" use addFirst
//			if (order.compareTo("ascending")==0) {
//				orderedValues.addLast(index);
//			} else if (order.compareTo("descending")==0) {
//				orderedValues.addFirst(index);
//			} else {
//				System.out.println("the order "+order+" does not exist. You can only use \"ascending\" or \"descending");
//				System.exit(0);
//			}
//			// remove the key/value combination from clone
//			clone.remove(index);
//		}
//		return orderedValues;
//	}// end of sortTheMatrix function
//
//	Integer getKeyForMinValue(HashMap <Integer,Integer> map,HashMap <Integer,Integer> checkTies) {
//System.out.println("Entered getkey for min value");
//System.out.println("checkTies is : "+checkTies);
//		Collection <Integer> mapValues=map.values();
//		Iterator <Integer>i=mapValues.iterator();
//		int min=Integer.MAX_VALUE;
//		while(i.hasNext()) {
//			int val=(i.next()).intValue();
//			min=(val<min)?val:min;
//		}
//		Set <Integer> keys=map.keySet();
//		Iterator <Integer> j=keys.iterator();
//		Integer key=new Integer(0);
//		LinkedList <Integer> keysWithMinValue = new LinkedList<Integer>();
//		while(j.hasNext()) {
//			key=j.next();
//			int val=((Integer)map.get(key)).intValue();
//			if (val==min) {
//				keysWithMinValue.add(key);
//			}
//		}
//System.out.println("keys with min value list is created");
//		if (keysWithMinValue.size()==1) {
//System.out.println("It has one element");
//			return  keysWithMinValue.getFirst();
//		} else {
//System.out.println("It has "+keysWithMinValue.size()+" elements");
//			// look for values obtained by these keys in the checkTies map
//			// (call the getKeyForMinValue recursively with a null checkties map)
//			// if only one minimum, return it, else return one randomly
//			if (checkTies != null) {
//System.out.println("There is a matrix in which to check wich element should be chosen");
//				HashMap  <Integer,Integer> mapToCheck = new HashMap <Integer,Integer> ();
//				for (int k=0;k<keysWithMinValue.size();++k) {
//					Integer element = keysWithMinValue.get(k);
//					Integer value = checkTies.get(element);
//					mapToCheck.put(element,value);
//System.out.println("Put element in maptocheck, key="+element+" value="+value);
//				}
//				LinkedList <Integer> sortedCheck = sortMap(mapToCheck,"descending",null);
//System.out.println("returning first element "+((Integer)sortedCheck.getFirst()));
//				return (sortedCheck.getFirst());
//			} else {// checkTies is null, choose one of keysWithMinValue at random
//				int rdm = generator.nextInt(keysWithMinValue.size());
//System.out.println("returning random "+((Integer)keysWithMinValue.get(rdm)));
//				return(keysWithMinValue.get(rdm));
//			}
//		}
//	}
//

	
}// end of class definition


