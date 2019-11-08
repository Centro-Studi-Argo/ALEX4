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
package parliaments;

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

public class GenericParliament extends Parliament {

	// constructor
	public GenericParliament() {
		super();
	}

	public GenericParliament(HashMap <String,String> dataFromFile) {
		super();
//System.out.println(dataFromFile.toString());
		int numberVoters = (new Integer(dataFromFile.get("numberVoters"))).intValue();
		sizeOfParliament = (new Integer(dataFromFile.get("numberUninominalDistricts"))).intValue();
		totalNumberVoters = numberVoters * sizeOfParliament;
		numberCandidates = (new Integer(dataFromFile.get("numberCandidates"))).intValue();
		int numberParties = (new Integer(dataFromFile.get("numberParties"))).intValue();

		if (dataFromFile.containsKey("majorityLevel")){
System.out.println("in generic parliament, key exists in dataFromFile");
			majorityLevel = (new Double(dataFromFile.get("majorityLevel"))).doubleValue();
		}
System.out.println("in generic parliament, majoritylevel: "+majorityLevel);		
		arrayOfParties = new ArrayList<Party>();
		proportionalAllocationOfSeats = new HashMap<Integer,Integer>();
		allocationOfSeats = new HashMap<Integer,Integer>();
		votesPerParty = new HashMap<Integer,Integer>();
		votesPerPartyRef = new HashMap<Integer,Integer>();
		int sumOfSeats = 0;
		for (int i=1;i<=numberParties;++i) {
			int name = i;
			String nameParty = dataFromFile.get("party_"+i+"_name");
			double share = (new Double(dataFromFile.get("party_"+i+"_share"))).doubleValue();
			int concentration = (new Integer(dataFromFile.get("party_"+i+"_concentration"))).intValue();
			double coefficient = (new Double(dataFromFile.get("party_"+i+"_coefficient"))).doubleValue();
			boolean major = (new Boolean(dataFromFile.get("party_"+i+"_major"))).booleanValue();
			double distance = (new Double(dataFromFile.get("party_"+i+"_distance"))).doubleValue();
			arrayOfParties.add(new Party(name,nameParty,share,concentration,coefficient,major,distance));

			proportionalAllocationOfSeats.put(new Integer(i),new Integer(dataFromFile.get("refAllocSeats_party_"+i)));
			allocationOfSeats.put(new Integer(i),new Integer(dataFromFile.get("allocSeats_party_"+i)));
			sumOfSeats += new Integer(dataFromFile.get("allocSeats_party_"+i)).intValue();
			votesPerParty.put(new Integer(i),new Integer(dataFromFile.get("votes_party_"+i)));
			votesPerPartyRef.put(new Integer(i),new Integer(dataFromFile.get("refVotes_party_"+i)));

			parliamentKey = dataFromFile.get("parliament_name");
		}
		if (sumOfSeats != sizeOfParliament) {
			sizeOfParliament = sumOfSeats;
		}
	}

	public String getParliamentName() {
		return parliamentKey;
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}
	public String getNameDescrSimulation() {
		return ("-");
	}
	// methods
	// method to allocate the seats (current method)
	// in each district, the winning party is the one who has the most votes
	public HashMap <Integer,Integer> findAllocationOfSeats() {
		return(allocationOfSeats);
	}


}