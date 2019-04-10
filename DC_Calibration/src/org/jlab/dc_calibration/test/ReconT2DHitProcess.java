// Filename: ReconT2DHitProcess.java
// Description: 
// Author: Latif Kabir < latif@jlab.org >
// Created: Fri Sep  8 23:58:45 2017 (-0400)
// URL: latifkabir.github.io

package org.jlab.dc_calibration.test;


import org.jlab.rec.dc.hit.FittedHit;

public class ReconT2DHitProcess
{

    public static void main(String[] args)
    {
	int sector = 2;
	int superlayer = 1;
	int layer = 1;
	int wire = 1;
	double time = 100;
	double docaEr = 0.0;
	double B = 0.0;
	int Id = 1;
	
	FittedHit dcHit = new FittedHit(sector, superlayer, layer, wire, time, docaEr, B, Id);
	// System.out.println(dcHit.printInfo());
	// System.out.println("Time: " + dcHit.get_Time() + " Distance: " + dcHit.get_TimeToDistance());
    }
    
}
