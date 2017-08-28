/*
 * Wanted to compare my and Veronique's calc times but didn't complete the task.
 */
package org.jlab.dc_calibration.test;

import static org.jlab.dc_calibration.constants.Constants.nFitPars;
import static org.jlab.dc_calibration.constants.Constants.nSL;
import static org.jlab.dc_calibration.constants.Constants.nSectors;
import static org.jlab.dc_calibration.constants.Constants.wpdist;

import javax.swing.JFrame;

import org.jlab.dc_calibration.fit.DCTimeFunction;
import org.jlab.dc_calibration.io.ReadT2DparsFromCCDB;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.rec.dc.CCDBConstants;
import org.jlab.rec.dc.CalibrationConstantsLoader;
import org.jlab.rec.dc.timetodistance.TableLoader;

/**
 *
 * @author kpadhikari
 */
public class Test_calc_Time
{
	private double[][][] parsFromCCDB_default = new double[nSectors][nSL][nFitPars];// nFitPars = 9
	private double[][][] parsFromCCDB_dc_test1 = new double[nSectors][nSL][nFitPars];// nFitPars = 9
	GraphErrors myFunc = new GraphErrors();
	GraphErrors vzFunc = new GraphErrors();

	public Test_calc_Time()
	{
		getParametersFromCCDB();
	}

	private void getParametersFromCCDB()
	{
		// Instead of reading the two tables again and again whenever we select the item from
		// the corresponding jComboBox4, it's better to read both once at the beginning,
		// keep them stored in two different array variables and use those arrays later.
		ReadT2DparsFromCCDB rdTable = new ReadT2DparsFromCCDB("dc_test1",1000);
		rdTable.LoadCCDB();
		parsFromCCDB_dc_test1 = rdTable.parsFromCCDB;

		ReadT2DparsFromCCDB rdTable2 = new ReadT2DparsFromCCDB("default",1000);
		rdTable2.LoadCCDB();
		parsFromCCDB_default = rdTable2.parsFromCCDB;
	}

