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
import actions.*;
import gui.*;
import com.tomtessier.scrollabledesktop.*;

public class SingleTransferableVote extends Parliament {

	private String rounding;
	private HashMap <String,String[]> roundingMethods = new HashMap<String,String[]>();
	

	// constructor

	public SingleTransferableVote(SimulationRepository sr) {
		super(sr);
		// initialise the rounding methods, the function they will call and their argument
		roundingMethods.put("N.B.",new String[] {"VSTshare","1","0"});
		roundingMethods.put("Droop",new String[] {"VSTshare","1","1"});
		roundingMethods.put("Hare",new String[] {"VSTshare","0","0"});
		// dialog to choose the rounding method
		Set <String> roundingKeys = roundingMethods.keySet();
		rounding = setMethodDialog(roundingKeys,"singleTransferableVote");
		String[] vals = {"SingleTransferableVote",rounding};
		final String key = makeKey(vals);
System.out.println("VST rounding "+rounding);		
		// before the allocation of seats, need to compute the preferences for the candidates, if they do not exist
		// start a swingworker thread that signals this and checks for candidates;
		// when finished, compute the allocation of seats
		CreatorVotingObjects creatorVotingObjects = new CreatorVotingObjects(simulationRepository.getGeneralParameters(),
			arrayOfParties,ListOfParliaments.getProgressBar(),simulationRepository);
		ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects"));
		LinkedList <String> what = new LinkedList<String>();
		Voter aVoter = listOfVoters.getFirst();
		if (aVoter.getCandidatePreferences().size() == 0) {
System.out.println("need to create the candidates");
			what.add("votersCandidates");
		} else {
			what.add("nothing");
		}
		creatorVotingObjects.createVotingObjects(what);
System.out.println("done creation of candidates");
		ListOfParliaments.getProgressBar().setValue(0);
		ListOfParliaments.setDisplayInProgressPanel(language.getString("labels","creationVotingObjects")+" "+getParliamentName());
System.out.println("done re-set progress bar");
		// compute the allocation of seats
System.out.println("start allocation of seats");
		if (simulationRepository.containsParliament(key)) {
System.out.println("repository already contains the parliament, load it");
			allocationOfSeats = simulationRepository.loadParliament(key);
		} else {
System.out.println("simulate the parliament");
		allocationOfSeats = findAllocationOfSeats();
		simulationRepository.saveParliament(key,SingleTransferableVote.this);
		setNoteToGraph(language.getString("messages","singleTransferableVote_title")+" "+rounding);
			setNoteToGraph(language.getString("labels","majorityLevel")+": "+majorityLevel);
		}

			
		// need to wait for the simulation of candidates to be complete and then
System.out.println(language.getString("plurinominal","SingleTransferableVote")+" created");
	}

