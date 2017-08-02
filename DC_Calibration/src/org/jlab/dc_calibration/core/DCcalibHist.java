/**
 * @author Latif Kabir < jlab.org/~latif >
 *
 */
package org.jlab.dc_calibration.core;

import java.io.File;

import org.jlab.ccdb.Stopwatch;
import org.jlab.geom.base.Sector;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.ui.TCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;


import org.freehep.math.minuit.FCNBase;
import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnStrategy;
import org.freehep.math.minuit.MnUserParameters;


public class DCcalibHist
{
	HipoDataSource reader = new HipoDataSource();
	String bankName = "TimeBasedTrkg::TBHits";
	TCanvas canvas = new TCanvas("c1", 800, 500);
	H2F histogram = new H2F("trkDoca vs time", 100, 0.0, 2.0, 100, -150, 1500);
	String pathPrefix = "/home/siplu/GIT/JLAB/DATA/clas/outP1p1_r";
	String filePath;
	int runNumber;
	int minRange;
	int maxRange;

	/**
	 * Constructor for DC calibration histogram
	 */
	public DCcalibHist(int run_number, int min_range, int max_range)
	{
		runNumber = run_number;
		minRange = min_range;
		maxRange = max_range;
	}

	public GraphErrors FillHist()
	{
		for (int i = minRange; i <= maxRange; i++)
		{
			filePath = pathPrefix + Integer.toString(runNumber) + "_" + Integer.toString(i) + ".hipo";
			// filePath = pathPrefix + Integer.toString(runNumber) + "_" +
			// String.format("%05d", i);
			System.out.println(filePath);

			if (!(new File(filePath).exists()))
			{
				System.out.println("\nThe run: " + filePath + " does NOT exist ..... SKIPPED.");
				continue;
			}
			else
				System.out.println("\nNow filling run: " + filePath);
			reader.open(filePath);
			DataEvent event;
			DataBank bank;

			// --------------- Required variables from TBHits
			// bank---------------------
			short TBHits_status;
			byte TBHits_sector;
			byte TBHits_superlayer;
			byte TBHits_layer;
			short TBHits_wire;
			float TBHits_time;
			float TBHits_doca;
			float TBHits_trkDoca;
			float TBHits_timeResidual;
			float TBHits_B;

			while (reader.hasEvent())
			{
				event = reader.getNextEvent();
				if (!event.hasBank(bankName))
					continue;

				bank = event.getBank(bankName);
				for (int k = 0; k < bank.rows(); k++)
				{
					// --------- Get desired values from TBHits bank
					// --------------
					TBHits_status = bank.getShort("status", k);
					TBHits_sector = bank.getByte("sector", k);
					TBHits_superlayer = bank.getByte("superlayer", k);
					TBHits_layer = bank.getByte("layer", k);
					TBHits_wire = bank.getShort("wire", k);
					TBHits_time = bank.getFloat("time", k);
					TBHits_doca = bank.getFloat("doca", k);
					TBHits_trkDoca = bank.getFloat("trkDoca", k);

					if (TBHits_sector == 2 && TBHits_superlayer == 1)
						histogram.fill(TBHits_trkDoca, TBHits_time);
				}
			}
			reader.close();
		}
		GraphErrors gr = histogram.getProfileX(); 
		canvas.draw(gr);
		//canvas.draw(histogram);
		return gr;
	}

	
	public void GetDataSet()
	{
		GraphErrors gr = FillHist();
		double[] X = new double[100];
		double[] Y = new double[100];
		
		for(int i = 0; i<100; i++)
		{
			System.out.println(i + ": " + gr.getDataX(i) +"\t " + gr.getDataY(i) + "\t" + gr.getDataEX(i) + 
					"\t" + gr.getDataEY(i));
			X[i] = gr.getDataX(i);
			Y[i] = gr.getDataY(i);
		}
	}
	
	
	//------------------- The fitting -----------------------------------------------------
	public void DoFitting(String[] args)
	{
		double[] x = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
		double[] y = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
		double[] theXvalues = x.clone();
		double[] measurements = y.clone();

		int npars = 2;

		double aLen = measurements.length;
		System.out.println("Size or length of array 'measurements' is " + aLen);
		System.out.print("xArray[] = ");
		printArray(theXvalues);

		KrishnaFcn theFCN = new KrishnaFcn(theXvalues, measurements);

		MnUserParameters upar = new MnUserParameters();
		upar.add("p0", 0.0, 0.001);
		upar.add("p1", 1.5, 0.001);

		System.out.println("Initial parameters: " + upar);

		System.out.println("start migrad");
		MnMigrad migrad = new MnMigrad(theFCN, upar);
		FunctionMinimum min = migrad.minimize();

		if (!min.isValid())
		{
			// try with higher strategy
			System.out.println("FM is invalid, try with strategy = 2.");
			MnMigrad migrad2 = new MnMigrad(theFCN, min.userState(), new MnStrategy(2));
			min = migrad2.minimize();
		}

		System.out.println("minimum: " + min);

		System.out.println("kp: ===================================== ");

		MnUserParameters userpar = min.userParameters();
		System.out.println("par0 = " + userpar.value("p0") + " +/- " + userpar.error("p0"));
		System.out.println("par1 = " + userpar.value("p1") + " +/- " + userpar.error("p1"));
		System.out.println("kp: ===================================== ");

	}

	static void printArray(double[] array)
	{
		double aLen = array.length;
		for (int i = 0; i < aLen; i++)
		{
			if (i > 0 && i % 10 == 0)
			{
				System.out.println("");
			}
			System.out.print(array[i] + " ");
		}
		System.out.println("");
	}

	// static class ReneFcn implements FCNBase
	static class KrishnaFcn implements FCNBase
	{
		private double[] theXvalues;
		private double[] theMeasurements;

		KrishnaFcn(double[] xVals, double[] meas)
		{
			theXvalues = xVals;
			theMeasurements = meas;
		}

		public double errorDef()
		{
			return 1;
		}

		public double valueOf(double[] par)
		{
			double m = par[1]; // straight line equation: y = m*x + c
			double c = par[0];
			double chisq = 0.0;
			for (int i = 0; i < theMeasurements.length; i++)
			{
				double yi = theMeasurements[i]; 

				double xi = theXvalues[i]; 
				double ei = yi;
				double nexp = m * xi + c;
				chisq += (yi - nexp) * (yi - nexp);/// ei;
			}
			return chisq;
		}

	}

	
	
	public static void main(String arg[])
	{
		Stopwatch timer = new Stopwatch();
		timer.start();
		DCcalibHist test = new DCcalibHist(810, 22, 42);
//	    test.FillHist();
		//test.FillHistTest();
		test.GetDataSet();
		timer.stop();
		System.out.println(timer.getElapsedTime());
	}

}
