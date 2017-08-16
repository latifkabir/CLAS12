DC_Calibration
====================
DC calibration Suite.
The source files are divided into the following modules:
 - constants : Static values used in the suite
 - core : The main routines -- T0 correction estimation, Reconstruction, Calibration 
 - fit : All the fit routine and functions
 - init : All the initialization (prepare histo, graph, data container etc) stuff
 - io : The input/output services
 - test : Scripts used for testing
 - ui : User interface 

The ```main()``` function for the calibration suite is invoked from the ```Application``` class. 
