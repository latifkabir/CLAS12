/*              
 * 		@author KPAdhikari
 *              @author m.c.kunkel
 */
package org.jlab.dc_calibration.domain;

import java.awt.Dimension;
import java.awt.Toolkit;
import static org.jlab.dc_calibration.domain.Constants.nHists;
import static org.jlab.dc_calibration.domain.Constants.nLayer;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nSectors;
import static org.jlab.dc_calibration.domain.Constants.nTh;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.parName;
import static org.jlab.dc_calibration.domain.Constants.prevFitPars;
import static org.jlab.dc_calibration.domain.Constants.rad2deg;
import static org.jlab.dc_calibration.domain.Constants.thBins;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;
import static org.jlab.dc_calibration.domain.Constants.wpdist;
import static org.jlab.dc_calibration.domain.Constants.tMaxSL;
import static org.jlab.dc_calibration.domain.Constants.timeAxisMax;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnStrategy;
import org.freehep.math.minuit.MnUserParameters;
//import org.jlab.dc_calibration.NTuple.NTuple;
import static org.jlab.dc_calibration.domain.Constants.bFieldBins;
import static org.jlab.dc_calibration.domain.Constants.bFieldMax;
import static org.jlab.dc_calibration.domain.Constants.bFieldMin;
import static org.jlab.dc_calibration.domain.Constants.binForTestPlotTemp;
import static org.jlab.dc_calibration.domain.Constants.calcDocaCut;
import static org.jlab.dc_calibration.domain.Constants.histTypeToUseInFitting;
import static org.jlab.dc_calibration.domain.Constants.iSecMax;
import static org.jlab.dc_calibration.domain.Constants.iSecMin;
import static org.jlab.dc_calibration.domain.Constants.nFitPars;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz2;
import static org.jlab.dc_calibration.domain.Constants.tMin;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH2;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL2;
import org.jlab.groot.base.TStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.hipo.HipoDataSource;

public class TimeToDistanceFitter implements ActionListener, Runnable {

    private DataBank bnkHits;
    private DataBank bnkSegs;
    private DataBank bnkSegTrks;
    private DataBank bnkTrks;
    private int nTrks;
    private int colIndivFit = 1, colSimulFit = 4;
    int[][][] segmentIDs; //[nTrks][3][2] //3 for crosses per track, 2 for segms per cross.
    double[][][] trkChi2;//Size equals the # of tracks for the event
    int nTracks = 0;

    private Map<Coordinate, H1F> hArrWire = new HashMap<Coordinate, H1F>();
    private Map<Coordinate, H1F> h1ThSL = new HashMap<Coordinate, H1F>();
    private Map<Coordinate, H1F> h1timeSlTh = new HashMap<Coordinate, H1F>();
    // Histograms to get ineff. as fn of trkDoca (NtrkDoca = trkDoca/docaMax)
    private Map<Coordinate, H1F> h1trkDoca2Dar = new HashMap<Coordinate, H1F>(); // #############################################################
    private Map<Coordinate, H1F> h1NtrkDoca2Dar = new HashMap<Coordinate, H1F>();// [3] for all good hits, only bad (matchedHitID == -1) and ratio
    private Map<Coordinate, H1F> h1NtrkDocaP2Dar = new HashMap<Coordinate, H1F>();// ############################################################
    private Map<Coordinate, H1F> h1trkDoca3Dar = new HashMap<Coordinate, H1F>(); // ############################################################
    private Map<Coordinate, H1F> h1NtrkDoca3Dar = new HashMap<Coordinate, H1F>();// [3] for all good hits, only bad (matchedHitID == -1) and ratio
    private Map<Coordinate, H1F> h1NtrkDocaP3Dar = new HashMap<Coordinate, H1F>();// ############################################################
    private Map<Coordinate, H1F> h1trkDoca4Dar = new HashMap<Coordinate, H1F>();
    private Map<Coordinate, H1F> h1wire4Dar = new HashMap<Coordinate, H1F>();// no ratio here
    private Map<Coordinate, H1F> h1avgWire4Dar = new HashMap<Coordinate, H1F>();// no ratio here
    private Map<Coordinate, H1F> h1fitChisqProbSeg4Dar = new HashMap<Coordinate, H1F>();
    //private Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();
    public Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();//made it public - to be able to access it from SliceViewer
    public Map<Coordinate, H2F> h2timeVcalcDoca = new HashMap<Coordinate, H2F>();//made it public - to be able to access it from SliceViewer
    private Map<Coordinate, H2F> h2timeVtrkDocaVZ = new HashMap<Coordinate, H2F>();
    private Map<Coordinate, H2F> h2timeFitResVtrkDoca = new HashMap<Coordinate, H2F>();//time - fitLine
    private Map<Coordinate, H1F> h1timeFitRes = new HashMap<Coordinate, H1F>();  //time - fitLine

    private Map<Integer, Integer> layerMapTBHits;
    private Map<Integer, Integer> wireMapTBHits;
    private Map<Integer, Double> timeMapTBHits;
    private Map<Integer, Double> trkDocaMapTBHits;
    private Map<Integer, Double> calcDocaMapTBHits;
    private Map<Integer, Double> timeResMapTBHits;
    private Map<Integer, Double> BMapTBHits;
    private Map<Integer, Integer> gSegmThBinMapTBSegments;
    private Map<Integer, Double> gSegmAvgWireTBSegments;
    private Map<Integer, Double> gFitChisqProbTBSegments;

    private Map<Coordinate, GraphErrors> htime2DisDocaProfile = new HashMap<Coordinate, GraphErrors>();
    private Map<Coordinate, DCFitFunction> mapOfFitFunctions = new HashMap<Coordinate, DCFitFunction>();
    private Map<Coordinate, MnUserParameters> mapOfFitParameters = new HashMap<Coordinate, MnUserParameters>();
    private Map<Coordinate, double[]> mapOfUserFitParameters = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, double[]> mapOfUserFitParErrors = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, DCFitDrawer> mapOfFitLines = new HashMap<Coordinate, DCFitDrawer>();
    private Map<Coordinate, DCFitDrawerForXDoca> mapOfFitLinesX = new HashMap<Coordinate, DCFitDrawerForXDoca>();

    private Map<Coordinate, DCFitFunctionForEachThBin> mapOfFitFunctionsOld = new HashMap<Coordinate, DCFitFunctionForEachThBin>();
    private Map<Coordinate, MnUserParameters> mapOfFitParametersOld = new HashMap<Coordinate, MnUserParameters>();
    private Map<Coordinate, double[]> mapOfUserFitParametersOld = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, double[]> mapOfUserFitParErrorsOld = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, DCFitDrawer> mapOfFitLinesOld = new HashMap<Coordinate, DCFitDrawer>();
    private Map<Coordinate, DCFitDrawerForXDoca> mapOfFitLinesXOld = new HashMap<Coordinate, DCFitDrawerForXDoca>();

    private Map<Coordinate, SimpleH3D> h3BTXmap = new HashMap<Coordinate, SimpleH3D>();   //uses absolute doca
    private Map<Coordinate, DCFitFunctionWithSimpleH3D> mapOfFitFunctionsXTB = new HashMap<Coordinate, DCFitFunctionWithSimpleH3D>();
    private Map<Coordinate, MnUserParameters> mapOfFitParametersXTB = new HashMap<Coordinate, MnUserParameters>();
    private Map<Coordinate, double[]> mapOfUserFitParametersXTB = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, double[]> mapOfUserFitParErrorsXTB = new HashMap<Coordinate, double[]>();
    private Map<Coordinate, DCFitDrawerForXDocaXTB> mapOfFitLinesXTB = new HashMap<Coordinate, DCFitDrawerForXDocaXTB>();

    private H1F h1bField;
    private H1F[] h1bFieldSL = new H1F[nSL];
    private H1F h1fitChisqProb, h1fitChi2Trk, h1fitChi2Trk2, h1ndfTrk, h1zVtx;
    private H2F testHist, h2ResidualVsTrkDoca;
    private H1F h1trkDoca4NegRes, h1trkDoca4PosRes;//Temp, 4/27/17
    private Map<Coordinate, H1F> h1timeRes = new HashMap<Coordinate, H1F>();
    private Map<Coordinate, H2F> h2timeResVsTrkDoca = new HashMap<Coordinate, H2F>();

    private GraphErrors[] vertLineDmax = new GraphErrors[nSL];
    private GraphErrors[] vertLineDmaxCos30 = new GraphErrors[nSL];
    private GraphErrors[][] vertLineDmaxCosTh = new GraphErrors[nSL][nThBinsVz];

    private boolean acceptorder = false;
    private boolean isLinearFit;

    private ArrayList<String> fileArray;
    private EvioDataChain reader;
    private HipoDataSource readerH;
    private OrderOfAction OAInstance;
    private DCTabbedPane dcTabbedPane;

    // MK testing
//    private NTuple nTupletimeVtrkDocaVZ;
    double[] tupleVars;

    public TimeToDistanceFitter(ArrayList<String> files, boolean isLinearFit) {
        this.fileArray = files;
        this.reader = new EvioDataChain();
        this.readerH = new HipoDataSource();
        this.dcTabbedPane = new DCTabbedPane("PooperDooper");
        this.isLinearFit = isLinearFit;
//        this.nTupletimeVtrkDocaVZ = new NTuple("testData", "Sector:SuperLayer:ThetaBin:Doca:Time");
        this.tupleVars = new double[5];

        createVerticalLinesForDMax();
        createHists();
    }

    public TimeToDistanceFitter(OrderOfAction OAInstance, ArrayList<String> files, boolean isLinearFit) {
        this.fileArray = files;
        this.OAInstance = OAInstance;
        this.reader = new EvioDataChain();
        this.readerH = new HipoDataSource();
        this.dcTabbedPane = new DCTabbedPane("PooperDooper");
//        this.nTupletimeVtrkDocaVZ = new NTuple("testData", "Sector:SuperLayer:ThetaBin:Doca:Time");
        this.tupleVars = new double[5];
        this.isLinearFit = isLinearFit;

        createVerticalLinesForDMax();
        createHists();
    }

