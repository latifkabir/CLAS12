
package org.jlab.clas12.DataExplorer;


import org.jlab.latif.clas12lib.core.ClasStyle;

/**
 * Data Explorer for CLAS12 data
 * @author Latif Kabir < jlab.org/~latif >
 *
 */
public class App
{

	/**
	 * @param args
	 */
	// ------------ The main function -------------------
	public static void main(String[] args)
	{		
		ExplorerLook.SetLook();		
		ClasStyle.setStyle();
		DataExplorer explorer = new DataExplorer();
		explorer.titleArea();
		explorer.runArea();
		explorer.eventArea();
		explorer.detectorSelArea();
		explorer.bankSelArea();
		explorer.xSelArea(null);
		explorer.ySelArea(null);
		explorer.cutArea(null);
		explorer.cutExpArea();
		explorer.makePlot();
		explorer.textArea();
		explorer.combineComponents();
	}
}
