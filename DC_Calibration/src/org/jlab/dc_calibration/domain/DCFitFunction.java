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
 *  `------'					based of the KrishnaFcn.java
 */
package org.jlab.dc_calibration.domain;

import java.util.HashMap;
import java.util.Map;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;

import org.freehep.math.minuit.FCNBase;
import static org.jlab.dc_calibration.domain.Constants.histTypeToUseInFitting;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.wpdist;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;

public class DCFitFunction implements FCNBase {

    private GraphErrors profileX;
    private int sector;
    private int superlayer;
    private int thetaBin;
    private boolean isLinear;
    private int meanErrorType = 2; //0: RMS, 1=RMS/sqrt(N), 2 = 1.0 (giving equal weight to all profile means)
    private double docaNormMin = 0.0, docaNormMax = 0.8;
    private Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();
    private Map<Coordinate, SimpleH3D> h3BvTvX = new HashMap<Coordinate, SimpleH3D>();
    private int nBinsX, nBinsY, nBinsZ;
    double dMax;// = 2 * wpdist[superlayer];
    boolean [] selectedAngleBins = new boolean [nThBinsVz];

    private DCTimeFunction timeFunc;

    public DCFitFunction(GraphErrors profileX, int superlayer, int thetaBin, boolean isLinear) {
        this.profileX = profileX;
        this.superlayer = superlayer;
        this.thetaBin = thetaBin;
        this.isLinear = isLinear;
        dMax = 2 * wpdist[superlayer];
    }

    //public DCFitFunction(Map<Coordinate, H2F> h2timeVtrkDocaNorm, int sector, int superlayer, int thetaBin, boolean isLinear) {
    public DCFitFunction(Map<Coordinate, H2F> h2timeVtrkDoca, int sector, int superlayer, boolean isLinear) {
        this.h2timeVtrkDoca = h2timeVtrkDoca;
        this.sector = sector;
        this.superlayer = superlayer;
        this.isLinear = isLinear;
        dMax = 2 * wpdist[superlayer];
    }

    public DCFitFunction(Map<Coordinate, H2F> h2timeVtrkDoca, int sector, int superlayer,
            int meanErrorType, double docaNormMin, double docaNormMax, boolean isLinear, boolean [] selectedAngleBins) {
        this.h2timeVtrkDoca = h2timeVtrkDoca;
        this.sector = sector;
        this.superlayer = superlayer;
        this.isLinear = isLinear;
        this.meanErrorType = meanErrorType;
        this.docaNormMin = docaNormMin;
        this.docaNormMax = docaNormMax;
        //this.thetaBin = thetaBin; //To be removed later
        dMax = 2 * wpdist[superlayer];
        this.selectedAngleBins = selectedAngleBins;
        
        System.out.println("Inside DCFitFunction constructor ...");
        for (int i = 0; i < selectedAngleBins.length; i++) {
            System.out.println(i + " " + selectedAngleBins[i]);
        }
    }

    //Added boolean dummy to avoid "Method has the same erasure add(Set) as above DCFitFunction(..)" error or warning
    //  Later, I may want to have a similar but an entirely different class to deal with the SimpleH3D histos.
    public DCFitFunction(Map<Coordinate, SimpleH3D> h3BvTvX, int sector, int superlayer,
            int meanErrorType, double docaNormMin, double docaNormMax, boolean isLinear, boolean [] selectedAngleBins, boolean dummy) {
        this.h3BvTvX = h3BvTvX;
        this.sector = sector;
        this.superlayer = superlayer;
        this.isLinear = isLinear;
        this.meanErrorType = meanErrorType;
        this.docaNormMin = docaNormMin;
        this.docaNormMax = docaNormMax;
        //this.thetaBin = thetaBin; //To be removed later
        dMax = 2 * wpdist[superlayer];
        this.selectedAngleBins = selectedAngleBins;
        
        System.out.println("Inside DCFitFunction constructor ...");
        for (int i = 0; i < selectedAngleBins.length; i++) {
            System.out.println(i + " " + selectedAngleBins[i]);
        }        
    }

    public double errorDef() {
        return 1;
    }

    @Override
    public double valueOf(double[] par) {
        double chisq = 0.0;
        int discardThBins = 4;
        if (histTypeToUseInFitting == 0) {
            chisq = getChisqUsingXProfiles(discardThBins, par);
        } else if (histTypeToUseInFitting == 1 || histTypeToUseInFitting == 2) {
            chisq = getChisqWithoutXProfiles(discardThBins, par);
        } else if(histTypeToUseInFitting == 3) {
            chisq = getChisqWithBFieldUsed(discardThBins, par);
        }
        return chisq;
    }

    public double getChisqUsingXProfiles(int discardThBins, double par[]) {
        double chisq = 0;
        double delta = 0;
        double thetaDeg = 0;
        double measTimeErr = 0.0;
        GraphErrors profileX;
        H2F h2tvXnorm;
        H1F sliceX;
        double nSliceX = 0.0;

        for (int th = 0 ; th < nThBinsVz; th++) {
            if(selectedAngleBins[th]==false) {
                continue;
            }
//        for (int th = 0 + discardThBins; th < nThBinsVz - discardThBins; th++) {
//            //discard central bin (i.e. the bin around zero-degree) to avoid bad relolution events
//            if (th == (nThBinsVz / 2)) {
//                continue;
//            }
//            if (th == (nThBinsVz / 2) - 1 || th == (nThBinsVz / 2) + 1) {
//                continue;
//            } //Next bin on each side of the central one

            thetaDeg = 0.5 * (thEdgeVzL[th] + thEdgeVzH[th]);
            h2tvXnorm = h2timeVtrkDoca.get(new Coordinate(sector, superlayer, th));
            //profileX = h2timeVtrkDocaNorm.get(new Coordinate(sector, superlayer, th)).getProfileX();
            profileX = h2tvXnorm.getProfileX();
            for (int i = 0; i < profileX.getDataSize(0); i++) {

                double docaNorm = profileX.getDataX(i) / dMax;
                double measTime = profileX.getDataY(i);
                sliceX = h2tvXnorm.sliceX(i);//Histogram of y-values for all data falling in the ith x-slice or x-bin.
                //nSliceX = (int) sliceX.getEntries();//derived histos always have 0 entries, so let's use integral()
                nSliceX = sliceX.integral();

                //if(th == 5 && i == 10 ) System.out.println("i=" + i + " n=" + nSliceX);
                //Three different options for errors to weigh the chisq calculation
                if (meanErrorType == 0) {
                    measTimeErr = profileX.getDataEY(i); //4/5/17: Replaced this with the followign two lines                
                } else if (meanErrorType == 1) {
                    measTimeErr = profileX.getDataEY(i) / Math.sqrt(nSliceX); //Using Error = RMS/sqrt(N)
                } else if (meanErrorType == 2) {
                    measTimeErr = 1.0; //Giving equal weight (to avoid having fit biased by heavily populated bins)
                }
                timeFunc = new DCTimeFunction(superlayer, thetaDeg, docaNorm, par);
                double calcTime = isLinear ? timeFunc.linearFit() : timeFunc.nonLinearFit();

                //if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.9) {
                if (measTimeErr == measTimeErr && measTimeErr > docaNormMin && docaNorm < docaNormMax) { //2/15/17
                    delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
                    chisq += delta * delta;
                }
            }
        }
        return chisq;
    }

    public double getChisqWithoutXProfiles(int discardThBins, double par[]) {
        double chisq = 0;
        double delta = 0;
        double thetaDeg = 0;

        H2F h2tvXnorm;

        int nBinsX = 0, nBinsY = 0;
        
        for (int th = 0 ; th < nThBinsVz; th++) {
            if(selectedAngleBins[th]==false) {
                continue;
            }
//        for (int th = 0 + discardThBins; th < nThBinsVz - discardThBins; th++) {
//            //discard central bin (i.e. the bin around zero-degree) to avoid bad relolution events
//            if (th == (nThBinsVz / 2)) {
//                continue;
//            }
//            if (th == (nThBinsVz / 2) - 1 || th == (nThBinsVz / 2) + 1) {
//                continue;
//            } //Next bin on each side of the central one

            thetaDeg = 0.5 * (thEdgeVzL[th] + thEdgeVzH[th]);
            if (histTypeToUseInFitting == 1) {
                h2tvXnorm = h2timeVtrkDoca.get(new Coordinate(sector, superlayer, th));//From the map of H2F histos
            } else if (histTypeToUseInFitting == 2 || histTypeToUseInFitting == 3) {
                h2tvXnorm = h3BvTvX.get(new Coordinate(sector, superlayer, th)).getXYProj();//From the map of SimpleH3D histos
            }
            nBinsX = h2tvXnorm.getXAxis().getNBins();
            nBinsY = h2tvXnorm.getYAxis().getNBins();
            double wBinX = h2tvXnorm.getDataEX(1); //width of the 1st x-bin (but will be the same for all)
            double wBinY = h2tvXnorm.getDataEY(1); //Width of the 1st y-bin (and of all)
            double measTimeErr = wBinY * wBinX; //Using area of the bin for error  
            //profileX = h2tvXnorm.getProfileX();
            for (int i = 0; i < nBinsX; i++) {
                for (int j = 0; j < nBinsY; j++) {
                    double docaNorm = h2tvXnorm.getDataX(i) / dMax;//profileX.getDataX(i); //ith x-Bin center
                    double measTime = h2tvXnorm.getDataY(j);//profileX.getDataY(i); //jth y-Bin center
                    double binContent = h2tvXnorm.getData(i, j);//getData(i,j) and getBinContent(i,j) are equivalent and both work

                    if (binContent < 5) {
                        continue; //discarding low stat bins (4/23/17)
                    }
                    //Three different options for errors to weigh the chisq calculation
                    // From https://github.com/KPAdhikari/groot/blob/master/src/main/java/org/jlab/groot/data/H2F.java
                    //getDataEX = xAxis.getBinWidth(bin); & getDataEY = yAxis.getBinWidth(bin); 
                    if (meanErrorType == 0) {
                        measTimeErr = wBinY * wBinX; //Using area of the bin for error           
                    } else if (meanErrorType == 1) {
                        measTimeErr = measTimeErr / Math.sqrt(binContent); //Using Error = RMS/sqrt(N)
                        measTimeErr = 1.0 / Math.sqrt(binContent); //Using Error = RMS/sqrt(N)
                    } else if (meanErrorType == 2) {
                        measTimeErr = 1.0; //Giving equal weight (to avoid having fit biased by heavily populated bins)
                    }
                    timeFunc = new DCTimeFunction(superlayer, thetaDeg, docaNorm, par);
                    double calcTime = isLinear ? timeFunc.linearFit() : timeFunc.nonLinearFit();

                    //if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.9) {
                    if (measTimeErr == measTimeErr && measTimeErr > docaNormMin && docaNorm < docaNormMax) { //2/15/17
                        delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
                        chisq += delta * delta;
                    }
                }
            }
        }
        return chisq;
    }

    public double getChisqWithBFieldUsed(int discardThBins, double par[]) {
        double chisq = 0;
        double delta = 0;
        double thetaDeg = 0;
        double[] xyz = new double[3];
        
        H2F h2tvXnorm;
        SimpleH3D h3d;

        int nBinsX = 0, nBinsT = 0, nBinsB = 0;

        for (int th = 0 + discardThBins; th < nThBinsVz - discardThBins; th++) {
            //discard central bin (i.e. the bin around zero-degree) to avoid bad relolution events
            if (th == (nThBinsVz / 2)) {
                continue;
            }
            if (th == (nThBinsVz / 2) - 1 || th == (nThBinsVz / 2) + 1) {
                continue;
            } //Next bin on each side of the central one

            thetaDeg = 0.5 * (thEdgeVzL[th] + thEdgeVzH[th]);
            h3d = h3BvTvX.get(new Coordinate(sector, superlayer, th));
            h2tvXnorm = h3d.getXYProj();//From the map of SimpleH3D histos

            nBinsX = h2tvXnorm.getXAxis().getNBins();
            nBinsT = h2tvXnorm.getYAxis().getNBins();
            nBinsB = h3d.getNBinsZ();

            double wBinX = h2tvXnorm.getDataEX(1); //width of the 1st x-bin (but will be the same for all)
            double wBinT = h2tvXnorm.getDataEY(1); //Width of the 1st y-bin (and of all)
            double wBinB = h3d.getBinWidthZ();
            double measTimeErr = wBinT * wBinX; //Using area of the bin for error  
            //profileX = h2tvXnorm.getProfileX();
            for (int i = 0; i < nBinsX; i++) {
                for (int j = 0; j < nBinsT; j++) {
                    for (int k = 0; k < nBinsB; k++) {
                        xyz = h3d.getBinCenter(i, j, k);
                        double docaNorm = xyz[0] / dMax;//profileX.getDataX(i); //ith x-Bin center
                        double measTime = xyz[1];//profileX.getDataY(i); //jth y-Bin center
                        double bField = xyz[2];
                        double binContent = h3d.getBinContent(i, j, k);
                        
                        if (binContent < 5) {
                            continue; //discarding low stat bins (4/23/17)
                        }
                        //Three different options for errors to weigh the chisq calculation
                        // From https://github.com/KPAdhikari/groot/blob/master/src/main/java/org/jlab/groot/data/H2F.java
                        //getDataEX = xAxis.getBinWidth(bin); & getDataEY = yAxis.getBinWidth(bin); 
                        if (meanErrorType == 0) {
                            measTimeErr = wBinT * wBinX; //Using area of the bin for error           
                        } else if (meanErrorType == 1) {
                            measTimeErr = measTimeErr / Math.sqrt(binContent); //Using Error = RMS/sqrt(N)
                            measTimeErr = 1.0 / Math.sqrt(binContent); //Using Error = RMS/sqrt(N)
                        } else if (meanErrorType == 2) {
                            measTimeErr = 1.0; //Giving equal weight (to avoid having fit biased by heavily populated bins)
                        }
                        timeFunc = new DCTimeFunction(superlayer, thetaDeg, docaNorm, bField, par);
                        double calcTime = isLinear ? timeFunc.linearFit() : timeFunc.nonLinearFit();

                        //if (measTimeErr == measTimeErr && measTimeErr > 0.0 && docaNorm < 0.9) {
                        if (measTimeErr == measTimeErr && measTimeErr > docaNormMin && docaNorm < docaNormMax) { //2/15/17
                            delta = (measTime - calcTime) / measTimeErr; // error weighted deviation
                            chisq += delta * delta;
                        }
                    }
                }
            }
        }
        return chisq;
    }
}