    //I didn't know how to make a vertical line out of the function classes such as Func1D.
    private void createVerticalLinesForDMax() {
        double rad2deg = 180.0 / Math.PI;
        double cos30 = Math.cos(30.0 / rad2deg);
        double cosTh = 0.0, reducedTh = 0.0;
        for (int i = 0; i < nSL; i++) {
            vertLineDmax[i] = new GraphErrors();
            vertLineDmaxCos30[i] = new GraphErrors();

            //Drawing an array of points (as I didn't know how to draw the lines to join them)
//            for (int j = 0; j < 20; j++) {
//                vertLineDmax[i].addPoint(2 * wpdist[i], j * 5, 0, 0);
//                vertLineDmaxCos30[i].addPoint(2 * wpdist[i] * cos30, j * 5, 0, 0);
//            }
//            vertLineDmax[i].setMarkerSize(2);
//            vertLineDmaxCos30[i].setMarkerSize(2);
            //Drawing the line as the error bar of a single point graph (with zero size for marker)
            vertLineDmax[i].addPoint(2 * wpdist[i], 50, 0, 50);
            vertLineDmaxCos30[i].addPoint(2 * wpdist[i] * cos30, 50, 0, 50);
            vertLineDmax[i].setMarkerSize(0);
            vertLineDmaxCos30[i].setMarkerSize(0);

            vertLineDmax[i].setMarkerColor(2);
            vertLineDmax[i].setLineColor(2);
            vertLineDmax[i].setLineThickness(1);

            vertLineDmaxCos30[i].setMarkerColor(2);
            vertLineDmaxCos30[i].setLineColor(2);
            vertLineDmaxCos30[i].setLineThickness(1);

            //Making more vertical lines at each dmax*cos(th) rather than dmax*cos(30)
            for (int k = 0; k < nThBinsVz; k++) {
                reducedTh = Math.abs(0.5 * (thEdgeVzH[k] + thEdgeVzL[k]));
                if (reducedTh > 30.0) {
                    reducedTh = reducedTh - 30.0;
                }
                cosTh = Math.cos((reducedTh) / rad2deg);
                vertLineDmaxCosTh[i][k] = new GraphErrors();
//                for (int j = 0; j < 20; j++) {
//                    vertLineDmaxCosTh[i][k].addPoint(2 * wpdist[i] * cosTh, j * 5, 0, 0);
//                }
                vertLineDmaxCosTh[i][k].addPoint(2 * wpdist[i] * cosTh, 50, 0, 50);
                vertLineDmaxCosTh[i][k].setMarkerColor(1);
                vertLineDmaxCosTh[i][k].setMarkerSize(0);
                vertLineDmaxCosTh[i][k].setLineColor(1);
                vertLineDmaxCosTh[i][k].setLineThickness(1);
            }
        }
    }

