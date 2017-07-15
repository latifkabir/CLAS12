/*  +__^_________,_________,_____,________^-.-------------------,
 *  | |||||||||   `--------'     |          |                   O
 *  `+-------------USMC----------^----------|___________________|
 *    `\_,---------,---------,--------------'
 *      / X MK X /'|       /'
 *     / X MK X /  `\    /'
 *    / X MK X /`-------'
 *   / X MK X /
 *  / X MK X /
 * (________(                @author m.c.kunkel, kpdhikari
 *  `------'
*/
package org.jlab.dc_calibration.domain;

public final class Constants {

	protected static final double rad2deg = 180.0 / Math.PI;
	protected static final double deg2rad = Math.PI / 180.0;

	protected static final double cos30 = Math.cos(30.0 / rad2deg);
	protected static final double beta = 1.0;

	// protected static final int nThBinsVz = 6; // [nThBinsVZ][2]
	// protected static final double[] thEdgeVzL = { -2.0, 8.0, 18.0, 28.0, 38.0, 48.0 };
	// protected static final double[] thEdgeVzH = { 2.0, 12.0, 22.0, 32.0, 42.0, 52.0 };

	//protected static final int nThBinsVz = 11; // [nThBinsVZ][2]
	//protected static final double[] thEdgeVzL = { -55.0, -45, -35.0, -25.0, -15.0, -5.0, 5.0, 15.0, 25.0, 35.0, 45.0 };
	//protected static final double[] thEdgeVzH = { -45.0, -35, -25.0, -15.0, -5.0, 5.0, 15.0, 25.0, 35.0, 45.0, 55.0 };

        protected static final int nThBinsVz = 17; // [nThBinsVZ][2]
	protected static final double[] thEdgeVzL = { -55, -45, -35, -25, -20, -15, -10, -6, -2, 2,  6, 10, 15, 20, 25, 35, 45};
	protected static final double[] thEdgeVzH = { -45, -35, -25, -20, -15, -10, -6,  -2,  2, 6, 10, 15, 20, 25, 35, 45, 55};
//        protected static final int nThBinsVz2 = 9;
//	protected static final double[] thEdgeVzL2 = { -36, -26, -16, -8, -2, 6, 14, 24, 34};
//	protected static final double[] thEdgeVzH2 = { -34, -24, -14, -6,  2, 8, 16, 26, 36};
        protected static final int nThBinsVz2 = 17; // [nThBinsVZ][2]
	protected static final double[] thEdgeVzL2 = { -55, -45, -35, -25, -20, -15, -10, -6, -2, 2,  6, 10, 15, 20, 25, 35, 45};
	protected static final double[] thEdgeVzH2 = { -45, -35, -25, -20, -15, -10, -6,  -2,  2, 6, 10, 15, 20, 25, 35, 45, 55};

	protected static final double[] wpdist = { 0.386160, 0.404220, 0.621906, 0.658597, 0.935140, 0.977982 };
	protected static final int nSL = 6;
	protected static final int nSectors = 6;
        protected static final int iSecMin = 1, iSecMax = 2;//iSecMin = 0, iSecMax = 6;//iSecMin = 1, iSecMax = 2;
	protected static final int nLayer = 6;
	protected static final double[] docaBins = { -0.8, -0.6, -0.4, -0.2, -0.0, 0.2, 0.4, 0.6, 0.8 };
	protected static final int nHists = 8;
	protected static final int nTh = 9;
	protected static final double[] thBins = { -60.0, -40.0, -20.0, -10.0, -1.0, 1.0, 10.0, 20.0, 40.0, 60.0 };

        protected static final int nFitPars = 10;//9;//v0, deltamn, tmax, distbeta, delta_bfield_coefficient, b1, b2, b3, b4, deltaT0;
	//protected static final String parName[] = { "v0", "deltamn", "tmax1", "tmax2", "distbeta" };
        protected static final double parSteps[] = {0.00001, 0.001, 0.01, 0.0001, 0.001, 0.001, 0.001, 0.001, 0.001, 0.001};
	protected static final String parName[] = { "v0", "deltamn", "tmax", "distbeta", "delta_bfield_coefficient", "b1", "b2", "b3", "b4", "deltaT0"};
	//protected static final String parName[] = { "v0", "deltamn", "tmax", "distbeta" };    
	//protected static final double prevFitPars[] = { 62.92e-04, 1.35, 137.67, 148.02, 0.055 };
	protected static final double prevFitPars[] = { 50e-04, 1.5, 137.67, 0.060 };

        //time could get -ve due to T0 over-correction. 
        protected static final double tMin = -50.0; //Initially I was using 0.0 for all time or t-vs-x histograms
        //protected static final double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 525.0, 550.0 };
        protected static final double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 600.0, 650.0 };
        //protected static final double timeAxisMax[] = {300.0, 300.0, 650.0, 650.0, 650.0, 650.0};
        protected static final double timeAxisMax[] = {300.0, 300.0, 650.0, 650.0, 800.0, 850.0};
        
        protected static final int nCrates = 18;//Goes from 41 to 58 (one per chamber)
        protected static final int nSlots = 20; //Total slots in each crate (only 14 used)
        protected static final int nChannels = 96;//Total channels per Slot (one channel per wire) 
        protected static final int nLayers0to35 = 36;//Layers in each sector (0th is closest to CLAS center), 6 in each SL
        protected static final int nWires = 112; //Wires in each layer of a chamber
        protected static final int nComponents = 112; //== nWires (translation table in CCDB uses components instead of wire)
        protected static final int nRegions = 3;
        protected static final int nCables = 84;
        protected static final int nCables6 = 6; //# of Cables per DCRB or STB.
        protected static final int nSlots7 = 7;  //# of STBs or occupied DCRB slots per SL.
        protected static final double [] tLow4TmaxFits = {180.0, 180.0, 180.0, 280.0, 480.0, 480.0};
        protected static final double [] tHigh = {380.0, 380.0, 680.0, 780.0, 1080.0, 1080.0};
        
//             Following constants are for temporary use (I plan to remove its use later) - Krishna
//       ===================                    ===================                ===================        
//       histTypeToUseInFitting = 0;   // for using X-profiles from h2timeVtrkDocaVZ
//                              = 1;   // for using 2D hist itself from h2timeVtrkDocaVZ
//                              = 2;   // for using XY-proj from h3BTXmap  
//                              = 3;   // for using B-field also from h3BTXmap  
        
        protected static final int histTypeToUseInFitting = 2;//1;//2; 
        //make the following controllable from the GUI (as we may have different Max for B-field depending on expt.
        protected static final int bFieldBins = 20;
        protected static double bFieldMin = 0.0, bFieldMax = 1.5;
        protected static final int binForTestPlotTemp = bFieldBins/2; //for temp purpose
        protected static final double calcDocaCut = 5.0; //1.0 //0.85
        
	private Constants() {}
}