	public void calculateTime()
	{
		int newRun = 810;
		// CalibrationConstantsLoader.LoadDevel(newRun, "default","dc_test1");
		CalibrationConstantsLoader.LoadDevel(newRun, "default", "default");
		// TableLoader.Fill(); //Not needed
		TableLoader tableLoader = new TableLoader();

		// public static synchronized double calc_Time(double x, double dmax, double tmax, double
		// alphaDeg, double bfield, int s, int r)
		// private static double calc_Time(double x, double dmax, double tmax, double alpha, double
		// bfield, int s, int r)
		// From
		// https://github.com/JeffersonLab/clas12-offline-software/blob/master/reconstruction/dc/src/main/java/org/jlab/rec/dc/timetodistance/TableLoader.java
		// it is understood that s and r are indices that start from 0 (so they go like 0, 1, .., 6)
		/**
		 * 
		 * @param x
		 * @param dmax
		 * @param tmax
		 * @param alpha
		 * @param bfield
		 * @param s
		 *            sector idx
		 * @param r
		 *            superlayer idx
		 * @return returns time (ns) when given inputs of distance x (cm), local angle alpha
		 *         (degrees) and magnitude of bfield (Tesla).
		 */
		int iSec = 2, iSL = 5;
		double dMax = 2.0 * wpdist[iSL];
		double tMax = parsFromCCDB_default[iSec][iSL][2];
		double alphaDeg = 17.5;
		double bField = 0.5;
		double doca = 0.4;

		double[] vzPars = new double[9];
		vzPars[0] = CCDBConstants.getV0()[iSec][iSL];
		vzPars[1] = CCDBConstants.getDELTANM()[iSec][iSL];
		vzPars[2] = CCDBConstants.getTMAXSUPERLAYER()[iSec][iSL];
		vzPars[3] = CCDBConstants.getDISTBETA()[iSec][iSL];
		vzPars[4] = CCDBConstants.getDELT_BFIELD_COEFFICIENT()[iSec][iSL];
		vzPars[5] = CCDBConstants.getDELTATIME_BFIELD_PAR1()[iSec][iSL];
		vzPars[6] = CCDBConstants.getDELTATIME_BFIELD_PAR2()[iSec][iSL];
		vzPars[7] = CCDBConstants.getDELTATIME_BFIELD_PAR3()[iSec][iSL];
		vzPars[8] = CCDBConstants.getDELTATIME_BFIELD_PAR4()[iSec][iSL];
		for (int i = 0; i < vzPars.length; i++)
		{
			System.out.println("p" + i + ": " + vzPars[i]);
		}
		// calc_Time(x, dmax, tmax, alpha, bfield, iSec, iSL)

		// ----------> Disable (by setting to -1.0) the following line for the time being to remove
		// error flag<---------------------
		double calcTime = -1.0; // tableLoader.calc_Time(doca, dMax, tMax, alphaDeg, bField, iSec,
								// iSL);
		System.out.println("calcTime=" + calcTime);

		// Call my dist-to-time function and call Veronique's and
		// Make three graphs: one for each and one for the difference
		// DCTimeFunction(int superlayer, double thetaDeg, double docaNorm, double bfield, double[]
		// par);

		double docaNorm = doca / dMax;
		// double fPars[] = {0.0047, 1.5, 175.0, 0.05, 0.16, 0.4, -2.0, 10.0, -6.5};
		double[] fPars = new double[9];
		for (int i = 0; i < 9; i++)
		{
			fPars[i] = parsFromCCDB_default[iSec][iSL][i];
		}
		DCTimeFunction myTimeFunc = new DCTimeFunction(iSL, alphaDeg, docaNorm, bField, fPars);
		double myCalcTime = myTimeFunc.nonLinearFit();
		System.out.println("myCalcTime=" + myCalcTime);

		docaNorm = 0.0;
		for (int i = 0; i < 100; i++)
		{
			docaNorm = 0.01 * i;
			doca = docaNorm * dMax;

			// ----------> Disable the following line (by setting to -1.0) for the time being to
			// remove error flag<---------------------
			calcTime = -1.0;// tableLoader.calc_Time(doca, dMax, tMax, alphaDeg, bField, iSec, iSL);

			DCTimeFunction myTimeFunc1 = new DCTimeFunction(iSL, alphaDeg, docaNorm, bField, fPars);
			myCalcTime = myTimeFunc1.nonLinearFit();
			System.out.println(i + " " + docaNorm + " " + doca + " " + calcTime + " " + myCalcTime);
			myFunc.addPoint(doca, myCalcTime, 0, 0);
			vzFunc.addPoint(doca, calcTime, 0, 0);
		}

		drawComparisonPlots();

		// private Map<Coordinate, DCFitDrawerForXDoca> mapOfFitLinesX = new HashMap<Coordinate,
		// DCFitDrawerForXDoca>();

		//
		// for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP)
		// data (not to waste time in empty hists)
		// for (int j = 0; j < nSL; j++) {
		// dMax = 2 * wpdist[j];
		// for (int k = 0; k < nThBinsVz; k++) {
		// String title = "timeVsNormDoca Sec=" + (i + 1) + " SL=" + (j + 1) + " Th=" + k;
		// double maxFitValue = h2timeVtrkDocaVZ.get(new Coordinate(i, j,
		// k)).getDataX(getMaximumFitValue(i, j, k));
		// mapOfFitLines.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k,
		// isLinearFit));
		// mapOfFitLines.get(new Coordinate(i, j, k)).setLineColor(4);//(2);
		// mapOfFitLines.get(new Coordinate(i, j, k)).setLineWidth(3);
		// mapOfFitLines.get(new Coordinate(i, j, k)).setLineStyle(4);
		// //mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new
		// Coordinate(i, j, k)));
		// //Because we do the simultaneous fit over all theta bins, we have the same set of pars
		// for all theta-bins.
		// mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new
		// Coordinate(i, j)));

		// String title = "kpLine";
		//// DCFitDrawer(String name, double xmin, double xmax, int sector, int superlayer,
		//// double thetaInDeg, double bField, int KPorVZ, boolean isLinearFit)
		// DCFitDrawer fitKP = new DCFitDrawer(title, 0.0, 1.0, 2, 5, 15.0, 0.5, 1, false);
		// fitKP.setParameters(params);
		// title = "vzLine";
		// DCFitDrawer fitVZ = new DCFitDrawer(title, 0.0, 1.0, 2, 5, 15.0, 0.5, 1, false);
		// fitVZ.setParameters(params);
	}

	public void drawComparisonPlots()
	{
		JFrame frame = new JFrame("Comparison of calcTimes (red: VZ, blue: KA)");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		EmbeddedCanvas canvas = new EmbeddedCanvas();
		frame.setSize(1200, 750);

		myFunc.setMarkerSize(1);
		myFunc.setLineColor(4);
		myFunc.getAttributes().setLineStyle(1);

		vzFunc.setMarkerSize(1);
		vzFunc.setLineColor(2);
		vzFunc.getAttributes().setLineStyle(1);

		myFunc.setTitleX("doca (cm)");
		myFunc.setTitleY("calcTime (ns)");
		canvas.getPad(0).setTitle("Total Cross Section #gammap#rarrowppp#bar");
		canvas.draw(myFunc);
		canvas.draw(vzFunc, "same");

		canvas.setFont("HanziPen TC");
		canvas.setTitleSize(48);
		canvas.setStatBoxFontSize(18);

		frame.add(canvas);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	public static void main(String[] args)
	{
		Test_calc_Time test = new Test_calc_Time();
		test.calculateTime();
	}

}
