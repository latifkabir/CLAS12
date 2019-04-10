/**
 * Compare DCDB values loaded from calibration and reconstruction
 * @author Latif Kabir < jlab.org/~latif >
 *
 */
package org.jlab.dc_calibration.test;

import org.jlab.dc_calibration.io.ReadT2DparsFromCCDB;
import org.jlab.rec.dc.CalibrationConstantsLoader;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.CCDBConstants;

public class CompareCCDBvariations
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// --------- T2D parameters from CCDB loaded by the reconstruction ---------
		System.out.println("\n\n\n=========================================================================================================");
		System.out.println("|                       Loading CCDB Prameters from Reconstruction Program                              |");
		System.out.println(" ========================================================================================================");

		CalibrationConstantsLoader.Load(1000, "default");
		TableLoader.Fill();

		// --------- T2D parameters from CCDB loaded by the Calibration suite ---------
		System.out.println("\n\n\n=========================================================================================================");
		System.out.println("|                       Loading and printing CCDB Prameters from Calibration Suite                      |");
		System.out.println(" ========================================================================================================");
		ReadT2DparsFromCCDB defPars = new ReadT2DparsFromCCDB("default", 1000);
		defPars.LoadCCDB();

		System.out.println("\n\n\n=========================================================================================================");
		System.out.println("|                       Printing CCDB Prameters from Reconstruction Program                              |");
		System.out.println(" ========================================================================================================");

		for (int sec = 0; sec < 6; sec++)
		{
			for (int sl = 0; sl < 6; sl++)
			{
				System.out.println(
						(sec + 1) + "   "
								+ (sl + 1) + "   "
								+ CCDBConstants.getV0()[sec][sl] + "    "
								+ CCDBConstants.getDELTANM()[sec][sl] + "    "
								+ CCDBConstants.getTMAXSUPERLAYER()[sec][sl] + "    "
								+ CCDBConstants.getDISTBETA()[sec][sl] + "    "
								+ CCDBConstants.getDELT_BFIELD_COEFFICIENT()[sec][sl] + "    "
								+ CCDBConstants.getDELTATIME_BFIELD_PAR1()[sec][sl] + "    "
								+ CCDBConstants.getDELTATIME_BFIELD_PAR2()[sec][sl] + "    "
								+ CCDBConstants.getDELTATIME_BFIELD_PAR3()[sec][sl] + "    "
								+ CCDBConstants.getDELTATIME_BFIELD_PAR4()[sec][sl]);
			}
		}
	}
}