	public String getParliamentName() {
		return (language.getString("plurinominal","SingleTransferableVote")+" - "+rounding+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return "SingleTransferableVote_"+rounding;
	}

	// methods

	// allocazioneSeggi:
	// for plurinominal colleges, then aggregate for the parliament
	// for each plurinominal college, get the allocazioneSeggi from the list of electors, save it in each college
	// then add each element to the allocazioneSeggi for the parliament
	public HashMap <Integer,Integer> findAllocationOfSeats() {
//System.out.println("entered findAllocationOfSeats");
		ListOfParliaments.getProgressBar().setString(language.getString("labels","creationParliament"));
		// get the name and parameter of method from the map roundingMethods.
		String[] methodChar = (String[])roundingMethods.get(rounding);
		String nameOfMethod = methodChar[0];
		Integer paramOfMethod1 = new Integer(methodChar[1]);
		Integer paramOfMethod2 = new Integer(methodChar[2]);
		// prepare the method to be invoked
		HashMap <Integer,Integer> mapOfSeats=new HashMap<Integer,Integer>();
		try {
			Class thisClass = this.getClass();
			Class[] typeOfParameters = new Class[] {LinkedList.class,LinkedList.class,Integer.class,Integer.class};
			Method method = (this.getClass()).getMethod(nameOfMethod,typeOfParameters);
			int n=listOfPlurinominalDistricts.size();
//System.out.println("size of list of plurinominal districts"+n);
			// for each plurinominal district
			for (int i=0;i<n;++i) {
				Thread.sleep(0);
				progressBar.setValue(i);

				DistrictPlurinominal districtP=listOfPlurinominalDistricts.get(i);
//System.out.println("considering plurinominal district: "+districtP.getNameOfDistrictP());
				// get the proportionalAllocationOfSeats on the voters of this plurinominal district
				LinkedList <Voter> votersInDistrictP=districtP.getListOfVoters();
//System.out.println("done get list of voters");				
				// list of parameters to invoke the method to find the seats in the district
//				LinkedList tempListOfCandidates = (LinkedList)listOfCandidates.clone(); // necessary as I remove elements from it
				LinkedList <Candidate> tempListOfCandidates = new LinkedList <Candidate> ();
				Iterator <Candidate> cl = listOfCandidates.iterator();
				while (cl.hasNext()) {
					Candidate c = cl.next();
					tempListOfCandidates.add(c);
				}
				Object[] paraList = new Object[]{votersInDistrictP,tempListOfCandidates,paramOfMethod1,paramOfMethod2};
				HashMap <Integer,Integer> seatsOfDistrict = (HashMap<Integer,Integer>)method.invoke(this,paraList);
//System.out.println("done seats of districts");
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


	// VST share methods to compute the seats in the plurinominal colleges
	// all the methods use the same equation.
	// for N.B., the increment is 1 and 0 is added
	// for Droop, the increment is 1 and 1 is added
	// for Hare, the increment is 0 and 0 is added
	public HashMap <Integer,Integer> VSTshare(LinkedList <Voter> listOfVoters,LinkedList <Candidate> tempListOfCandidates,Integer incr,Integer adv)
		{
		int increment = incr.intValue();
		int addValue = adv.intValue();
//System.out.println("\n\tentered shareVST\n");
		HashMap <Integer,Integer> seatsOfDistrict=new HashMap<Integer,Integer>();
		LinkedList <Candidate> electedCandidates=new LinkedList<Candidate>();
		// number of electors in the college
		int numberVoters=listOfVoters.size();
//System.out.println("\tnumberVoters "+numberVoters);
		// share: ([numero di elettori nel collegio/(numero di candidates per collegio plurinominale+increment)]+addValue)
		int share=(numberVoters/(numberCandidates+increment))+addValue;
//System.out.println("\tthe share is: "+share);
		// get the total votes for the candidates in each party
		int n=tempListOfCandidates.size();
//System.out.println("\tsize of tempListOfCandidates "+n);
		HashMap <Candidate,Integer> totalVotes=new HashMap <Candidate,Integer> ();
		HashMap <Candidate,LinkedList<Voter>> totalVoters=new HashMap <Candidate,LinkedList<Voter>> ();
		int positionInList=1;
		for (int i=0;i<n;++i)
			{
			Candidate candidate=tempListOfCandidates.get(i);
			LinkedList <Voter> totalE=getTotalVotesCandidates(candidate,listOfVoters,positionInList);
			int votes=totalE.size();
			totalVotes.put(candidate,new Integer(votes));
			totalVoters.put(candidate,totalE);
//System.out.println("\t\tthe candidate in position "+candidate.getPositionInList()+" for party "+candidate.getPartyName()+" has "+votes+" votes");
			}
//System.out.println("\ttotalVotes has "+totalVotes.size()+" elements");
		// while numberCandidatesAttributed is not equal to numCandidatiCollegio
		// for each candidate in totalVotes
		// if votes>share, "elect" the candidate, compute the unused votes (votes-share) for this candidate and put the value
		// in remainingVotes map, delete the candidate from totalVotes and add it to electedCandidates
		// attribute the surplus of votes of the elected candidate to the candidate next in the list for the party
		// of no candidate has votes>=share, eliminate the candidate with the least votes and redistribute it considering the second, third
		// preferences of the electors, until either all candidates necessary have been elected, or the number of remaining
		// candidates is equal to the number of seats to fill
		int numberCandidatesAttributed=0;
		int numberCandidatesToAttribute=numberCandidates-numberCandidatesAttributed;
//System.out.println("tnumberCandidatesToAttribute "+numberCandidatesToAttribute);
//System.out.println("\tsize of list of candidates "+tempListOfCandidates.size());
		// label
		whileLoop:
		while(numberCandidatesToAttribute>0)
			{
			// if the number of seats left is equal to the number of candidates left, elect them all
			if (numberCandidatesToAttribute==tempListOfCandidates.size())
				{
//System.out.println("\tnumberCandidatesToAttribute is equal to the size of tempListOfCandidates: elect the remaining candidates");
				for (int j=0;j<tempListOfCandidates.size();++j)
					{
					Candidate candidate=tempListOfCandidates.get(j);
					Integer partyName=new Integer(candidate.getPartyName());
					if (seatsOfDistrict.containsKey(partyName))
						{
						int val=(seatsOfDistrict.get(partyName)).intValue();
						++val;
						seatsOfDistrict.remove(partyName);
						seatsOfDistrict.put(partyName,new Integer(val));
						}
					else
						{
						seatsOfDistrict.put(partyName,new Integer(1));
						}
					}
				// all candidates are elected, break the loop
				break;
				}// end of if numberCandidatesToAttribute
			// find the votes with the share
//System.out.println("\tnumberCandidatesToAttribute "+numberCandidatesToAttribute);
//System.out.println("\tsize of list of candidates "+tempListOfCandidates.size());
System.out.println("\tfind the votes with the share");
			LinkedList <Candidate> candidatesElectedThisTurn=new LinkedList<Candidate>();
//			LinkedList candidatesElectedBefore=(LinkedList)electedCandidates.clone();
			LinkedList <Candidate> candidatesElectedBefore = new LinkedList <Candidate> ();
			Iterator <Candidate> ceb = electedCandidates.iterator();
			while (ceb.hasNext()) {
				candidatesElectedBefore.add(ceb.next());
			}
			HashMap <Candidate,Integer> remainingVotes=new HashMap<Candidate,Integer>();
			LinkedList <Candidate> orderedVotesKeys=sortMapCandidates(totalVotes,"ascending");
//			Set keys=totalVotes.keySet();
//			Iterator i=keys.iterator();
//			while (i.hasNext())
			for (int i=0;i<totalVotes.size();++i)
				{
				Candidate candidate=orderedVotesKeys.get(i);
//System.out.println("\t\tentered while loop on elements of totalVotes");
//				Candidate candidate=(Candidate)i.next();
				int votes=(totalVotes.get(candidate)).intValue();
//System.out.println("\t\ttotal votes for candidate in position "+candidate.getPositionInList()+" for party "+candidate.getPartyName()+" is "+votes);
				int nbSeats=votes/share;
				// check that the number of seats is smaller than the number of seats left to attribute
				if (nbSeats>numberCandidatesToAttribute)
					{
					nbSeats=numberCandidatesToAttribute;
					}
				if (nbSeats>1)	// only one seat can be attributed to a candidate!!!
					{
					nbSeats=1;
					}
//System.out.println("\t\tthe resulting number of seats is "+nbSeats);
				// in seatsOfDistrict, put the name of the party of the candidate if he is elected
				if (nbSeats>0)
					{
					Integer partyName=new Integer(candidate.getPartyName());
					if (seatsOfDistrict.containsKey(partyName))
						{
						int val=((Integer)seatsOfDistrict.get(partyName)).intValue();
						val+=nbSeats;
						seatsOfDistrict.remove(partyName);
						seatsOfDistrict.put(partyName,new Integer(val));
						}
					else
						{
						seatsOfDistrict.put(partyName,new Integer(nbSeats));
						}
					numberCandidatesAttributed+=nbSeats;
					numberCandidatesToAttribute-=nbSeats;
					int lastVotes=votes-(share*nbSeats);
//System.out.println("\t\tthe candidate has "+lastVotes+" votes left");
					remainingVotes.put(candidate,new Integer(lastVotes));
					electedCandidates.add(candidate);
					candidatesElectedThisTurn.add(candidate);
//System.out.println("\t\toverall, "+electedCandidates.size()+" candidates have been elected so far");
//System.out.println("\t\tcandidatesElectedThisTurn has "+candidatesElectedThisTurn.size()+" elements");
					// if all candidates have been elected, break the search and return
					if (electedCandidates.size()==numberCandidates)
						{
						break whileLoop;
						}
					}
				}// end of while loop on totalVotes
			// if no seats have been attributed this time, remove the candidate with the smallest number of votes
			// find from list of electors all those who had him as first preference (?)
			// put them in the list then get a totalVotes for the next position in the list (for candidates not already elected)
			// and add to the existing totalVotes
			if (candidatesElectedThisTurn.size()==0)
				{
//System.out.println("\tthere are no candidates with enough votes");
				Candidate candidate=getCandidateForMinValue(totalVotes);
//System.out.println("\tthe candidate with the least votes is at the "+candidate.getPositionInList()+" place in the list for party "+candidate.getPartyName());
				// eliminate it from tempListOfCandidates and totalVotes
				tempListOfCandidates.remove(candidate);
				totalVotes.remove(candidate);
//System.out.println("\tthe candidate is removed from totalVotes and tempListOfCandidates");
				// create the list of electors with this candidate as first (?) preference
//				LinkedList listOfVotersPref=new LinkedList();
//				for(int e=0;e<listOfVoters.size();++e)
//					{
//					Voter voter=(Voter)listOfVoters.get(e);
//					Candidate candidatoE=voter.getFirstCandidatePreference();
//					if ((candidatoE.getPartyName()==candidate.getPartyName())&&(candidatoE.getPositionInList()==candidate.getPositionInList()))
//						{
//						listOfVotersPref.add(voter);
//						}
//					}
//
//				LinkedList listOfVotersPref=(LinkedList)((LinkedList)totalVoters.get(candidate)).clone();
				LinkedList <Voter> listOfVotersPref = new LinkedList<Voter>();
				LinkedList <Voter> origin = totalVoters.get(candidate);
				Iterator <Voter> o = origin.iterator();
				while (o.hasNext()){
					listOfVotersPref.add(o.next());
				}
				


//System.out.println("\t"+listOfVotersPref.size()+" electors have this candidate as first preference");
				totalVoters.remove(candidate);
				// find the next non-elected and non-eliminated candidate in these voter's preferences,
				// add 1 to its votes in totalVotes and add the voter to the corresponding list of electors
				for (int e=0;e<listOfVotersPref.size();++e)
					{
					Voter el=listOfVotersPref.get(e);
//System.out.println("\t\tvoter "+el.getNameVoter());
					// preferences of the voter
					LinkedList <Candidate> preferences=el.getCandidatePreferences();
					// loop through preferences unless the candidate exists in totalVotes
					looppref: // (label on which the break is applied when a vote has been added)
					for (int p=0;p<preferences.size();++p)
						{
						Candidate candp=preferences.get(p);
//System.out.println("\t\t\tcurrent candidate is at the "+candp.getPositionInList()+"th position for party "+candp.getPartyName());

//System.out.println("\t\t\tcontents of totalVotes");
//Set ktv=totalVotes.keySet();
//Iterator tvk=ktv.iterator();
//while(tvk.hasNext())
////	{
//	Candidate candtv=(Candidate)tvk.next();
//	System.out.println("\t\t\tcandidate C"+candtv.getPositionInList()+"P"+candtv.getPartyName()+" with votes "+(Integer)totalVotes.get(candtv));
//	}
						Set <Candidate> keystv=totalVotes.keySet();
						Iterator <Candidate> tv=keystv.iterator();
						while(tv.hasNext())
							{
							Candidate candtv=tv.next();
							if ((candtv.getPositionInList()==candp.getPositionInList())&&(candtv.getPartyName()==candp.getPartyName()))
								{
//System.out.println("\t\t\tthe candidate C"+candp.getPositionInList()+"P"+candp.getPartyName()+" is neither elected nor eliminated");
								// add to totalVotes
								Integer votiInt=totalVotes.get(candtv);
//System.out.println("\t\t\tgot the votes as Integer: "+votiInt);
								int votitv=votiInt.intValue();
//System.out.print("\t\t\tthis candidate had "+votitv+" votes");
								++votitv;
//System.out.println(" which become "+votitv+" votes");
								totalVotes.remove(candtv);
								totalVotes.put(candtv,new Integer(votitv));
								// add to totalVoters
								LinkedList <Voter> elettori=totalVoters.get(candtv);
//System.out.print("\t\t\tthis candidate had "+elettori.size()+" elettori");
								elettori.add(el);
//System.out.println("which become "+elettori.size()+" elettori");
								totalVoters.remove(candtv);
								totalVoters.put(candtv,elettori);
								break looppref;	  // the votes have been added, stop searching through preferences
								} // end of if candidates are the same
							} // end while tv.hasnext
						}// end loop on preferences
					}// end for on listOfVotersPref


				// increment positionInList (????? or should it be always the second)
//				++positionInList;
//				// get a totalVotes map for the next-in-line preferences of these electors
//System.out.println("get the total votes for the "+positionInList+"th preferences");
//				for (int c=0;c<tempListOfCandidates.size();++c)
//					{
//					Candidate candidatoC=(Candidate)tempListOfCandidates.get(c);
//System.out.println("- candidate at the "+candidatoC.getPositionInList()+"th position of party "+candidatoC.getPartyName());
//					LinkedList elettoriCandidato=getTotalVotesCandidates(candidatoC,listOfVotersPref,positionInList);
//					int votesCandidato=elettoriCandidato.size();
//System.out.println("has "+votesCandidato+" votes");
//					// add the value to the existing totalVotes for this candidate and update totalVoters
//					int val=((Integer)totalVotes.get(candidatoC)).intValue();
//					val+=votesCandidato;
//					totalVotes.remove(candidatoC);
//					totalVotes.put(candidatoC,new Integer(val));
//					LinkedList tempel=(LinkedList)totalVoters.get(candidatoC);
//System.out.println("before, tempel has "+tempel.size()+" elements");
//					tempel.addAll(elettoriCandidato);
//System.out.println("elettoriCandidato has "+elettoriCandidato.size()+" so tempel has now "+tempel.size()+" elements");
//					totalVoters.remove(candidatoC);
//					totalVoters.put(candidatoC,tempel);
//					}
				}// end of if size of candidatesElectedThisTurn is 0
			else
				{
//System.out.println("\tsome candidates elected this turn: transfer their remaining votes to the next candidate of the same party");
				// give the remaining votes from candidatesElectedThisTurn to the next candidate in the list (the one with the highest
				// number of votes from the same party) and remove the candidate from tempListOfCandidates and totalVotes
				// at the end, reinitialise candidatesElectedThisTurn and remainingVotes
				for (int k=0;k<candidatesElectedThisTurn.size();++k)
					{
					Candidate candidate=candidatesElectedThisTurn.get(k);
//System.out.println("\t\tCONSIDERING THE CANDIDATE AT THE "+candidate.getPositionInList()+" POSITION FOR PARTY "+candidate.getPartyName());
					totalVotes.remove(candidate);
					tempListOfCandidates.remove(candidate);

					// get the remaining votes for this candidate
					int votesNotUsed=(remainingVotes.get(candidate)).intValue();
//System.out.println("surplus votes for the candidate "+votesNotUsed);
//					// find in totalVotes the other candidates from the same party
//					HashMap samePartyCandidates=new HashMap();
//					Set keys=totalVotes.keySet();
//					Iterator c=keys.iterator();
//					while(c.hasNext())
//						{
//						Candidate cand=(Candidate)c.next();
//						Integer voti=(Integer)totalVotes.get(cand);
//						if (cand.getPartyName()==candidate.getPartyName())
//							{
//							samePartyCandidates.put(cand,voti);
//							}
//						}
//					// if there are still candidates of the same party, allocate the votes.
//					// Otherwise the votes are lost
//					if (samePartyCandidates.size()>0)
//						{
//						// find the candidate with the highest number of votes
//						Candidate cand=getKeyForMaxValue(samePartyCandidates);
//						// find the number of votes and add votesNotUsed
//						int voti=((Integer)samePartyCandidates.get(cand)).intValue();
//						voti+=votesNotUsed;
//						// update the number of votes in totalVotes for this candidate
//						totalVotes.remove(cand);
//						totalVotes.put(cand,new Integer(voti));
//						}
					// get the list of electors who have elected this candidate (he is their first preference) get them from totalVoters
//Set elkeys=totalVoters.keySet();
//Iterator elit=elkeys.iterator();
//while (elit.hasNext())
//	{
//	Candidate elcand=(Candidate)elit.next();
//	LinkedList el=(LinkedList)totalVoters.get(elcand);
//System.out.println("candidate at "+elcand.getPositionInList()+"th pos. of party "+elcand.getPartyName()+" has "+el.size()+" electors");
//	}
//					LinkedList listOfVotersPref=(LinkedList)((LinkedList)totalVoters.get(candidate)).clone();

					LinkedList <Voter> listOfVotersPref = new LinkedList<Voter>();
					LinkedList <Voter> origin = totalVoters.get(candidate);
					Iterator <Voter> o = origin.iterator();
					while (o.hasNext()){
						listOfVotersPref.add(o.next());
					}


					totalVoters.remove(candidate);
//					for (int e=0;e<listOfVoters.size();++e)
//						{
//						Voter voter=(Voter)listOfVoters.get(e);
//						Candidate candidatoE=voter.getFirstCandidatePreference(candidatesElectedBefore);
//						if ((candidatoE.getPartyName()==candidate.getPartyName())&&(candidatoE.getPositionInList()==candidate.getPositionInList()))
//							{
//							listOfVotersPref.add(voter);
//							}
//						}
//System.out.println("candidate is at the "+candidate.getPositionInList()+"th position of party "+candidate.getPartyName());
//System.out.println(listOfVotersPref.size()+" electors have this candidate as first preference");
					// get a random number of them corresponding to votesNotUsed and give a vote to the next non-elected
					// candidate in their preferences. Add this vote to the total obtained by the candidate in totalVotes.
					// candidate in their preferences. Add this vote to the total obtained by the candidate in totalVotes.
					while (votesNotUsed>0)
						{
						int index=generator.nextInt(listOfVotersPref.size());
						Voter voter=listOfVotersPref.get(index);
//System.out.println("index: "+index);
//System.out.println("voter "+voter.getNameElector());
						// find in the list of electors the next voter in the preferences not yet elected, remove the voter from
						// listOfVotersPref, decrement votesNotUsed
						LinkedList <Candidate> candidatesPreferences=voter.getCandidatePreferences();
						for (int c=0;c<candidatesPreferences.size();++c)
							{
							int inTotalVotes=0;
							Candidate cand=candidatesPreferences.get(c);
//System.out.println("the current candidate is the "+cand.getPositionInList()+"th in the list for party "+cand.getPartyName());
							int posCand=cand.getPositionInList();
							int partCand=cand.getPartyName();
							Set <Candidate> tkeys=totalVotes.keySet();
							Iterator <Candidate> tk=tkeys.iterator();
							while(tk.hasNext())
								{
								Candidate cadd=tk.next();
								if ((cadd.getPositionInList()==posCand)&&(cadd.getPartyName()==partCand))
									{
									++inTotalVotes;
									}
								}
							int elected=0;
							for (int ca=0;ca<electedCandidates.size();++ca)
								{
								Candidate candel=electedCandidates.get(ca);
								int posCandel=candel.getPositionInList();
								int partCandel=candel.getPartyName();
								if ((posCandel==posCand)&&(partCandel==partCand))
									{
									++elected;
									}
								}
//System.out.println("elected is "+elected+" and totalvotes contains the candidate "+inTotalVotes);
							if ((elected==0)&&(inTotalVotes>0))// if not elected, add a vote for this candidate in totalVotes
								{
//System.out.println("the current candidate is the "+cand.getPositionInList()+"th in the list for party "+cand.getPartyName());
//System.out.println("not elected, add a vote to totalVotes and the voter to templistOfVoters");
								// loop through totalVotes, find the candidate coresponding to the current one, get its votes
								// and add 1 to them
								Set <Candidate> keys=totalVotes.keySet();
								Iterator <Candidate> ke=keys.iterator();
								while (ke.hasNext())
									{
									Candidate candt=ke.next();
									int posdd=candt.getPositionInList();
									int partdd=candt.getPartyName();
									if ((posdd==posCand)&&(partdd==partCand))
										{
										int votes=(totalVotes.get(candt)).intValue();
										++votes;
//System.out.println("increased votes by 1: "+votes);								
										totalVotes.remove(candt);
//System.out.println("removed candidate from totalVotes");								
										totalVotes.put(candt,new Integer(votes));
//System.out.println("totalVotes updated");
										// get the list of electors from totalVoters and add the current voter to it
										LinkedList <Voter> templistOfVoters=totalVoters.get(candt);
										templistOfVoters.add(voter);
										totalVoters.remove(candt);
										totalVoters.put(candt,templistOfVoters);
										break;
										}
									}
								// votes now attributed, break loop on candidates and stard with next voter
								--votesNotUsed;
//System.out.println("decrease votesNotUsed which becomes "+votesNotUsed);
								listOfVotersPref.remove(voter);
//System.out.println("remove voter from listOfVotersPref which has now "+listOfVotersPref.size()+" elements");
								break;
								}
							else
								{
//System.out.println("elected, continue");
								continue;
								}
							}// end for c
						} // end while votesNotUsed
					}// end of for candidatesElectedThisTurn
				}// end of else: candidatesElectedThisTurn!=0
			}// end of while numberCandidatesToAttribute
System.out.println("returning seats of district");
		return seatsOfDistrict;
		}// end of shareVST method

	// function to find the totalVotes for each candidate of each party, based on the preference
	// for the candidate in the specified position
	LinkedList <Voter> getTotalVotesCandidates(Candidate candidate,LinkedList <Voter> listOfVoters,int position)
		{
//		int totalVotes=0;
		LinkedList <Voter> totalCandidates=new LinkedList<Voter>();
		// for each voter
		for (int e=0;e<listOfVoters.size();++e)
			{
			Voter voter=listOfVoters.get(e);
			LinkedList <Candidate> prefs=voter.getCandidatePreferences();
			Candidate preferredCandidate=prefs.get(position-1);
			if ((preferredCandidate.getPartyName()==candidate.getPartyName())&&(preferredCandidate.getPositionInList()==candidate.getPositionInList()))
				{
//				++totalVotes;
				totalCandidates.add(voter);
				}
			}
		// return totalCandidates, its size is the number of totalVotes for the candidate.
		return totalCandidates;
		}// end of getTotalVotesCandidates method

	// function that sorts the values in a map in decreasing or increasing order of the keys
	// returns a list containing the keys.
	// the arguments are the map and the order of the sorting (ascending or descending)
	// overrides the function defined in proporzionalePlurinominale as the keys are candidates, not Integers
	public LinkedList <Candidate> sortMapCandidates(HashMap <Candidate,Integer> map,String order)
		{
System.out.println("entered sortMapCandidates describe map:");
Set cands = map.keySet();
Iterator c = cands.iterator();
while (c.hasNext()) {
	Candidate cd = (Candidate)c.next();
System.out.println("candidate "+cd+" has "+(Integer)map.get(cd)+" votes");
}
//		HashMap clone=(HashMap)map.clone();
		HashMap <Candidate,Integer> clone = new HashMap<Candidate,Integer>();
		Set <Candidate> keysc = map.keySet();
		Iterator <Candidate> kc = keysc.iterator();
		while (kc.hasNext()) {
			Candidate cand = kc.next();
			Integer val = map.get(cand);
			clone.put(cand,val);
		}
		LinkedList <Candidate> orderedValues=new LinkedList<Candidate>();
		// while the number of elements in orderedKeys is smaller then size of map
		while (orderedValues.size()<map.size())
			{
			// find the index corresponding to the minimum value in clone
			Candidate index=getCandidateForMinValue(clone);
			// add to orderedKeys: if order="ascending", use addLast, else if "descending" use addFirst
			if (order.compareTo("ascending")==0)
				{
				orderedValues.addLast(index);
				}
			else if (order.compareTo("descending")==0)
				{
				orderedValues.addFirst(index);
				}
			else
				{
				System.out.println("the order "+order+" does not exist. You can only use \"ascending\" or \"descending");
				System.exit(0);
				}
			// remove the key/value combination from clone
			clone.remove(index);
			}
		return orderedValues;
		}// end of sortMap function

		
	Candidate getCandidateForMaxValue(HashMap <Candidate,Integer> map)
		{
System.out.println("entered getKeyfor max value");
		Collection <Integer> mapValues=map.values();
		Iterator <Integer> i=mapValues.iterator();
		int max=0;
		while(i.hasNext())
			{
			int val=(i.next()).intValue();
			max=(val>max)?val:max;
			}
		Set <Candidate> keys=map.keySet();
		Iterator <Candidate> j=keys.iterator();
		Candidate key=new Candidate(0,0);
		while (j.hasNext())
			{
			key=j.next();
			int val=(map.get(key)).intValue();
			if (val==max)
				{
				break;
				}
			}
		return(key);
		}


	Candidate getCandidateForMinValue(HashMap <Candidate,Integer> map)
		{
System.out.println("entered get key for min value, map has "+map.size()+" elements");
		Collection <Integer> mapValues=map.values();
		Iterator <Integer> i=mapValues.iterator();
		int min=Integer.MAX_VALUE;
System.out.println("initial minimum: "+min);
		while(i.hasNext())
			{
			int val=(i.next()).intValue();
			min=(val<min)?val:min;
System.out.println("val is "+val+" so min becomes "+min);
			}
		Set <Candidate> keys=map.keySet();
System.out.println("there are "+keys.size()+" elements in set keys");
		Iterator <Candidate> j=keys.iterator();
		Candidate key=new Candidate(0,0);
		while (j.hasNext())
			{
System.out.println("considering candidate ");
			key=j.next();
System.out.println(key);
			int val=(map.get(key)).intValue();
System.out.println("has votes of "+val);
			if (val==min)
				{
				break;
				}
			}
		return(key);
		}
		
	public String getRounding()
		{
		return rounding;
		}

	
	}// end of class definition
