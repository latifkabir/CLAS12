/*  +__^_________,_________,_____,________^-.-------------------,
 *  | |||||||||   `--------'     |          |                   O
 *  `+-------------USMC----------^----------|___________________|
 *    `\_,---------,---------,--------------'
 *      / X MK X /'|       /'
 *     / X MK X /  `\    /'
 *    / X MK X /`-------'
 *   / X MK X /
 *  / X MK X /
 * (________(                @author m.c.kunkel, kpadhikari
 *  `------'
 */
package org.jlab.dc_calibration.ui;

public class Application
{
	public static void main(String[] args)
	{
		CalibStyle.setStyle();
		DC_Calibration DcCalib = new DC_Calibration();
		DcCalib.Initialize();
	}
}
