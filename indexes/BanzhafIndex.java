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
package indexes;

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
import java.beans.*;

import classesWrittenByOthers.*;
import globals.*;
import parliaments.*;
import votingObjects.*;
import gui.*;

public class BanzhafIndex extends ShapleyValue//// implements ParliamentEventListener
	{

	public BanzhafIndex(Parliament parl, HashMap<Integer,Integer> map) {
		super(parl, map);
	}

	protected Object computeIndex() {
		Object obj = super.computeIndex();
		String result = new String();
		Iterator <Party> p = arrayOfParties.iterator();
		double sum = 0;
		while (p.hasNext()) {
			Party pa = p.next();
			Integer pname = pa.getName();
			sum += (shapleyValue.get(pname.intValue())).doubleValue();
		}

		HashMap <Integer,Double> banzhafIndex = new HashMap<Integer,Double>();
		p = arrayOfParties.iterator();
		while (p.hasNext()) {
			Party pa = p.next();
			Integer pname = pa.getName();
			double v = (shapleyValue.get(pname.intValue())).doubleValue()/sum;
			banzhafIndex.put(pname,new Double(v));
			DecimalFormat df = new DecimalFormat("##.####");
			result += pa.getNameParty()+": "+df.format(v)+" <br> ";
			indexValue=banzhafIndex;
			setPowerMap(banzhafIndex);
		}
System.out.println("Banzhaf index: "+banzhafIndex.toString());
		return(result);	
		
	}
	
	public double computeCoefficient (int si,int nbParties) {
//System.out.println("using banzhaf computeCoefficient");
		return  (double)1/Math.pow(2,(nbParties-1));
	}

}