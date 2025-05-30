package presentation_layer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import Common.ITextAnalysisFacade;

public class RMIclient {
	private static final Logger logger = (Logger) LogManager.getLogger(RMIclient.class);
	public static void main(String[] args) {
		try {
			
			
			Registry registry = LocateRegistry.getRegistry("localhost",1099);
			ITextAnalysisFacade taf = (ITextAnalysisFacade)registry.lookup("Facade");
			SwingUtilities.invokeLater(() -> {
				try {
					new HomePage(taf).setVisible(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			logger.info("Client Running...");
			
		} catch (Exception e) {
			logger.warn("Exception");
			e.printStackTrace();
		}
        
    }
}
