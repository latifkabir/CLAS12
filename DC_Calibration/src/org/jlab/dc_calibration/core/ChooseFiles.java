
package org.jlab.dc_calibration.core;

import java.io.File;
import java.util.ArrayList;

import org.jlab.dc_calibration.domain.TimeToDistanceFitter;

public class ChooseFiles
{
	
	//------------------- Choose the files ------------------------------
	ArrayList<String> fileArray = new ArrayList<String>();

	public static void main(String arg[])
	{

	}
	
	public void TestFnc()
	{
		for (int i = 0; i < 5; i++)
		{
			fileArray.add("My_file_name");
		}			
	}
	
	
	//------------------------------------ Do the fitting ----------------------------
	boolean isLinearFit = false; // choose true for linear fit
	
	TimeToDistanceFitter e3 = new TimeToDistanceFitter(OA, fileArray, isLinearFit);
	// Will have to replace OA or have to do function overloading
	
	
	
}
