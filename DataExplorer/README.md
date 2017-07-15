CLAS12 Data Explorer
=======================

Dependencies
--------------

 - CoatJava
 - Jackson
 - Clas12lib

The jar files for each of the above packages are stored inside the ```lib/``` directory. You need to add them in the ```Build Path``` before you can compile the DataExplorer.

Instructions
--------------

1. Download/clone the ```DataExplorer package```
2. From your IDE export the package as new project.
3. Add jars for ```CoatJava, Jackson, Clas12lib``` to your build path.
4. Modify the file ```src/*/Constants.java``` to set correct path for your system.


How to use
----------

Check the presentation ![here](https://userweb.jlab.org/~latif/Hall_B/DC_Update_CalCom_meeting_July14_2017.pdf)

**Note:**
- In the run number field, the systax is ```RunNumber:FileNumber```
- In the cut expression box, you must enter a logical expression. 


**Screenshot**
![Demo](Demo.jpg)
