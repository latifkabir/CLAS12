package org.jlab.clas12.DataExplorer;

import javax.swing.JFrame;

import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;

import org.jlab.latif.clas12lib.core.ClasRun;
import org.jlab.latif.clas12lib.core.ClasStyle;

/**
 * Exploring the CLAS run
 * 
 * @author Kabir <jlab.org/~latif>
 *
 */

public class RunExplorer
{
	
	/**
	 *  Constructor
	 */
	public RunExplorer()
	{
		ClasStyle.setStyle();
	}
	public void Explorer()
	{

	}

	public void make1DPlot(
			ClasRun run,  //Run number
			String detector, // detector name
			String bank,     // Bank Name
			String param,    // Variable (item)
			int nBins,       // Number of bins
			float min,       // min
			float max)		 // max
	{

		JFrame frame = new JFrame(bank + ":" + param);
		EmbeddedCanvas canvas = new EmbeddedCanvas();
		frame.setSize(800, 500);
		H1F histogram;
		histogram = run.getH1(detector, bank, param, nBins, min, max);
		canvas.draw(histogram);
		frame.add(canvas);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void make2DPlot(
			ClasRun run,
			String detector,
			String bank,
			String paramX,
			String paramY,
			int xnBins,
			float xMin,
			float xMax,
			int ynBins,
			float yMin,
			float yMax)
	{
		JFrame frame = new JFrame(bank + ":" + paramX + " vs " + paramY);
		EmbeddedCanvas canvas = new EmbeddedCanvas();
		frame.setSize(800, 500);
		H2F histogram;
		histogram = run.getH2(detector, bank, paramX, paramY, xnBins, xMin, xMax, ynBins, yMin, yMax);
		canvas.draw(histogram);
		frame.add(canvas);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void make1DPlotCut(
			ClasRun run,
			String detector,
			String bank,
			String param,
			String cut1Str,
			String cut2Str,
			String cut3Str,
			String cut4Str,
			String cutStr,
			int nBins,
			float min,
			float max)
	{
		JFrame frame = new JFrame(bank + ":" + param);
		EmbeddedCanvas canvas = new EmbeddedCanvas();
		frame.setSize(800, 500);
		H1F histogram;
		histogram = run.getH1Cut(detector, bank, param, cut1Str, cut2Str, cut3Str, cut4Str, cutStr, nBins, min, max);
		canvas.draw(histogram);
		frame.add(canvas);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void make2DPlotCut(
			ClasRun run,
			String detector,
			String bank,
			String paramX,
			String paramY,
			String cut1Str,
			String cut2Str,
			String cut3Str,
			String cut4Str,
			String cutStr,
			int xnBins,
			float xMin,
			float xMax,
			int ynBins,
			float yMin,
			float yMax)
	{
		JFrame frame = new JFrame(bank + ":" + paramX + " vs " + paramY);
		EmbeddedCanvas canvas = new EmbeddedCanvas();
		frame.setSize(800, 500);
		H2F histogram;
		histogram = run.getH2Cut(detector, bank, paramX, paramY, cut1Str, cut2Str, cut3Str, cut4Str, cutStr, xnBins,
				xMin, xMax, ynBins, yMin, yMax);
		canvas.draw(histogram);
		frame.add(canvas);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
