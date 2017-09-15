/**
 * 
 * @author Latif Kabir < jlab.org/~latif >
 */
		

package org.jlab.clas12.DataExplorer;

public class ExplorerLook
{
	public static void SetLook()
	{
		// Set the Nimbus look and feel
		try
		{
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
			{
				if ("Nimbus".equals(info.getName()))
				{
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (ClassNotFoundException ex)
		{
			java.util.logging.Logger.getLogger(DataExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		catch (InstantiationException ex)
		{
			java.util.logging.Logger.getLogger(DataExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		catch (IllegalAccessException ex)
		{
			java.util.logging.Logger.getLogger(DataExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		catch (javax.swing.UnsupportedLookAndFeelException ex)
		{
			java.util.logging.Logger.getLogger(DataExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
	}	
}
