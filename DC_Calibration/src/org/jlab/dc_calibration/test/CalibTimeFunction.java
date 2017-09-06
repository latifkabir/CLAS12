// Filename: CalibTimeFunction.java
// Description: Plot DC time function from calibration suite
// Author: Latif Kabir < latif@jlab.org >
// Created: Sat Aug 19 23:32:29 2017 (-0400)
// URL: latifkabir.github.io

package org.jlab.dc_calibration.test;

import org.jlab.groot.math.Func1D;
import org.jlab.groot.ui.TCanvas;
import static org.jlab.dc_calibration.constants.Constants.wpdist;

import org.jlab.dc_calibration.fit.DCTimeFunction;
import org.jlab.dc_calibration.io.ReadT2DparsFromCCDB;
import org.jlab.dc_calibration.ui.CalibStyle;
import org.jlab.groot.base.GStyle;

public class CalibTimeFunction extends Func1D
{
	int		secIndex	= 0;
	int		slIndex		= 0;
	double	angDeg			= 0;
	double	bField			= 0;

	int		nParam		= 10;					// Number of parameters
	double	params[]	= new double[nParam];

	public CalibTimeFunction(String name, double min, double max)
	{
		super(name, min, max);
	}

	public void setValues(int sec_index, int sl_index, double b_field, double ang)
	{
		secIndex = sec_index;
		slIndex = sl_index;
		bField = b_field;
		angDeg = ang;
	}

	@Override
	public void setParameters(double[] values)
	{
		for (int i = 0; i < (nParam - 1); i++)
			params[i] = values[i];

		params[nParam - 1] = 0.0; // Delta_T_0 constant not in CCDB
	}

	@Override
	public double evaluate(double x)
	{
		DCTimeFunction dcTimeFnc = new DCTimeFunction(slIndex, angDeg, x / (2.0 * wpdist[slIndex]), bField, params);
		return dcTimeFnc.nonLinearFit();
	}

	public static void main(String[] args)
	{
		CalibStyle.setStyle();
		GStyle.getFunctionAttributes().setTitleX("Distance [cm]");
		GStyle.getFunctionAttributes().setTitleY("Time [ns]");

		TCanvas c1 = new TCanvas("c1", 800, 600);
		c1.setTitle("Distance (cm) vs Time (ns)");

		int secIndex = 0;
		int slIndex = 0;
		double bField = 0.0;
		double angDegree = 30.0;
		double maxRange = 0.8;
		double minRange = 0.0;

		GStyle.getFunctionAttributes()
				.setTitle("Time vs Distance for S = " + (secIndex + 1) + " SL = " + (slIndex + 1));

		ReadT2DparsFromCCDB defPars = new ReadT2DparsFromCCDB("default",1000);
		defPars.LoadCCDB();

		for (angDegree = 0.0; angDegree <= 30.0; angDegree += 5)
		{
			CalibTimeFunction myFnc = new CalibTimeFunction("myFnc", minRange, maxRange * Math.cos(Math.toRadians(30 - angDegree)));
			myFnc.setParameters(defPars.parsFromCCDB[secIndex][slIndex]);
			myFnc.setValues(secIndex, slIndex, bField, angDegree);
			myFnc.setLineColor((int) (angDegree / 5) + 1);
			c1.draw(myFnc, "same");
		}
	}
}
