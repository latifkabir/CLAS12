// Filename: ReconTimeFunction.java
// Description: Plot the DC time function used in the reconstruction
// Author: Latif Kabir < latif@jlab.org >
// Created: Sat Aug 26 10:24:56 2017 (-0400)
// URL: latifkabir.github.io

package org.jlab.dc_calibration.test;

import org.jlab.groot.ui.TCanvas;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;
import org.jlab.rec.dc.CalibrationConstantsLoader;
import org.jlab.dc_calibration.ui.CalibStyle;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;

public class ReconTimeFunction
{
	int SL_index;
	int sec_index;
	double distance;
	
	TimeToDistanceEstimator recFnc = new TimeToDistanceEstimator();
	
    public ReconTimeFunction(int SecIndex,int SLindex)
    {
    	sec_index = SecIndex;
    	SL_index = SLindex;
    	
		CalibrationConstantsLoader.Load(1000, "default");
		TableLoader.Fill();
	 }

    public GraphErrors getGraph(double bField, double angDegree, double minTime, double maxTime)
	{
    	GraphErrors gr = new GraphErrors();

    	for (double i = minTime; i< maxTime; i = i + 1.0)
		{			
    		distance = recFnc.interpolateOnGrid(bField, angDegree, i, sec_index, SL_index);
    		gr.addPoint(distance, i, 0, 0);
		}
    	
    	return gr;
	}
    
     // --------------------> Run this call to main() from Emacs. Eclipse throws an exception for the loop used for a possible bug in groot <-------------- 
    public static void main(String[] args)
    {
    	CalibStyle.setStyle();
    	GStyle.getGraphErrorsAttributes().setMarkerSize(2);
		GStyle.getGraphErrorsAttributes().setTitleX("Distance [cm]");;
		GStyle.getGraphErrorsAttributes().setTitleY("Time [ns]");;
		
		TCanvas c1 = new TCanvas("c1", 800, 600);
		c1.setTitle("Distance (cm) vs Time (ns)");
		
		double maxTime = 200;
		double minTime = 0;
		
		int secIndex = 0;
		int slIndex = 0;
		double bField = 0.0;

		GStyle.getGraphErrorsAttributes().setTitle("Time (ns) vs Distance (cm) for S " + (secIndex + 1) + " SL " + (slIndex + 1) + " from Reconstruction");
		
		ReconTimeFunction recon = new ReconTimeFunction(secIndex, slIndex);

		// c1.draw(recon.getGraph(bField, 30, minTime, maxTime));		
		for(double angDegree = 0; angDegree <= 30; angDegree += 5)
		{
		    GStyle.getGraphErrorsAttributes().setMarkerColor((int)(Math.abs(angDegree)/5) + 1);
			c1.draw(recon.getGraph(bField, angDegree, minTime, maxTime),"same");
		}

		// for( bField = 0.0; bField <= 1.5; bField += 0.5)
		// {
		//     for(double angDegree = 0; angDegree <= 30; angDegree += 5)
		//     {
		// 	GStyle.getGraphErrorsAttributes().setMarkerColor((int)(angDegree/5) + 1);
		// 	c1.draw(recon.getGraph(bField, angDegree, minTime, maxTime),"same");
		//     }		
		// }
    }    
}
