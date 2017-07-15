/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dc_calibration.domain;

/**
 *
 * @author KPAdhikari
 */
import java.io.IOException;
import org.jlab.dc_calibration.domain.*;
import org.jlab.ccdb.*;
import org.jlab.ccdb.JDBCProvider;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.jlab.dc_calibration.domain.Constants.nCables6;
import static org.jlab.dc_calibration.domain.Constants.nFitPars;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nSectors;
import static org.jlab.dc_calibration.domain.Constants.nSlots7;

public class ReadT0parsFromCCDB {

    //private int superlayer;    
    public Vector<Integer> Sector, Superlayer, Slot, Cable;
    public Vector<Double> T0Correction, T0Error;
    //public double [][][][] T0sFromCCDB    = new double [nSectors][nSL][nSlots7][nCables6];//[6][6][7][6]
    //public double [][][][] T0ErrsFromCCDB = new double [nSectors][nSL][nSlots7][nCables6];//[6][6][7][6]
    String ccdbVariation = "dc_test1";

    public ReadT0parsFromCCDB(String ccdbVariation) {
        this.ccdbVariation = ccdbVariation;

        System.out.println("Hi ... ");
        //JDBCProvider provider = CcdbPackage.createProvider("mysql://localhost")  ;
        JDBCProvider provider = CcdbPackage.createProvider("mysql://clas12reader@clasdb.jlab.org/clas12");
        provider.connect();

        //to check the table exists
        System.out.println("/calibration/dc/time_corrections/T0Corrections exists? - "
                + provider.isTypeTableAvailable("/calibration/dc/time_corrections/T0Corrections"));
        System.out.println("CCDB variation name is " + ccdbVariation);
        //provider.setDefaultVariation("dc_test1");  //("default");
        provider.setDefaultVariation(ccdbVariation);

        Assignment asgmt = provider.getData("/calibration/dc/time_corrections/T0Corrections");
//        //It Has columns for:       Sector Superlayer Slot Cable T0Correction T0Error
//        for (Vector<Double> row : asgmt.getTableDouble()) {
//            for (Double cell : row) {
//                System.out.print(cell + " ");
//            }
//            System.out.println(); //next line after a row
//        }
//        Vector<Double> doubleValues; // System.out.println(doubleValues);
//
//        doubleValues = asgmt.getColumnValuesDouble(0); //First column values
//        System.out.println("First 2 in Sector column:" + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));
//        doubleValues = asgmt.getColumnValuesDouble(1); //Second column values
//        System.out.println("First 2 in Superlayer column: " + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));
//        doubleValues = asgmt.getColumnValuesDouble(2); //Third column values
//        System.out.println("First 2 in v0 column: " + doubleValues.elementAt(0) + " " + doubleValues.elementAt(1));

        //Now put all the columns in the corresponding Vector members.
        Sector = asgmt.getColumnValuesInt(0);
        Superlayer = asgmt.getColumnValuesInt(1);
        Slot = asgmt.getColumnValuesInt(2);
        Cable = asgmt.getColumnValuesInt(3);
        T0Correction = asgmt.getColumnValuesDouble(4);
        T0Error = asgmt.getColumnValuesDouble(5);
    }

    public void printCurrentT0s() {
        for (int i = 0; i < Sector.size(); i++) {
            System.out.println(String.format("%d  %d  %d  %d  %4.3f  %4.3f", Sector.elementAt(i), Superlayer.get(i),
                    Slot.get(i), Cable.get(i), T0Correction.get(i), T0Error.get(i)));
        }
    }

    /**
     * Prints new values of T0s (current-T0 + deltaT0) for a given sector &
     * superlayer
     *
     * @param superlayer superlayer (1,2, ..,6)
     * @param deltaT0 further correction to T0 (determined from the
     * time-to-distance fits)
     */
    public void printModifiedT0s(int sector, int superlayer, double deltaT0) {
        int sec = 0, sl = 0;
        double newT0 = 0.0;
        for (int i = 0; i < Sector.size(); i++) {
            sec = Sector.elementAt(i);
            sl = Superlayer.get(i);
            newT0 = T0Correction.get(i) - deltaT0;

            if (sec == sector && sl == superlayer) {
                System.out.println(String.format("%d  %d  %d  %d  %4.3f  %4.3f",
                        sec, sl, Slot.get(i), Cable.get(i), newT0, T0Error.get(i)));
            }
        }
    }

    /**
     * Writes out new values of T0s (current-T0 + deltaT0) to a file for a given
     * sector & superlayer
     *
     * @param superlayer superlayer (1,2, ..,6)
     * @param deltaT0 further correction to T0 (determined from the
     * time-to-distance fits)
     */
    public void writeOutModifiedT0s(int sector, int superlayer, double deltaT0) {
        boolean append_to_file = false;
        FileOutputWriter file = null;
        try {
            file = new FileOutputWriter(String.format("src/files/T0plus_deltaT0_Sec%dSL%d.txt",
                    sector, superlayer), append_to_file);
            file.Write("#Sector  Superlayer  Slot  Cable  T0Correction  T0Error");
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }

        int sec = 0, sl = 0;
        double newT0 = 0.0;
        for (int i = 0; i < Sector.size(); i++) {
            sec = Sector.elementAt(i);
            sl = Superlayer.get(i);
            newT0 = T0Correction.get(i) - deltaT0;
            String str = " ";

            if (sec == sector && sl == superlayer) {
                str = String.format("%d  %d  %d  %d  %4.3f  %4.3f",
                        sec, sl, Slot.get(i), Cable.get(i), newT0, T0Error.get(i));
                if (!(file == null)) {
                    file.Write(str);
//                    file.Close();
                }
            }
        }
        
        try {
            file.Close();
        } catch (IOException ex) {
            Logger.getLogger(ReadT0parsFromCCDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        ReadT0parsFromCCDB readT0 = new ReadT0parsFromCCDB("dc_test1");
        //readT0.printCurrentT0s();
        readT0.printModifiedT0s(6, 6, 10.0);
        readT0.writeOutModifiedT0s(6, 6, 10.0);
    }
}
