/**
 * @author Latif Kabir < jlab.org/~latif >
 *
 */
package org.jlab.dc_calibration.test;

import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.ui.TCanvas;
import org.jlab.rec.dc.CalibrationConstantsLoader;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;

public class TestCalcTime
{

	public static void main(String[] args)
	{
		GStyle.getGraphErrorsAttributes().setMarkerSize(1);
				
		TCanvas c1 = new TCanvas("c1", 800, 600);
		c1.setTitle("Distance (cm) vs Time (ns)");
		int superLaterIndex = 2;

		CalibrationConstantsLoader.Load(10, "default");
		TableLoader.Fill();
		
		TimeToDistanceEstimator recFnc = new TimeToDistanceEstimator();

		GraphErrors gr1 = new GraphErrors();
		GraphErrors gr2 = new GraphErrors();
		GraphErrors gr3 = new GraphErrors();
		GraphErrors gr4 = new GraphErrors();
		GraphErrors gr5 = new GraphErrors();
		GraphErrors gr6 = new GraphErrors();
		GraphErrors gr7 = new GraphErrors();

	
		for (double i=0; i< 200; i=i+1.0)
		{			
			double dist = recFnc.interpolateOnGrid(0, 0.0, i, 0, superLaterIndex);
			//System.out.println("Time "+ i + " dist " + dist);
			gr1.addPoint(dist, i, 0, 0);
			
			dist = recFnc.interpolateOnGrid(0, 5.0, i, 0, superLaterIndex);
			gr2.addPoint(dist, i, 0, 0);

			dist = recFnc.interpolateOnGrid(0, 10.0, i, 0, superLaterIndex);
			gr3.addPoint(dist, i, 0, 0);

			dist = recFnc.interpolateOnGrid(0, 15.0, i, 0, superLaterIndex);
			gr4.addPoint(dist, i, 0, 0);

			dist = recFnc.interpolateOnGrid(0, 20.0, i, 0, superLaterIndex);
			gr5.addPoint(dist, i, 0, 0);

			dist = recFnc.interpolateOnGrid(0, 25.0, i, 0, superLaterIndex);
			gr6.addPoint(dist, i, 0, 0);
						
			dist = recFnc.interpolateOnGrid(0, 30.0, i, 0, superLaterIndex);
			//System.out.println("Time "+ i + " dist " + dist);
			gr7.addPoint(dist, i, 0, 0);

		}						
		gr1.setTitleX("Distance [cm]");
		gr1.setTitleY("Time [ns]");
		c1.draw(gr1);
		c1.draw(gr2,"same");
		c1.draw(gr3,"same");
		c1.draw(gr4,"same");
		c1.draw(gr5,"same");
		c1.draw(gr6,"same");
		c1.draw(gr7,"same");

	}
}
