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

public class StrategicVoteMixedMemberI extends MixedMemberI {

	int thisAllocSeats = 1;
	int maxValue = 100;
	
	// constructor

	public StrategicVoteMixedMemberI(SimulationRepository sr) {
		super(sr,true);
System.out.println(language.getString("uninominal","StrategicVoteMixedMemberI")+" created");
System.out.println("now valuePercProp is "+valuePercProp+" and kForDistance is "+kForDistance);
	}

	public String getParliamentName() {
		return (language.getString("uninominal","StrategicVoteMixedMemberI")+" - "+valuePercProp+"%"+" - "+language.getString("labels","weightDistanceShort")+"="+kForDistance+" - "+language.getString("labels","coalitions")+"="+nameOfGroupOfCoalitions+" - "+language.getString("labels","majorityLevel")+": "+majorityLevel);
	}
	
	public String getParliamentKey() {
		return parliamentKey;
	}


	
	// method to allocate the seats (current method)
	// in each district, the winning party is the one who has the most votes
	public HashMap <Integer,Integer> findAllocationOfSeats() {
		return super.findAllocationOfSeats();
	}
}