    private void createHists() {
        initializeBFieldHistograms();

        h1fitChisqProb = new H1F("fitChisqProb", 120, 0.0, 1.2);
        h1fitChisqProb.setTitle("fitChisqProb");
        h1fitChisqProb.setLineColor(2);
        h1fitChi2Trk = new H1F("fitChi2Trk", 100, 0.0, 8000);//1.2);
        h1fitChi2Trk.setTitle("fitChi2Trk");
        h1fitChi2Trk.setLineColor(2);
        h1fitChi2Trk2 = new H1F("fitChi2Trk2", 100, 0.0, 8000);//To see how a zVtx cut affects
        h1fitChi2Trk2.setTitle("fitChi2Trk2");
        h1fitChi2Trk2.setLineColor(2);
        h1ndfTrk = new H1F("ndfTrk", 80, 5.0, 45);
        h1ndfTrk.setTitle("ndfTrk");
        h1ndfTrk.setLineColor(2);
        h1zVtx = new H1F("zVtx", 200, -50.0, 50.0);
        h1zVtx.setTitle("zVtx");
        h1zVtx.setLineColor(2);
        h1trkDoca4NegRes = new H1F("trkDoca4NegRes", 200, -2.0, 2.0);
        h1trkDoca4NegRes.setTitle("trkDoca for negative residual");
        h1trkDoca4PosRes = new H1F("trkDoca4PosRes", 200, -2.0, 2.0);
        h1trkDoca4PosRes.setTitle("trkDoca for positive residual");
        h2ResidualVsTrkDoca = new H2F("ResidualVsTrkDoca", 200, -2.0, 2.0, 100, -0.5, 0.5);
        h2ResidualVsTrkDoca.setTitle("residual vs trkDoca");
        h2ResidualVsTrkDoca.setTitleY("residual");

        testHist = new H2F("A test of superlayer6 at thetabin6", 200, 0.0, 1.0, 150, 0.0, 200.0);

        TStyle.createAttributes();
        String hNm = "";
        String hTtl = "";
        for (int i = 0; i < nSL; i++) {
            for (int j = 0; j < nLayer; j++) {
                for (int k = 0; k < nHists; k++) {
                    hNm = String.format("wireS%dL%dDb%02d", i + 1, j + 1, k);
                    hArrWire.put(new Coordinate(i, j, k), new H1F(hNm, 120, -1.0, 119.0));
                    hTtl = String.format("wire (SL=%d, Layer%d, DocaBin=%02d)", i + 1, j + 1, k);
                    hArrWire.get(new Coordinate(i, j, k)).setTitleX(hTtl);
                    hArrWire.get(new Coordinate(i, j, k)).setLineColor(i + 1);
                }
            }
        }
        for (int i = 0; i < nSL; i++) {
            hNm = String.format("thetaSL%d", i + 1);
            hTtl = "#theta";
            h1ThSL.put(new Coordinate(i), new H1F(hNm, 120, -60.0, 60.0));
            h1ThSL.get(new Coordinate(i)).setTitle(hTtl);
            h1ThSL.get(new Coordinate(0)).setLineColor(i + 1);
        }
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                for (int k = 0; k < nThBinsVz; k++) {
                    hNm = String.format("timeS%dSL%dThBn%d", i, j, k);
                    h1timeSlTh.put(new Coordinate(i, j, k), new H1F(hNm, 200, tMin, 190.0)); //-10.0, 190.0));
                    hTtl = String.format("time (S=%d, SL=%d, th(%.1f,%.1f)", i + 1, j+1, thEdgeVzL[k], thEdgeVzH[k]);
                    h1timeSlTh.get(new Coordinate(i, j, k)).setTitleX(hTtl);
                    h1timeSlTh.get(new Coordinate(i, j, k)).setLineColor(j + 1);
                }
            }
        }

        String[] hType = {"all hits", "matchedHitID==-1", "Ratio==Ineff."};// as
        // String[];

        for (int i = 0; i < nSL; i++) {
            for (int k = 0; k < 3; k++) { // These are for histos integrated
                // over all layers
                hNm = String.format("trkDocaS%dH%d", i + 1, k);
                h1trkDoca2Dar.put(new Coordinate(i, k), new H1F(hNm, 90, -0.9, 0.9));
                hNm = String.format("NtrkDocaS%dH%d", i + 1, k);
                h1NtrkDoca2Dar.put(new Coordinate(i, k), new H1F(hNm, 120, -1.2, 1.2));
                hNm = String.format("NtrkDocaPS%dH%d", i + 1, k);
                h1NtrkDocaP2Dar.put(new Coordinate(i, k), new H1F(hNm, 120, 0.0, 1.2));

                if (k == 0) {
                    hTtl = String.format("all hits (SL=%d)", i + 1);
                }
                if (k == 1) {
                    hTtl = String.format("matchedHitID==-1 (SL=%d)", i + 1);
                }
                if (k == 2) {
                    hTtl = String.format("Ineff. (SL=%d)", i + 1);
                }
                h1trkDoca2Dar.get(new Coordinate(i, k)).setTitle(hTtl);
                h1NtrkDoca2Dar.get(new Coordinate(i, k)).setTitle(hTtl);
                h1NtrkDocaP2Dar.get(new Coordinate(i, k)).setTitle(hTtl);

                h1trkDoca2Dar.get(new Coordinate(i, k)).setLineColor(i + 1);
                h1NtrkDoca2Dar.get(new Coordinate(i, k)).setLineColor(i + 1);
                h1NtrkDocaP2Dar.get(new Coordinate(i, k)).setLineColor(i + 1);

            }
            for (int j = 0; j < nLayer; j++) {
                for (int k = 0; k < 3; k++) { // These are for histos integrated
                    // over all theta

                    hNm = String.format("trkDocaS%dL%dH%d", i + 1, j + 1, k);
                    h1trkDoca3Dar.put(new Coordinate(i, j, k), new H1F(hNm, 90, -0.9, 0.9));

                    hNm = String.format("NtrkDocaS%dL%dH%d", i + 1, j + 1, k);
                    h1NtrkDoca3Dar.put(new Coordinate(i, j, k), new H1F(hNm, 120, -1.2, 1.2));

                    hNm = String.format("NtrkDocaPS%dL%dH%d", i + 1, j + 1, k);
                    h1NtrkDocaP3Dar.put(new Coordinate(i, j, k), new H1F(hNm, 120, 0.0, 1.2));

                    if (k == 0) {
                        hTtl = String.format("all hits (SL=%d, Layer%d)", i + 1, j + 1);
                    }
                    if (k == 1) {
                        hTtl = String.format("matchedHitID==-1 (SL=%d, Layer%d)", i + 1, j + 1);
                    }
                    if (k == 2) {
                        hTtl = String.format("Ineff. (SL=%d, Layer%d)", i + 1, j + 1);
                    }

                    h1trkDoca3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h1NtrkDoca3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h1NtrkDocaP3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);

                    h1trkDoca3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);
                    h1NtrkDoca3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);
                    h1NtrkDocaP3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);

                }

                for (int th = 0; th < nTh; th++) {
                    for (int k = 0; k < 3; k++) {
                        hNm = String.format("trkDocaS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
                        h1trkDoca4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 90, -0.9, 0.9));

                        if (k == 0) {
                            hTtl = String.format("all hits (SL=%d, Layer%d, th(%.1f,%.1f))", i + 1, j + 1, thBins[th], thBins[th + 1]);
                        }
                        if (k == 1) {
                            hTtl = String.format("matchedHitID==-1 (SL=%d, Layer%d, th(%.1f,%.1f))", i + 1, j + 1, thBins[th],
                                    thBins[th + 1]);
                        }
                        if (k == 2) {
                            hTtl = String.format("Ineff. (SL=%d, Layer%d, th(%.1f,%.1f))", i + 1, j + 1, thBins[th], thBins[th + 1]);
                        }
                        h1trkDoca3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);
                        h1trkDoca3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);

                    }
                    for (int k = 0; k < 2; k++) {
                        hNm = String.format("wireS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
                        h1wire4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 120, -1.0, 119.0));

                        hTtl = String.format("wire # for %s (SL=%d, Lay%d, th(%.1f,%.1f))", hType[k], i + 1, j + 1, thBins[th],
                                thBins[th + 1]);
                        h1wire4Dar.get(new Coordinate(i, j, th, k)).setTitle(hTtl);
                        h1wire4Dar.get(new Coordinate(i, j, th, k)).setLineColor(i + 1);

                        hNm = String.format("avgWireS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
                        h1avgWire4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 120, -1.0, 119.0));

                        hTtl = String.format("avgWire(SegBnk) for %s (SL=%d, Lay%d, th(%.1f,%.1f))", hType[k], i + 1, j + 1, thBins[th],
                                thBins[th + 1]);
                        h1avgWire4Dar.get(new Coordinate(i, j, th, k)).setTitle(hTtl);
                        h1avgWire4Dar.get(new Coordinate(i, j, th, k)).setLineColor(i + 1);

                        hNm = String.format("fitChisqProbS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
                        h1fitChisqProbSeg4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 90, -0.1, 0.1));

                        hTtl = String.format("fitChisqProbSeg(SegBnk) for %s (SL=%d, Lay%d, th(%.1f,%.1f))", hType[k], i + 1, j + 1,
                                thBins[th], thBins[th + 1]);
                        h1fitChisqProbSeg4Dar.get(new Coordinate(i, j, th, k)).setTitle(hTtl);
                        h1fitChisqProbSeg4Dar.get(new Coordinate(i, j, th, k)).setLineColor(i + 1);

                    }
                }
            }
        }

        double dMax;
        //for (int i = 0; i < nSectors; i++) {
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)            
            for (int j = 0; j < nSL; j++) {
                dMax = 2 * wpdist[j];
                for (int k = 0; k < nThBinsVz; k++) { // nThBinsVz theta bins +/-2
                    // deg around 0, 10, 20, 30,
                    // 40, and 50 degs
                    hNm = String.format("Sector %d timeVnormDocaS%dTh%02d", i, j, k);
                    //h2timeVtrkDocaVZ.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.0, 150, 0.0, 200.0));
                    h2timeVtrkDocaVZ.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.0, 150, tMin, timeAxisMax[j]));

                    hTtl = String.format("time vs. |normDoca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).setTitleX("|normDoca|");
                    h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");

                    hNm = String.format("Sector %d timeVtrkDocaS%dTh%02d", i, j, k);
                    //h2timeVtrkDocaVZ.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.0, 150, 0.0, 200.0));
                    //h2timeVtrkDoca.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.2 * dMax, 150, 0.0, timeAxisMax[j]));
                    h2timeVtrkDoca.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.2 * dMax, 150, tMin, timeAxisMax[j]));

                    hTtl = String.format("time vs. |Doca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h2timeVtrkDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h2timeVtrkDoca.get(new Coordinate(i, j, k)).setTitleX("|Doca| (cm)");
                    h2timeVtrkDoca.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");

                    hNm = String.format("Sector %d timeVcalcDocaS%dTh%02d", i, j, k);
                    h2timeVcalcDoca.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.2 * dMax, 150, tMin, timeAxisMax[j]));

                    hTtl = String.format("time vs. |calcDoca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h2timeVcalcDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h2timeVcalcDoca.get(new Coordinate(i, j, k)).setTitleX("|calcDoca| (cm)");
                    h2timeVcalcDoca.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");

                    hNm = String.format("Sector %d timeFitResVtrkDocaS%dTh%02d", i, j, k);
                    h2timeFitResVtrkDoca.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.2 * dMax, 150, -timeAxisMax[j] / 2, timeAxisMax[j] / 2));
                    hTtl = String.format("time - fit vs. |Doca| (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h2timeFitResVtrkDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h2timeFitResVtrkDoca.get(new Coordinate(i, j, k)).setTitleX("|Doca| (cm)");
                    h2timeFitResVtrkDoca.get(new Coordinate(i, j, k)).setTitleY("Time (ns)");

                    hNm = String.format("Sector %d timeFitResS%dTh%02d", i, j, k);
                    h1timeFitRes.put(new Coordinate(i, j, k), new H1F(hNm, 150, -timeAxisMax[j] / 2, timeAxisMax[j] / 2));
                    hTtl = String.format("time - fit (Sec=%d, SL=%d, th(%2.1f,%2.1f))", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h1timeFitRes.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h1timeFitRes.get(new Coordinate(i, j, k)).setTitleX("Time (ns)");
                }
                //SimpleH3D> h3BTXmap
                for (int k = 0; k < nThBinsVz2; k++) {
                    //hNm = String.format("Sector %d BvTvXS%dTh%02d", i, j, k);
                    h3BTXmap.put(new Coordinate(i, j, k), new SimpleH3D(100, 0.0, 1.2 * dMax, 100, tMin, timeAxisMax[j], 60, 0.0, 1.2));
                    //h3BTXmapVZ.put(new Coordinate(i, j, k), new SimpleH3D(100, 0.0, 1.0, 100, 0.0, timeAxisMax[j], 60, 0.0, 1.2));
                }
            }
        }

        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)            
            for (int j = 0; j < nSL; j++) {
                dMax = 2 * wpdist[j];

                //Following is used for all angle-bins combined
                hNm = String.format("timeResS%dSL%d", i, j);
                h1timeRes.put(new Coordinate(i, j), new H1F(hNm, 200, -1.0, 1.0));
                hTtl = String.format("residual (cm) (Sec=%d, SL=%d)", i, j + 1);
                h1timeRes.get(new Coordinate(i, j)).setTitle(hTtl);
                h1timeRes.get(new Coordinate(i, j)).setTitleX("residual");

                //h2timeResVsTrkDoca
                hNm = String.format("timeResVsTrkDocaS%dSL%d", i, j);
                h2timeResVsTrkDoca.put(new Coordinate(i, j), new H2F(hNm, 200, 0.0, 1.2 * dMax, 200, -1.0, 1.0));
                hTtl = String.format("residual (cm) (Sec=%d, SL=%d)", i, j + 1);
                h2timeResVsTrkDoca.get(new Coordinate(i, j)).setTitle(hTtl);
                h2timeResVsTrkDoca.get(new Coordinate(i, j)).setTitleX("|trkDoca|");
                h2timeResVsTrkDoca.get(new Coordinate(i, j)).setTitleY("residual");

                //Following is used for individual angle bins
                for (int k = 0; k < nThBinsVz; k++) {
                    hNm = String.format("timeResS%dSL%dTh%02d", i, j, k);
                    h1timeRes.put(new Coordinate(i, j, k), new H1F(hNm, 200, -1.0, 1.0));
                    hTtl = String.format(" Sec=%d, SL=%d, Th(%2.1f,%2.1f)", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h1timeRes.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h1timeRes.get(new Coordinate(i, j, k)).setTitleX("residual");

                    //h2timeResVsTrkDoca
                    hNm = String.format("timeResVsTrkDocaS%dSL%d", i, j, k);
                    h2timeResVsTrkDoca.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.2 * dMax, 200, -1.0, 1.0));
                    hTtl = String.format("Sec=%d, SL=%d, Th(%2.1f,%2.1f)", i, j + 1, thEdgeVzL[k], thEdgeVzH[k]);
                    h2timeResVsTrkDoca.get(new Coordinate(i, j, k)).setTitle(hTtl);
                    h2timeResVsTrkDoca.get(new Coordinate(i, j, k)).setTitleX("|trkDoca|");
                    h2timeResVsTrkDoca.get(new Coordinate(i, j, k)).setTitleY("residual");
                }
            }
        }
    }

    private void initializeBFieldHistograms() {
        //Overall for SL=3 & 4
        h1bField = new H1F("Bfield", bFieldBins, bFieldMin, bFieldMax);
        h1bField.setTitle("B field");
        h1bField.setLineColor(2);

        //For each of the 6 SLs
        String hName = "", hTitle = "";
        for (int i = 0; i < nSL; i++) {
            String.format(hName, "BfieldSL%d", i + 1);
            h1bFieldSL[i] = new H1F(hName, 4 * bFieldBins, bFieldMin, bFieldMax);
            String.format(hTitle, "B field for SL=%d", i + 1);
            h1bField.setTitle(hTitle);
            h1bField.setLineColor(2);
        }

    }

    protected void processData() {
        int counter = 0;
        int icounter = 0;
        int ndf = -1;
        double chi2 = -1.0, Vtx0_z = -10000.0;

//       
//		for (String str : fileArray) {
//			reader.addFile(str);
//		}
//                reader.open();
//         
//        //readerH.open("src/files/DCRBREC.hipo");
        for (String str : fileArray) { //Now reading multiple hipo files.
            System.out.println("Ready to Open & read " + str);
            readerH.open(str);

            while (readerH.hasEvent()) {// && icounter < 100

                icounter++;
                if (icounter % 2000 == 0) {
                    System.out.println("Processed " + icounter + " events.");
                }
                //EvioDataEvent event = reader.getNextEvent();
                DataEvent event = readerH.getNextEvent();
//                  //got 'bank not found' message for each event.
//			ProcessTBSegmentTrajectory tbSegmentTrajectory = new ProcessTBSegmentTrajectory(event);
//			if (tbSegmentTrajectory.getNsegs() > 0) {
//				counter++;
//			}
//                 
                if (event.hasBank("TimeBasedTrkg::TBSegmentTrajectory")) {
                    counter++;
                }

                if (event.hasBank("TimeBasedTrkg::TBHits") && event.hasBank("TimeBasedTrkg::TBSegments")//) {// && event.hasBank("TimeBasedTrkg::TBSegmentTrajectory") &&
                        && event.hasBank("TimeBasedTrkg::TBTracks")) {

                    processTBTracksAndCrosses(event); //Identify corresponding segments (4/13/17)

                    ProcessTBTracks tbTracks = new ProcessTBTracks(event);
                    if (tbTracks.getNTrks() > 0) {
                        //processTBhits(event);
                        //processTBSegments(event);

                        //===========
                        bnkTrks = (DataBank) event.getBank("TimeBasedTrkg::TBTracks");
                        for (int j = 0; j < bnkTrks.rows(); j++) {
                            chi2 = (double) bnkTrks.getFloat("chi2", j);
                            ndf = bnkTrks.getInt("ndf", j);
                            Vtx0_z = (double) bnkTrks.getFloat("Vtx0_z", j);
                            h1fitChi2Trk.fill(chi2);
                            h1ndfTrk.fill(1.0 * ndf);
                            h1zVtx.fill(Vtx0_z);
                            if (Vtx0_z > -3.0 && Vtx0_z < 5.0) ///if(Math.abs(Vtx0_z)<10.0) 
                            {
                                h1fitChi2Trk2.fill(chi2);
                            }
                        }
                        //===========
                    }
                }

            }
        }
        System.out.println(
                "processed " + counter + " Events with TimeBasedTrkg::TBSegmentTrajectory entries from a total of " + icounter + " events");
        saveNtuple();
    }

    private void processTBTracksAndCrosses(DataEvent event) {
        int[][] crossIDs;//[nTrk][3], with [3] for 3 crosses from R1/2/3
        double[] trkFitChi2; //[nTrk]

        int[] crossID = {-1, -1, -1};
        //int [][][] segmentIDs; //[nTracks][3][2] //3 for crosses per track, 2 for segms per cross. //Now global
        //double [] trkChi2;//Size equals the # of tracks for the event //Now global
        //int nTracks = 0; //Now global
        int segmID1 = -1, segmID2 = -1, id = -1;

        double chi2 = 1000000.0;
        boolean hasTracks = event.hasBank("TimeBasedTrkg::TBTracks");
        boolean hasCrosses = event.hasBank("TimeBasedTrkg::TBCrosses");
        boolean hasSegments = event.hasBank("TimeBasedTrkg::TBSegments");
        DataBank bnkTracks, bnkCrosses, bnkSegments;
        if (hasTracks) {
            bnkTracks = (DataBank) event.getBank("TimeBasedTrkg::TBTracks");
            nTracks = bnkTracks.rows();
            crossIDs = new int[nTracks][3];
            trkFitChi2 = new double[nTracks];
            //# of valid segments: nTracks*3*2, because each will have 3 crosses, and each cross has 2 segments.
            segmentIDs = new int[nTracks][3][2];
            trkChi2 = new double[nTracks][3][2];
            for (int j = 0; j < bnkTracks.rows(); j++) {
                crossIDs[j][0] = bnkTracks.getInt("Cross1_ID", j);//Region 1
                crossIDs[j][1] = bnkTracks.getInt("Cross2_ID", j);//R2
                crossIDs[j][2] = bnkTracks.getInt("Cross3_ID", j);//R3
                trkFitChi2[j] = (double) bnkTracks.getFloat("chi2", j);
            }

            if (hasCrosses) {
                bnkCrosses = (DataBank) event.getBank("TimeBasedTrkg::TBCrosses");
                for (int i = 0; i < bnkCrosses.rows(); i++) {
                    id = bnkCrosses.getInt("id", i);
                    segmID1 = bnkCrosses.getInt("Segment1_ID", i);
                    segmID2 = bnkCrosses.getInt("Segment2_ID", i);
                    for (int j = 0; j < nTracks; j++) {
                        for (int k = 0; k < 3; k++) {
                            if (id == crossIDs[j][k]) {
//                                System.out.println("nTrks=" + nTracks
//                                        + " CrossIndex=" + k + " id = " + bnkCrosses.getInt("id", i)
//                                        + " region = " + bnkCrosses.getInt("region", i));
                                segmentIDs[j][k][0] = segmID1;
                                segmentIDs[j][k][1] = segmID2;
                                trkChi2[j][k][0] = trkFitChi2[j];
                                trkChi2[j][k][1] = trkFitChi2[j];
//                                System.out.println("crossID trkChi2  seg1id seg2id: " + k + " " + chi2 
//                                        + " " + segmentIDs[j][k][0] + " " + segmentIDs[j][k][1]);
                            }
                        }
                    }
                }
            }

            processTBhits(event);
            if (hasSegments) {
                processTBSegments(segmentIDs, trkChi2, event);
            }
        }
    }

    //private void processTBhits(EvioDataEvent event) {
    private void processTBhits(DataEvent event) {
        double bFieldVal = 0.0;
        int sector = -1, superlayer = -1;
        layerMapTBHits = new HashMap<Integer, Integer>();
        wireMapTBHits = new HashMap<Integer, Integer>();
        timeMapTBHits = new HashMap<Integer, Double>();
        trkDocaMapTBHits = new HashMap<Integer, Double>();
        calcDocaMapTBHits = new HashMap<Integer, Double>();
        timeResMapTBHits = new HashMap<Integer, Double>();
        BMapTBHits = new HashMap<Integer, Double>();

        bnkHits = (DataBank) event.getBank("TimeBasedTrkg::TBHits");
        for (int j = 0; j < bnkHits.rows(); j++) {
            layerMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("layer", j));
            wireMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("wire", j));
            timeMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("time", j));
            //trkDocaMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getDouble("trkDoca", j));
            //BMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getDouble("B", j));
            trkDocaMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("trkDoca", j));
            calcDocaMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("doca", j));
            timeResMapTBHits.put(bnkHits.getInt("id", j), (double) bnkHits.getFloat("timeResidual", j));
            bFieldVal = (double) bnkHits.getFloat("B", j);
            sector = bnkHits.getInt("sector", j);
            superlayer = bnkHits.getInt("superlayer", j);
            BMapTBHits.put(bnkHits.getInt("id", j), bFieldVal);
            h1bFieldSL[superlayer - 1].fill(bFieldVal);
            if (superlayer == 3 || superlayer == 4) {
                h1bField.fill(bFieldVal); //for a quick look
            }            //System.out.println("S = " + sector + "superlayer = " + superlayer + " B = " + bFieldVal);
            //System.out.println("B = " + BMapTBHits.get(j));
            int docaBin = (int) (((double) bnkHits.getFloat("trkDoca", j) - (-0.8)) / 0.2);
            if (bnkHits.getInt("sector", j) == 1 && (docaBin > -1 && docaBin < 8)) {
                hArrWire.get(new Coordinate(bnkHits.getInt("superlayer", j) - 1, bnkHits.getInt("layer", j) - 1, docaBin))
                        .fill(bnkHits.getInt("wire", j));
            }
        }
    }

    //private void processTBSegments(EvioDataEvent event) {
    private void processTBSegments(int[][][] segmIDs, double[][][] trkChi2, DataEvent event) {
        boolean validSegm = false;
        double trkChiSq = 1000000.0;//Just giving a very big value of trk-fit-chi-square (for bad fits, its a big #)
        gSegmThBinMapTBSegments = new HashMap<Integer, Integer>();
        gSegmAvgWireTBSegments = new HashMap<Integer, Double>();
        gFitChisqProbTBSegments = new HashMap<Integer, Double>();

        bnkSegs = (DataBank) event.getBank("TimeBasedTrkg::TBSegments");
        int nHitsInSeg = 0, idSeg = -1;
        for (int j = 0; j < bnkSegs.rows(); j++) {
            int superlayer = bnkSegs.getInt("superlayer", j);
            int sector = bnkSegs.getInt("sector", j);
            //System.out.println("superlayer sector" + superlayer + " " + sector);

            //Check if any of these segments matches with those associated with the available tracks
            //   Else continue
            idSeg = bnkSegs.getInt("id", j);
            validSegm = false;
            //System.out.println("nTracks: " + nTracks);
            for (int i = 0; i < nTracks; i++) {
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 2; l++) {
                        //System.out.println("idSeg  segTrks: " + idSeg + " " + segmentIDs[i][k][l]);
                        if (idSeg == segmIDs[i][k][l]) { //segmentIDs[i][k][l]
                            validSegm = true;
                            trkChiSq = trkChi2[i][k][l];
                        }
                    }
                }
            }
            if (validSegm == false) {
                continue;
            }
            if (trkChiSq > 2000.0) {
                continue;
            }

            //int [][][] segmentIDs; //[nTrks][3][2] //3 for crosses per track, 2 for segms per cross. //Now global
            //double [] trkChi2;//Size equals the # of tracks for the event //Now global
            //int nTracks = 0; //Now global
            //It turns out there were cases of sector==1 & superlayer ==1 in pass2 data (4/11/17)
            //   causing the program to crash. Therefore I had to put the following line.
            if (sector < 1 || superlayer < 1) {
                continue;
            }

            //gSegmAvgWireTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("avgWire", j));
            //gFitChisqProbTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("fitChisqProb", j));
            h1fitChisqProb.fill((double) bnkSegs.getFloat("fitChisqProb", j));

            double thDeg = rad2deg * Math.atan2((double) bnkSegs.getFloat("fitSlope", j), 1.0);
            //System.out.println("superlayer thDeg " + superlayer + " " + thDeg);
            h1ThSL.get(new Coordinate(bnkSegs.getInt("superlayer", j) - 1)).fill(thDeg);
            for (int h = 1; h <= 12; h++) {
                if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1) {
                    nHitsInSeg++;
                }
            }
            int thBn = -1, thBnVz = -1, thBnVz2 = -1;
            for (int th = 0; th < nTh; th++) {
                if (thDeg > thBins[th] && thDeg <= thBins[th + 1]) {
                    thBn = th;
                }
            }
            for (int th = 0; th < nThBinsVz; th++) {
                if (thDeg > thEdgeVzL[th] && thDeg <= thEdgeVzH[th]) {
                    thBnVz = th;
                }
            }
            //3/26/17: following bins are of size 2 degrees each.
            for (int th = 0; th < nThBinsVz2; th++) {
                if (thDeg > thEdgeVzL2[th] && thDeg <= thEdgeVzH2[th]) {
                    thBnVz2 = th;
                }
            }
            gSegmThBinMapTBSegments.put(bnkSegs.getInt("id", j), thBn);
            double thTmp1 = thDeg;
            double thTmp2 = thDeg - 30.0;
            double docaMax = 2.0 * wpdist[superlayer - 1];
            for (int h = 1; h <= 12; h++) {
                if (nHitsInSeg > 5)// Saving only those with more than 5 hits
                {
                    Double gTime = timeMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
                    Double gTrkDoca = trkDocaMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
                    Double gCalcDoca = calcDocaMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
                    Double gTimeRes = timeResMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
                    Double gBfield = BMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
                    

                    if (gTime == null || gTrkDoca == null && gBfield == null) {
                        continue;
                    }
                    double gCalcDocaNorm = gCalcDoca/docaMax;

                    //============ For Temp purpose
                    boolean inBfieldBin = true; //For SL=3 & 4, using only data that correspond to Bfield = (0.4,0.6)
                    if ((superlayer == 3 || superlayer == 4) && (gBfield < 0.4 || gBfield > 0.6)) {
                        inBfieldBin = false;
                    }
                    //This allows to use an average default value of 0.5 Tesla for B-field in classes such as DCTimeFunction
                    //============ Temp                    

//                    if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBn > -1 && thBn < nTh) {
//                        h1timeSlTh.get(new Coordinate(superlayer - 1, thBn)).fill(gTime);
//                    }
                    
                    if (gCalcDocaNorm < calcDocaCut) { //1.0) { //0.85) { //kp: 6/7/17
                        if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz > -1 && thBnVz < nThBinsVz && inBfieldBin == true) {// && thBnVz < nThBinsVz
                            double docaNorm = gTrkDoca / docaMax;
                            h2timeVtrkDocaVZ.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(Math.abs(docaNorm), gTime);
                            h2timeVtrkDoca.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(Math.abs(gTrkDoca), gTime);
                            h2timeVcalcDoca.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(Math.abs(gCalcDoca), gTime);
                            if (Math.abs(thDeg) < 30.0) {
                                h1timeSlTh.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(gTime);
                                //Following two for all angle-bins combined (but for individual superlayers in each sector)
                                h1timeRes.get(new Coordinate(sector - 1, superlayer - 1)).fill(gTimeRes);
                                h2timeResVsTrkDoca.get(new Coordinate(sector - 1, superlayer - 1)).fill(Math.abs(gTrkDoca), gTimeRes);
                                //Following tow for individual angular bins as well.
                                h1timeRes.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(gTimeRes);
                                h2timeResVsTrkDoca.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(Math.abs(gTrkDoca), gTimeRes);
                                h2ResidualVsTrkDoca.fill(gTrkDoca, gTimeRes);
                                if (gTimeRes > 0.0) {
                                    h1trkDoca4PosRes.fill(gTrkDoca);
                                } else {
                                    h1trkDoca4NegRes.fill(gTrkDoca);
                                }
                            }
                        }

                        if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz2 > -1 && thBnVz2 < nThBinsVz2) {
                            double docaNorm = gTrkDoca / docaMax;
                            //System.out.print("B-fill: "+ sector+" "+superlayer+" "+thBnVz2+" "+gTrkDoca+" "+gTime+" "+gBfield);
                            h3BTXmap.get(new Coordinate(sector - 1, superlayer - 1, thBnVz2)).fill(Math.abs(gTrkDoca), gTime, gBfield);
                            //System.out.println("  la la ..");
                        }
                    }
                    // here I will fill a test histogram of superlay6 and thetabin6
                    if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz == 5 && superlayer == 6) {
                        double docaNorm = gTrkDoca / docaMax;
                        tupleVars[0] = (double) sector;
                        tupleVars[1] = (double) superlayer;
                        tupleVars[2] = (double) thBnVz;
                        tupleVars[3] = Math.abs(docaNorm);
                        tupleVars[4] = gTime;
//                        nTupletimeVtrkDocaVZ.addRow(tupleVars);
                        testHist.fill(Math.abs(docaNorm), gTime);
                    }

                }
            }
        }

    }

    protected void runFitterAndDrawPlots(JFrame frame, JTextArea textArea, int Sec, int SL,
            int xMeanErrorType, double xNormLow, double xNormHigh, boolean[] fixIt, boolean checkBoxFixAll, 
            double[][] pLow, double[][] pInit, double[][] pHigh, double[][] pSteps, boolean [] selectedAngleBins) {
        System.out.println(String.format("%s %d %d %d %2.1f %2.1f",
                "Selected values of Sector Superlayer errorType xNorm(Min,Max) are:",
                Sec, SL, xMeanErrorType, xNormLow, xNormHigh));
        int iSL = SL - 1;
        System.out.println("parLow   parInit    parHigh    FixedStatus");
        for (int i = 0; i < nFitPars; i++) {
            System.out.println(String.format("%5.4f    %5.4f   %5.4f   %b", pLow[iSL][i],
                    pInit[iSL][i], pHigh[iSL][i], fixIt[i]));
        }

        System.out.println("Called createCanvasMaps(); ");
        drawQuickTestPlots();
        System.out.println("Called drawQuickTestPlots();");

        try {
            runFitterNew(textArea, Sec, SL, xMeanErrorType, xNormLow, xNormHigh, fixIt, checkBoxFixAll, pLow, 
                    pInit, pHigh, pSteps, selectedAngleBins);
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Called runFitterNew(Sec, SL, fixIt, pLow, pInit, pHigh);");
        createFitLinesNew(Sec, SL);
        System.out.println("Fit lines are prepared.");
        drawFitLinesNew(frame, Sec, SL);
    }
    

    protected void runFitterNew(JTextArea textArea, int Sec, int SL, int xMeanErrorType,
            double xNormLow, double xNormHigh, boolean[] fixIt, boolean checkBoxFixAll,
            double[][] pLow, double[][] pInit, double[][] pHigh, double [][] pSteps, boolean [] selectedAngleBins) throws IOException {

        System.out.println("Inside runFitterNew(..) ");
        int iSec = Sec - 1, iSL = SL - 1;
        boolean append_to_file = false;
        FileOutputWriter file = null;
        String str = " ", pStr = " ";
        try {
            file = new FileOutputWriter("src/files/fitParameters.txt", append_to_file);
            file.Write("#Sec  SL  v0  deltanm  tMax  distbeta  delta_bfield_coefficient  b1  b2  b3  b4");
        } catch (IOException ex) {
            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
        }

        int nFreePars = nFitPars; //9;//4;
//        if (histTypeToUseInFitting == 3) {
//            nFreePars = 9;
//        }

        double[][][] pars2write = new double[nSectors][nSL][nFitPars];//nFitPars = 9
        //double parSteps[] = {0.00001, 0.001, 0.01, 0.0001, 0.001, 0.001, 0.001, 0.001, 0.001, 0.001};

        Map<Coordinate, MnUserParameters> mapTmpUserFitParameters = new HashMap<Coordinate, MnUserParameters>();
        

        if (histTypeToUseInFitting < 2) {
            mapOfFitFunctions.put(new Coordinate(iSec, iSL),
                    //new DCFitFunction(h2timeVtrkDocaVZ, iSec, iSL, isLinearFit));
                    new DCFitFunction(h2timeVtrkDoca, iSec, iSL, xMeanErrorType, xNormLow, xNormHigh, isLinearFit, selectedAngleBins));//Using map of H2F
        } else if (histTypeToUseInFitting > 1) {
            mapOfFitFunctions.put(new Coordinate(iSec, iSL),
                    new DCFitFunction(h3BTXmap, iSec, iSL, xMeanErrorType, xNormLow, xNormHigh, isLinearFit, selectedAngleBins, true)); //Using map of SimpleH3D
        }
        
        mapOfFitParameters.put(new Coordinate(iSec, iSL), new MnUserParameters());
        
        for (int p = 0; p < nFreePars; p++) {            
            mapOfFitParameters.get(new Coordinate(iSec, iSL)).add(parName[p], pInit[iSL][p], pSteps[iSL][p], pLow[iSL][p], pHigh[iSL][p]);
            //mapOfFitParameters.get(new Coordinate(iSec, iSL)).add(parName[p], pInit[iSL][p], parSteps[p], pLow[iSL][p], pHigh[iSL][p]);
            if (fixIt[p] == true) {
                mapOfFitParameters.get(new Coordinate(iSec, iSL)).fix(p);
            }
        }

        System.out.println("Debug1");

        double[] fPars = new double[nFreePars];
        double[] fErrs = new double[nFreePars];
        //Following is to ensure that initial values are written as output if all parameters are fixed i.e. when checkBoxFixAll == true;
        for (int p = 0; p < nFreePars; p++) { //Don't delete
            fPars[p] = pInit[iSL][p];
        }

        //If all the parameters are fixed, don't run Minuit
        if (checkBoxFixAll == false) {
            System.out.println("Debug10");
            MnMigrad migrad
                    = new MnMigrad(mapOfFitFunctions.get(new Coordinate(iSec, iSL)), mapOfFitParameters.get(new Coordinate(iSec, iSL)));
            FunctionMinimum min = migrad.minimize();
            System.out.println("Debug100");
            if (!min.isValid()) {
                // try with higher strategy
                System.out.println("FM is invalid, try with strategy = 2.");
                MnMigrad migrad2 = new MnMigrad(mapOfFitFunctions.get(new Coordinate(iSec, iSL)), min.userState(), new MnStrategy(2));
                min = migrad2.minimize();
            }
            System.out.println("Debug11");
            mapTmpUserFitParameters.put(new Coordinate(iSec, iSL), min.userParameters());
            System.out.println("Debug12");
            for (int p = 0; p < nFreePars; p++) {
                fPars[p] = mapTmpUserFitParameters.get(new Coordinate(iSec, iSL)).value(parName[p]);
                fErrs[p] = mapTmpUserFitParameters.get(new Coordinate(iSec, iSL)).error(parName[p]);
            }
        }

        pStr = "  ";
        for (int p = 0; p < nFreePars; p++) {
            pars2write[iSec][iSL][p] = fPars[p];
            pStr = String.format("%s %5.4f ", pStr, fPars[p]);
        }
        str = String.format("%d %d %s", iSec + 1, iSL + 1, pStr);
        
        System.out.println("Debug2");

        mapOfUserFitParameters.put(new Coordinate(iSec, iSL), fPars);
                
        if (!(file == null)) {            
            file.Write(str);
            textArea.append(str + "\n"); //Show the results in the text area of fitControlUI
            file.Close();
        }

        if (nFreePars > 9) {
            System.out.println("deltaT0 determined from the time-to-distance fit: " + fPars[9]);
            ReadT0parsFromCCDB readT0 = new ReadT0parsFromCCDB("dc_test1");
            //readT0.printCurrentT0s();
            readT0.printModifiedT0s(iSec+1, iSL+1, fPars[9]);
            readT0.writeOutModifiedT0s(iSec+1, iSL+1, fPars[9]);
        }

        System.out.println("End of runFitterNew(..) ");
    }

    
    
    
    private void createFitLinesNew(int Sec, int SL) {
        int iSec = Sec - 1, iSL = SL - 1;
        double bField = 0.0;
        double dMax = 2 * wpdist[iSL];
        for (int k = 0; k < nThBinsVz; k++) {
            String title = "timeVsNormDoca Sec=" + (iSec + 1) + " SL=" + (iSL + 1) + " Th=" + k;
            double maxFitValue = h2timeVtrkDocaVZ.get(new Coordinate(iSec, iSL, k)).getDataX(getMaximumFitValue(iSec, iSL, k));
            if (histTypeToUseInFitting < 3) {
                mapOfFitLines.put(new Coordinate(iSec, iSL, k), new DCFitDrawer(title, 0.0, 1.0, iSL, k, isLinearFit));
                mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setLineColor(4);//(2);
                mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setLineWidth(3);
                mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setLineStyle(4);
                //mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL, k)));
                //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL)));
            }

            title = "timeVsTrkDoca Sec=" + (iSec + 1) + " SL=" + (iSL + 1) + " Th=" + k;
            //In case of (histTypeToUseInFitting == 3), we'll draw the histograms and the fit line for only a particular value
            //   of B-field (for now, corresponding to some B-bin in the middle of its range which is (0.0,1.2) for now)

            if (histTypeToUseInFitting == 3) {
                //Want some middle bin so that bField is about bFieldMax/2;
                //The fit lines would be different for different values of B-field and it would
                //  be clumsy to show the T-vs-X 2D histograms corresponding to all the B-bins.
                // So, for a quick test, I decided to draw for only one bin ( or may be couple more bins later)
                bField = h3BTXmap.get(new Coordinate(iSec, iSL, k)).getBinCenterZ(binForTestPlotTemp);
            } else {
                bField = 0.5; //For now I am using only events with B(0.4,0.6) for SL=3/4 (6/5/17)
            }
            
            mapOfFitLinesX.put(new Coordinate(iSec, iSL, k), new DCFitDrawerForXDoca(title, 0.0, dMax, iSL, k, bField, isLinearFit));
            
            mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setLineColor(1);//(colSimulFit);//(2);
            mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setLineWidth(3);
            mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setLineStyle(3);
            //mapOfFitLines.get(new Coordinate(iSec, iSL, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL, k)));
            //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
            mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(iSec, iSL)));
        }
    }


    private void drawFitLinesNew(JFrame fitControlFrame, int Sec, int SL) {
        int iSec = Sec - 1, iSL = SL - 1;
        int nSkippedThBins = 4; //Skipping marginal 4 bins from both sides
        String Title = "";

        JTabbedPane tabbedPane = new JTabbedPane();

        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.setSize(3 * 400, 3 * 400);
        canvas.divide(3, 3);
        for (int k = nSkippedThBins; k < nThBinsVz - nSkippedThBins; k++) {
            canvas.cd(k - nSkippedThBins);
            Title = "Sec=" + Sec + " SL=" + SL
                    + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")"
                    + " indvFitCol=" + colIndivFit;
            if (histTypeToUseInFitting < 2) {
                canvas.draw(h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k)));
            } else if (histTypeToUseInFitting == 2) {
                canvas.draw(h3BTXmap.get(new Coordinate(iSec, iSL, k)).getXYProj());
            } else if (histTypeToUseInFitting == 3 && (iSL == 2 || iSL == 3)) {
                canvas.draw(h3BTXmap.get(new Coordinate(iSec, iSL, k)).getSliceZ(binForTestPlotTemp));//Tmp
            }
            canvas.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)), "same");
            //canvas.draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
            canvas.getPad(k - nSkippedThBins).setTitle(Title);
            canvas.setPadTitlesX("trkDoca");
            canvas.setPadTitlesY("time (ns)");
            canvas.draw(vertLineDmax[iSL], "same");
        }
        tabbedPane.add(canvas, "t vs trkDoca");

        EmbeddedCanvas canvas2 = new EmbeddedCanvas();
        canvas2.setSize(3 * 400, 3 * 400);
        canvas2.divide(3, 3);
        for (int k = nSkippedThBins; k < nThBinsVz - nSkippedThBins; k++) {
            canvas2.cd(k - nSkippedThBins);
            Title = "Sec=" + Sec + " SL=" + SL
                    + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")"
                    + " indvFitCol=" + colIndivFit;
            if (histTypeToUseInFitting < 2) {
                canvas2.draw(h2timeVtrkDoca.get(new Coordinate(iSec, iSL, k)).getProfileX());
            } else if (histTypeToUseInFitting == 2) {
                canvas2.draw(h3BTXmap.get(new Coordinate(iSec, iSL, k)).getXYProj().getProfileX());
            }
            canvas2.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)), "same");
            //canvas.draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
            canvas2.getPad(k - nSkippedThBins).setTitle(Title);
            canvas2.setPadTitlesX("trkDoca");
            canvas2.setPadTitlesY("time (ns)");
            canvas2.draw(vertLineDmax[iSL], "same");
        }
        tabbedPane.add(canvas2, "X-profiles & fits");

        //Now Drawing a new tab, with the fit line and the time-vs-calcDoca
        EmbeddedCanvas canvas3 = new EmbeddedCanvas();
        canvas3.setSize(3 * 400, 3 * 400);
        canvas3.divide(3, 3);
        for (int k = nSkippedThBins; k < nThBinsVz - nSkippedThBins; k++) {
            canvas3.cd(k - nSkippedThBins);
            Title = "Sec=" + Sec + " SL=" + SL
                    + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")"
                    + " indvFitCol=" + colIndivFit;

            canvas3.draw(h2timeVcalcDoca.get(new Coordinate(iSec, iSL, k)));
            canvas3.draw(mapOfFitLinesX.get(new Coordinate(iSec, iSL, k)), "same");
            canvas3.getPad(k - nSkippedThBins).setTitle(Title);
            canvas3.setPadTitlesX("trkDoca");
            canvas3.setPadTitlesY("time (ns)");
            canvas3.draw(vertLineDmax[iSL], "same");
            canvas3.draw(vertLineDmaxCos30[iSL], "same");
            canvas3.draw(vertLineDmaxCosTh[iSL][k], "same");
        }
        tabbedPane.add(canvas3, "t vs calcDoca");

        JFrame frame = new JFrame();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
        //frame.setLocationRelativeTo(null); //Centers on the default screen
        //Following line makes the canvas or frame open in the same screen where the fitCtrolUI is.
        frame.setLocationRelativeTo(fitControlFrame);//centered w.r.t fitControlUI frame
        frame.add(tabbedPane);//(canvas);
        frame.setVisible(true);
    }        
        
        
    public void showResidualDistributions(JFrame fitControlFrame, int Sec, int SL, double xNormLow, double xNormHigh) {
        int iSec = Sec - 1, iSL = SL - 1;
        int nSkippedThBins = 4; //Skipping marginal 4 bins from both sides
        String Title = "";

        JTabbedPane tabbedPane = new JTabbedPane();
        //============= Residual plots
        F1D[][] func = new F1D[nSectors][nSL];
        int iPad = 0;
        //Following two tabs are for overall residuals (1D & 2D) for the given sector and SL
        EmbeddedCanvas canvas4 = new EmbeddedCanvas(); //Will provide tab for 1D res.
        canvas4.setSize(3 * 400, 2 * 400);
        canvas4.divide(3, 2);
        for (int j = 0; j < nSL; j++) {
            Title = "Sec=" + (iSec + 1) + " SL=" + (j + 1);
            iPad = j;
            canvas4.cd(iPad);
            H1F h1 = h1timeRes.get(new Coordinate(iSec, j));
            func[iSec][j] = new F1D("func", "[amp]*gaus(x,[mean],[sigma])", -0.07, 0.07);
            func[iSec][j].setLineColor(2);
            func[iSec][j].setLineStyle(1);
            func[iSec][j].setLineWidth(2);
            func[iSec][j].setParameter(0, 1000);
            func[iSec][j].setParameter(1, 0.0);
            func[iSec][j].setParameter(2, 0.05);//500 micron            
            func[iSec][j].show(); //Prints fit parameters                    
            DataFitter.fit(func[iSec][j], h1, "E");
            func[iSec][j].setOptStat(1110);//(1110000111);//(1110);
            h1.setOptStat(1110);//(1110001111);//(1110);

            canvas4.draw(h1);
            canvas4.getPad(iPad).setTitle(Title);
            canvas4.setPadTitlesX("residual (cm)");//"Residual vs trkDoca"
        }
        //canvas4.save(String.format("src/images/residualSec%d.png", i + 1));
        tabbedPane.add(canvas4, "residual (cm)");

        EmbeddedCanvas canvas5 = new EmbeddedCanvas();
        canvas5.setSize(3 * 400, 2 * 400);
        canvas5.divide(3, 2);
        for (int j = 0; j < nSL; j++) {
            Title = "Sec=" + (iSec + 1) + " SL=" + (j + 1);
            iPad = j;
            canvas5.cd(iPad);
            canvas5.getPad(iPad).getAxisZ().setLog(true);
            canvas5.draw(h2timeResVsTrkDoca.get(new Coordinate(iSec, j)));
            canvas5.getPad(iPad).setTitle(Title);
            canvas5.setPadTitlesX("|trkDoca| (cm)");
            canvas5.setPadTitlesY("residual (cm)");
        }
        //canvas5.save(String.format("src/images/residualVsTrkDocaSec%d.png", i + 1));
        tabbedPane.add(canvas5, "Residual vs trkDoca"); //Second tab in the resPanes
      
        
        //Following two tabs are for individual residuals (1D & 2D) for the given sec, SL & th-bin
        F1D[][][] funcTh = new F1D[nSectors][nSL][nThBinsVz];
        EmbeddedCanvas canvas6 = new EmbeddedCanvas(); //Will provide tab for 1D res.
        canvas6.setSize(3 * 400, 3 * 400);
        canvas6.divide(3, 3);
        for (int k = nSkippedThBins; k < nThBinsVz - nSkippedThBins; k++) {            
        //for (int j = 0; j < nSL; j++) {
            Title = "Sec=" + (iSec + 1) + " SL=" + (iSL + 1) 
                    + " th(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
            iPad = k - nSkippedThBins; //= iSL;
            canvas6.cd(iPad);
            H1F h1 = h1timeRes.get(new Coordinate(iSec, iSL, k));
            funcTh[iSec][iSL][k] = new F1D("func", "[amp]*gaus(x,[mean],[sigma])", -0.07, 0.07);
            funcTh[iSec][iSL][k].setLineColor(2);
            funcTh[iSec][iSL][k].setLineStyle(1);
            funcTh[iSec][iSL][k].setLineWidth(2);
            funcTh[iSec][iSL][k].setParameter(0, 1000);
            funcTh[iSec][iSL][k].setParameter(1, -0.0);
            funcTh[iSec][iSL][k].setParameter(2, 0.05);
            funcTh[iSec][iSL][k].setOptStat(1110);
            funcTh[iSec][iSL][k].show(); //Prints fit parameters                    
            DataFitter.fit(funcTh[iSec][iSL][k], h1, "E");
            funcTh[iSec][iSL][k].setOptStat(1110);//(1110000111);//(1110);
            h1.setOptStat(1110);//(1110001111);//(1110);
            
            canvas6.draw(h1);
            canvas6.getPad(iPad).setTitle(Title);
            canvas6.setPadTitlesX("residual (cm)");//"Residual vs trkDoca"
        }
        //canvas4.save(String.format("src/images/residualSec%d.png", i + 1));
        tabbedPane.add(canvas6, "residual (cm) (In ThBins)");

        EmbeddedCanvas canvas7 = new EmbeddedCanvas();
        canvas7.setSize(3 * 400, 3 * 400);
        canvas7.divide(3, 3);
        for (int k = nSkippedThBins; k < nThBinsVz - nSkippedThBins; k++) {            
        //for (int j = 0; j < nSL; j++) {
            Title = "Sec=" + (iSec + 1) + " SL=" + (iSL + 1) 
                    + " th(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
            iPad = k - nSkippedThBins; //= iSL;
            canvas7.cd(iPad);
            canvas7.getPad(iPad).getAxisZ().setLog(true);
            canvas7.draw(h2timeResVsTrkDoca.get(new Coordinate(iSec, iSL, k)));
            canvas7.getPad(iPad).setTitle(Title);
            canvas7.setPadTitlesX("|trkDoca| (cm)");
            canvas7.setPadTitlesY("residual (cm)");
        }
        //canvas5.save(String.format("src/images/residualVsTrkDocaSec%d.png", i + 1));
        tabbedPane.add(canvas7, "Res. vs trkDoca (In ThBins)"); 
        //=============

        JFrame frame = new JFrame();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
        //frame.setLocationRelativeTo(null); //Centers on the default screen
        //Following line makes the canvas or frame open in the same screen where the fitCtrolUI is.
        frame.setLocationRelativeTo(fitControlFrame);//centered w.r.t fitControlUI frame
        frame.add(tabbedPane);//(canvas);
        frame.setVisible(true);
    }

    public void showTimeDistributions(JFrame fitControlFrame, int Sec, int SL, double xNormLow, double xNormHigh) {
        int iSec = Sec - 1, iSL = SL - 1;
        int nSkippedThBins = 4; //Skipping marginal 4 bins from both sides
        String Title = "";

        JTabbedPane tabbedPane = new JTabbedPane();
        //============= Residual plots
        F1D[][] func = new F1D[nSectors][nSL];
        int iPad = 0;
        
        EmbeddedCanvas canvas7 = new EmbeddedCanvas();
        canvas7.setSize(3 * 400, 3 * 400);
        canvas7.divide(3, 3);
        for (int k = nSkippedThBins; k < nThBinsVz - nSkippedThBins; k++) {            
        //for (int j = 0; j < nSL; j++) {
            Title = "Sec=" + (iSec + 1) + " SL=" + (iSL + 1) 
                    + " th(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")";
            iPad = k - nSkippedThBins; //= iSL;
            canvas7.cd(iPad);
            canvas7.getPad(iPad).getAxisZ().setLog(true);
            canvas7.draw(h1timeSlTh.get(new Coordinate(iSec, iSL, k)));
            canvas7.getPad(iPad).setTitle(Title);
            canvas7.setPadTitlesX("time (ns)");            
        }
        tabbedPane.add(canvas7, "Time (In ThBins)"); 
        
        JFrame frame = new JFrame();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
        //frame.setLocationRelativeTo(null); //Centers on the default screen
        //Following line makes the canvas or frame open in the same screen where the fitCtrolUI is.
        frame.setLocationRelativeTo(fitControlFrame);//centered w.r.t fitControlUI frame
        frame.add(tabbedPane);//(canvas);
        frame.setVisible(true);
    }    
    
    public void showBFieldDistributions(JFrame fitControlFrame, int Sec, int SL, double xNormLow, double xNormHigh) {
        int iSec = Sec - 1, iSL = SL - 1;
        int nSkippedThBins = 4; //Skipping marginal 4 bins from both sides
        String Title = "";

        JTabbedPane tabbedPane = new JTabbedPane();
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.setSize(3 * 400, 2 * 400);
        canvas.divide(3, 2);
        for (int i = 0; i < nSL; i++) {
            canvas.cd(i);
            canvas.draw(h1bFieldSL[i]);
        }
        //canv0.save("src/images/test_bFieldAllSL.png");        
        tabbedPane.add(canvas, "Time (In ThBins)"); 
        
        JFrame frame = new JFrame();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
        //frame.setLocationRelativeTo(null); //Centers on the default screen
        //Following line makes the canvas or frame open in the same screen where the fitCtrolUI is.
        frame.setLocationRelativeTo(fitControlFrame);//centered w.r.t fitControlUI frame
        frame.add(tabbedPane);//(canvas);
        frame.setVisible(true);        
    }
    
    protected void drawHistograms() {
        drawQuickTestPlots();

        //for (int i = 0; i < nSectors; i++) {
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                for (int k = 0; k < nThBinsVz; k++) {
                    htime2DisDocaProfile.put(new Coordinate(i, j, k), h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX());
                    htime2DisDocaProfile.get(new Coordinate(i, j, k)).setTitle("Sector " + i + " timeVtrkDocaS " + j + " Th" + k);
                }
            }
        }

