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
package graphs;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import java.awt.Toolkit;
import java.text.*;
import java.io.*;

import votingObjects.*;

public class SemiCircle extends JPanel {
	HashMap <Integer,Integer> allocationOfSeats;
	HashMap <String,Double> powerIndex;
    final Color bg = Color.white;
	int sizeOfParliament;
	Dimension dimension;
	ArrayList <Party> arrayOfParties;
	
//	public SemiCircle(HashMap allocSeats,int sizeParl,Dimension dim) {
//		allocationOfSeats = allocSeats;
//		sizeOfParliament = sizeParl;
//		dimension = dim;
//		setPreferredSize(dimension);
//	    setBackground(bg);
//	}

	public SemiCircle(HashMap <Integer,Integer> allocSeats,Dimension dim,ArrayList <Party> array) {
		allocationOfSeats = allocSeats;
System.out.println("for semi circle, allocation of seats : "+allocationOfSeats.toString());
//		sizeOfParliament = sizeParl;
		arrayOfParties = array;
System.out.println("in graph: array of parties is : " + arrayOfParties);
		Collection <Integer> values = allocationOfSeats.values();
		Iterator <Integer> it = values.iterator();
		sizeOfParliament = 0;
		while (it.hasNext()) {
			Integer i = it.next();
			sizeOfParliament += i.intValue();
		}
		dimension = dim;
		setPreferredSize(dimension);
	    setBackground(bg);
	}


	public void paintComponent(Graphics g) {
		super.paintComponent(g); // paints the background
		int ox=0;
		int oy=0;
		float quantityColor=0;
		int startingDegrees=0;
		int centerGraph = (int)dimension.width/2;
		int graphHeight = (int)dimension.height/2;
		int graphWidth = dimension.width;
		// create a list of all parties with at least 1 seat
		int n=allocationOfSeats.size();
//System.out.println("n "+n);
//System.out.println("partiesIn "+partiesIn.toString());
		Color c;
		// now draw the graph based on the partiesIn vector: for the last party in the vector,
		// the size of the "slice" goes up to 180 (otherwise, there may be some rounding going
		// on and the graph does not look finished)
//		int varPosition=dimension.width/arrayOfParties.size();
		int pos = 0;
		Set <Integer>ka = allocationOfSeats.keySet();
		Iterator <Integer> it = ka.iterator();
		while (it.hasNext()) {
			Integer kit = it.next();
			Integer val = allocationOfSeats.get(kit);
			if (val.intValue()>0) {
			++pos;
			}
		}
		for (int j=arrayOfParties.size()-1;j>=0;--j) {
			Party party = arrayOfParties.get(j);
			int nameOfParty=party.getName();
//System.out.println("graph: index j is "+j+" corresponding to party "+nameOfParty);
			int nbSeatsForParty=((Integer)allocationOfSeats.get(new Integer(nameOfParty))).intValue();
//System.out.println("graph, for party "+nameOfParty+" seats are "+nbSeatsForParty+" - size of parliament "+sizeOfParliament);
			int degrees = 0;
			float qcol = party.getColor();
			if (j>0) {
				if (qcol==-1.0) {
//					quantityColor+=(float)nbSeatsForParty/sizeOfParliament;
					quantityColor=(float)j/arrayOfParties.size();
System.out.println("j="+j+" and nbparties="+arrayOfParties.size()+" so quantityColor "+quantityColor);
					party.setColor(quantityColor) ;
				} else {
					quantityColor = qcol;
				}
				degrees=Math.round(nbSeatsForParty*(float)180/sizeOfParliament);
				c=Color.getHSBColor((float) quantityColor, 1.0f, 1.0f);
//				g.setColor(c);
//				g.fillArc(ox,oy,graphWidth,graphWidth,startingDegrees,degrees);
//				startingDegrees+=degrees;
			} else {
				if (qcol==-1.0) {
//					quantityColor+=(float)nbSeatsForParty/sizeOfParliament;
					quantityColor=(float)j/arrayOfParties.size();
System.out.println("j="+j+" and nbparties="+arrayOfParties.size()+" so quantityColor "+quantityColor);
					party.setColor(quantityColor);
				} else {
					quantityColor = qcol;
				}
				degrees=180-startingDegrees;
				c=Color.getHSBColor(quantityColor, 1.0f, 1.0f);
//				g.setColor(c);
//				g.fillArc(ox,oy,graphWidth,graphWidth,startingDegrees,degrees);
			}
			g.setColor(c);
			if (nbSeatsForParty>0) {
				g.fillArc(ox,oy,graphWidth,graphWidth,startingDegrees,degrees);
				startingDegrees+=degrees;
				int startx = (pos%2 == 0) ? ox : ox+(int)(dimension.width/2);
				int starty = oy+graphHeight+10+((int)(pos/2)*20);
//System.out.println("party "+party.getNameParty()+" pos: "+pos+" startx "+startx+" starty "+starty);
				--pos;
				g.fillRect(startx,starty,10,10);
				g.setColor(Color.black);
//			g.drawString((new Integer(nameOfParty)).toString(),startx+20,starty+10);
				g.drawString(party.getNameParty(),startx+20,starty+10);
			}
		}
		g.setColor(Color.black);
		g.drawLine(0,centerGraph,graphWidth,centerGraph);
		g.drawArc(ox,oy,graphWidth,graphWidth,0,180);
	}

}