//        try {
//            // Lets Run the Fitter
//            //runFitterUsing3DHists();
//
//            runFitter();    //This one does simultaneous fit over all theta bins
//            runFitterOld(); //This one fits in individual theta bins
//
//        } catch (IOException ex) {
//            Logger.getLogger(TimeToDistanceFitter.class.getName()).log(Level.SEVERE, null, ex);
//        }

        // Done running fitter
        // lets create lines we just fit
        createFitLines();
        /*
        createFitLinesXTB();
        MakeAndDrawXTProjectionsOfXTBhists();
         */

        DrawResidualsInTabbedPanes(); //5/14/17

        //drawSectorWiseCanvases();
        DrawInTabbedPanesOfSecSLTh();

        // lets add the canvas's to the pane and draw it.
        //addToPane();  //Temprarily disabled 
        System.out.println("====================================");
        System.out.println("Done with the fitting & drawing ...`");
        System.out.println("====================================");
    }

    protected void drawQuickTestPlots() {
        //DrawResidualsInTabbedPanes();

        int nEntries = h1bField.getEntries();//.getDataSize(1);
        System.out.println("# of entries in h1bField = " + nEntries);
        // this is temp for testHist
        EmbeddedCanvas canv = new EmbeddedCanvas();
        canv.setSize(500, 500);
        canv.cd(0);
        canv.draw(h1bField);
        canv.save("src/images/test_bField.png");

        EmbeddedCanvas canv0 = new EmbeddedCanvas();
        canv0.setSize(1500, 1000);
        canv0.divide(3, 2);
        for (int i = 0; i < nSL; i++) {
            canv0.cd(i);
            canv0.draw(h1bFieldSL[i]);
        }
        canv0.save("src/images/test_bFieldAllSL.png");

        EmbeddedCanvas canv1 = new EmbeddedCanvas();
        canv1.setSize(1200, 800);
        canv1.divide(3, 2);
        canv1.cd(0);
        canv1.draw(h1fitChisqProb);
        canv1.cd(1);
        canv1.draw(h1fitChi2Trk);
        canv1.cd(2);
        canv1.draw(h1ndfTrk);
        canv1.cd(3);
        canv1.draw(h1zVtx);
        canv1.cd(4);
        canv1.draw(h1fitChi2Trk2);//(h1fitChisqProb);
        canv1.save("src/images/test_fitChisqProb.png");

        canv1 = new EmbeddedCanvas();
        canv1.setSize(1200, 400);
        canv1.divide(3, 1);
        canv1.cd(0);
        canv1.draw(h1trkDoca4PosRes);
        canv1.cd(1);
        canv1.draw(h1trkDoca4NegRes);
        canv1.cd(2);
        canv1.draw(h2ResidualVsTrkDoca);
        canv1.save("src/images/residualVsTrkDoca.png");
    }

    protected void DrawResidualsInTabbedPanes() {

        F1D[][] func = new F1D[nSectors][nSL];

        String Title = "";
        int iPad = 0;
        JTabbedPane sectorPanes = new JTabbedPane();
        for (int i = iSecMin; i < iSecMax; i++) {
            JTabbedPane resPanes = new JTabbedPane(); //this pane will have two tabs for 1D & 2D res (using canvas & canvas2)
            EmbeddedCanvas canvas = new EmbeddedCanvas(); //Will provide tab for 1D res.
            canvas.setSize(3 * 400, 2 * 400);
            canvas.divide(3, 2);
            for (int j = 0; j < nSL; j++) {
                Title = "Sec=" + (i + 1) + " SL=" + (j + 1);
                iPad = j;
                canvas.cd(iPad);
                H1F h1 = h1timeRes.get(new Coordinate(i, j));
                func[i][j] = new F1D("func", "[amp]*gaus(x,[mean],[sigma])", -0.11, 0.11);
                func[i][j].setLineColor(2);
                func[i][j].setLineStyle(1);
                func[i][j].setLineWidth(2);
                func[i][j].setParameter(0, 1000);
                func[i][j].setParameter(1, -0.0);
                func[i][j].setParameter(2, 0.2);
                DataFitter.fit(func[i][j], h1, "E");
                func[i][j].setOptStat(1110);
                func[i][j].show(); //Prints fit parameters                    

                canvas.draw(h1);
                canvas.getPad(iPad).setTitle(Title);
                canvas.setPadTitlesX("residual (cm)");//"Residual vs trkDoca"
            }
            canvas.save(String.format("src/images/residualSec%d.png", i + 1));
            resPanes.add(canvas, "residual (cm)");

            EmbeddedCanvas canvas2 = new EmbeddedCanvas();
            canvas2.setSize(3 * 400, 2 * 400);
            canvas2.divide(3, 2);
            for (int j = 0; j < nSL; j++) {
                Title = "Sec=" + (i + 1) + " SL=" + (j + 1);
                iPad = j;
                canvas2.cd(iPad);
                canvas2.getPad(iPad).getAxisZ().setLog(true);
                canvas2.draw(h2timeResVsTrkDoca.get(new Coordinate(i, j)));
                canvas2.getPad(iPad).setTitle(Title);
                canvas2.setPadTitlesX("|trkDoca| (cm)");
                canvas2.setPadTitlesY("residual (cm)");
            }
            canvas2.save(String.format("src/images/residualVsTrkDocaSec%d.png", i + 1));
            resPanes.add(canvas2, "Residual vs trkDoca"); //Second tab in the resPanes

            sectorPanes.add(resPanes, String.format("Sector%d", i + 1));
        }

        JFrame frame = new JFrame();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
        frame.setLocationRelativeTo(null);
        frame.add(sectorPanes);
        frame.setVisible(true);
    }

    protected void DrawInTabbedPanesOfSecSLTh() {

        DrawProfilesInTabbedPanesOfSecSLTh();

        String Title = "";
        JFrame frame = new JFrame();
        JTabbedPane sectorPanes = new JTabbedPane();
        for (int i = iSecMin; i < iSecMax; i++) {
            JTabbedPane anglePanes = new JTabbedPane();
            for (int k = 0; k < nThBinsVz; k++) {
                EmbeddedCanvas canvas = new EmbeddedCanvas();
                canvas.setSize(3 * 400, 2 * 400);
                canvas.divide(3, 2);
                for (int j = 0; j < nSL; j++) {
                    canvas.cd(j);
                    Title = "Sec=" + (i + 1) + " SL=" + (j + 1)
                            + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")"
                            + " indvFitCol=" + colIndivFit;
                    canvas.draw(h2timeVtrkDoca.get(new Coordinate(i, j, k)));
                    canvas.draw(mapOfFitLinesXOld.get(new Coordinate(i, j, k)), "same");
                    //canvas.draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
                    canvas.getPad(j).setTitle(Title);
                    canvas.setPadTitlesX("trkDoca");
                    canvas.setPadTitlesY("time (ns)");
                    /*
                    PaveText stat1 = new PaveText(colSimulFit); stat1.addText("simulFit");
                    PaveText stat2 = new PaveText(colIndivFit); stat2.addText("indivFit");
                     */
                }
                anglePanes.add(canvas, "ThBin" + (k + 1));
            }
            sectorPanes.add(anglePanes, "Sector " + (i + 1));
        }
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
        frame.setLocationRelativeTo(null);
        frame.add(sectorPanes);
        frame.setVisible(true);
    }

    protected void DrawProfilesInTabbedPanesOfSecSLTh() {
        String Title = "";
        JFrame frame = new JFrame();
        JTabbedPane sectorPanes = new JTabbedPane();
        for (int i = iSecMin; i < iSecMax; i++) {
            JTabbedPane anglePanes = new JTabbedPane();
            for (int k = 0; k < nThBinsVz; k++) {
                EmbeddedCanvas canvas = new EmbeddedCanvas();
                canvas.setSize(3 * 400, 2 * 400);
                canvas.divide(3, 2);
                for (int j = 0; j < nSL; j++) {
                    canvas.cd(j);
                    Title = "Sec=" + (i + 1) + " SL=" + (j + 1)
                            + " theta=(" + thEdgeVzL[k] + "," + thEdgeVzH[k] + ")"
                            + " indvFitCol=" + colIndivFit;
                    canvas.draw(h2timeVtrkDoca.get(new Coordinate(i, j, k)).getProfileX());
                    //canvas.draw(mapOfFitLinesXOld.get(new Coordinate(i, j, k)), "same");
                    //canvas.draw(mapOfFitLinesX.get(new Coordinate(i, j, k)), "same");                    
                    canvas.getPad(j).setTitle(Title);
                    canvas.setPadTitlesX("trkDoca");
                    canvas.setPadTitlesY("time (ns)");
                }
                anglePanes.add(canvas, "ThBin" + (k + 1));
            }
            sectorPanes.add(anglePanes, "Sector " + (i + 1));
        }
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
        frame.setLocationRelativeTo(null);
        frame.add(sectorPanes);
        frame.setVisible(true);
    }

    //Cannot draw my SimpleH3D, so I want to check it by drawing its XT projections & errors separately
    public void MakeAndDrawXTProjectionsOfXTBhists() {
        int nXbins, nTbins, nBbins;
        //Now drawing these projections onto Tabbed Panes:
        String Title = "";
        JFrame frame = new JFrame();
        JTabbedPane sectorPanes = new JTabbedPane();
        for (int i = iSecMin; i < iSecMax; i++) {
            JTabbedPane anglePanes = new JTabbedPane();
            for (int k = 0; k < nThBinsVz2; k++) {
                EmbeddedCanvas canvas = new EmbeddedCanvas();
                canvas.setSize(3 * 400, 2 * 400);
                canvas.divide(3, 2);
                for (int j = 0; j < nSL; j++) {
                    canvas.cd(j);
                    Title = "Sec=" + (i + 1) + " SL=" + (j + 1)
                            + " theta=(" + thEdgeVzL2[k] + "," + thEdgeVzH2[k] + ")"
                            + " indvFitCol=" + colIndivFit;
                    canvas.draw(h3BTXmap.get(new Coordinate(i, j, k)).getXYProj());
                    canvas.getPad(j).setTitle(Title);
                    canvas.setPadTitlesX("trkDoca");
                    canvas.setPadTitlesY("time (ns)");
                }
                anglePanes.add(canvas, "ThBin" + (k + 1));
            }
            sectorPanes.add(anglePanes, "Sector " + (i + 1));
        }
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * .9), (int) (screensize.getHeight() * .9));
        frame.setLocationRelativeTo(null);
        frame.add(sectorPanes);
        frame.setVisible(true);
    }

    
    private void createFitLinesXTB() {
        double dMax;
        //for (int i = 0; i < nSectors; i++) {
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                dMax = 2 * wpdist[j];
                for (int k = 0; k < nThBinsVz2; k++) {
                    String title = "timeVsNormDoca Sec=" + (i + 1) + " SL=" + (j + 1) + " Th=" + k;
                    double maxFitValue = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getDataX(getMaximumFitValue(i, j, k));
                    //mapOfFitLinesXTB.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k, isLinearFit));
                    mapOfFitLinesXTB.put(new Coordinate(i, j, k), new DCFitDrawerForXDocaXTB(title, 0.0, 1.1 * dMax, j, k, isLinearFit));
                    mapOfFitLinesXTB.get(new Coordinate(i, j, k)).setLineColor(4);//(2);
                    mapOfFitLinesXTB.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLinesXTB.get(new Coordinate(i, j, k)).setLineStyle(4);
                    //mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j, k)));
                    //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                    mapOfFitLinesXTB.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParametersXTB.get(new Coordinate(i, j)));
                }
            }
        }

    }

    private void createFitLines() {
        double dMax;
        //for (int i = 0; i < nSectors; i++) {
        for (int i = iSecMin; i < iSecMax; i++) { //2/15/17: Looking only at the Sector2 (KPP) data (not to waste time in empty hists)
            for (int j = 0; j < nSL; j++) {
                dMax = 2 * wpdist[j];
                for (int k = 0; k < nThBinsVz; k++) {
                    String title = "timeVsNormDoca Sec=" + (i + 1) + " SL=" + (j + 1) + " Th=" + k;
                    double maxFitValue = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getDataX(getMaximumFitValue(i, j, k));
                    mapOfFitLines.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k, isLinearFit));
                    mapOfFitLines.get(new Coordinate(i, j, k)).setLineColor(4);//(2);
                    mapOfFitLines.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLines.get(new Coordinate(i, j, k)).setLineStyle(4);
                    //mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j, k)));
                    //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                    mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j)));

                    //Now creating lines using parameters from individual theta-bin fits
                    mapOfFitLinesOld.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k, isLinearFit));
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setLineColor(colIndivFit);
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setLineStyle(1);
                    mapOfFitLinesOld.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParametersOld.get(new Coordinate(i, j, k)));

                    title = "timeVsTrkDoca Sec=" + (i + 1) + " SL=" + (j + 1) + " Th=" + k;
                    mapOfFitLinesX.put(new Coordinate(i, j, k), new DCFitDrawerForXDoca(title, 0.0, 1.1 * dMax, j, k, isLinearFit));
                    mapOfFitLinesX.get(new Coordinate(i, j, k)).setLineColor(colSimulFit);//(2);
                    mapOfFitLinesX.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLinesX.get(new Coordinate(i, j, k)).setLineStyle(4);
                    //mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j, k)));
                    //Because we do the simultaneous fit over all theta bins, we have the same set of pars for all theta-bins.
                    mapOfFitLinesX.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j)));

                    //Now creating lines using parameters from individual theta-bin fits
                    mapOfFitLinesXOld.put(new Coordinate(i, j, k), new DCFitDrawerForXDoca(title, 0.0, 1.1 * dMax, j, k, isLinearFit));
                    mapOfFitLinesXOld.get(new Coordinate(i, j, k)).setLineColor(1);
                    mapOfFitLinesXOld.get(new Coordinate(i, j, k)).setLineWidth(3);
                    mapOfFitLinesXOld.get(new Coordinate(i, j, k)).setLineStyle(1);
                    mapOfFitLinesXOld.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParametersOld.get(new Coordinate(i, j, k)));
                }
            }
        }

    }

    public int getMaximumFitValue(int i, int j, int k) {
        int maxOutput = 0;
        int nX = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getXAxis().getNBins();
        int nY = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getYAxis().getNBins();
        double[][] mybuff = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getContentBuffer();
        for (int iX = 0; iX < nX; iX++) {
            for (int iY = 0; iY < nY; iY++) {
                if (mybuff[iX][iY] != 0.0) {
                    maxOutput = iX;
                }
            }
        }
        return maxOutput;

    }

    public void actionPerformed(ActionEvent e) {
        OAInstance.buttonstatus(e);
        acceptorder = OAInstance.isorderOk();
        JFrame frame = new JFrame("JOptionPane showMessageDialog example1");
        if (acceptorder) {
            JOptionPane.showMessageDialog(frame, "Click OK to start processing the time to distance fitting...");
            processData();
            drawHistograms();
            // DCTabbedPane test = new DCTabbedPane();
        } else {
            System.out.println("I am red and it is not my turn now ;( ");
        }
    }

    public void OpenFitControlUI(TimeToDistanceFitter fitter) {

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FitControlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FitControlUI(fitter).setVisible(true); //Defined in FitControlUI.java
            }
        });
    }

    public void SliceViewer(TimeToDistanceFitter fitter) {
        //Create a frame and show it through SwingUtilities
        //   It doesn't require related methods and variables to be of static type
        SwingUtilities.invokeLater(() -> {
            new SliceViewer("Slice Viewer").create(fitter);
        });
    }

    @Override
    public void run() {

        processData();

        drawQuickTestPlots();
        System.out.println("Called drawQuickTestPlots();");

        MakeAndDrawXTProjectionsOfXTBhists();
        System.out.println("Called MakeAndDrawXTProjectionsOfXTBhists();");

        //SliceViewer(this); //Now can be opened with a button in FitControlUI

        OpenFitControlUI(this);
        //drawHistograms(); //Disabled 4/3/17 - to control it by clicks in FitConrolUI.
    }

    private void saveNtuple() {
//        nTupletimeVtrkDocaVZ.write("src/files/pionTest.evio");
    }

    public static void main(String[] args) {
        String fileName;
        String fileName2;

        fileName = "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.evio";
        ArrayList<String> fileArray = new ArrayList<String>();
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_1.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_10.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_2.evio");
        fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_4.evio");

        TimeToDistanceFitter rd = new TimeToDistanceFitter(fileArray, true);

        rd.processData();
        // System.out.println(rd.getMaximumFitValue(5, 5, 5) + " output");
        rd.drawHistograms();

    }
}
