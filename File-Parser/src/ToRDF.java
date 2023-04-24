import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;


/*
 *  wget  -r -p -U Mozilla www.planalto.gov.br/ccivil_03
 *  ultima coleta = 06/08/2017
 */



public class ToRDF {
	
    public static void main(String[] args) throws IOException, SQLException {

		// vars to catch call arguments
		String action  = "CONVERT"; // default action 
//      String html    = "Constituicao.html";
//		String html    = "D3382.html";
//		String html    = "D9360.html";
//		String html    = "D9662.html";
		String html    = "D39481.html";
    	
    	// revisar arquivo config.properties
		Properties properties = new Properties();
		properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));

		String baseDir = properties.getProperty("baseDir");
		String htmlDir = baseDir + properties.getProperty("htmlDir");
		String rdfDir  = baseDir + properties.getProperty("rdfDir");

		
		File[] fileList = new File(htmlDir).listFiles();
//        for (int i=0;i < fileList.length;i++) {
//            System.out.println(fileList[i]);
//        }		
//		
//        System.exit(0);
//        
		
		// catch call arguments
    	if (args != null && args.length > 0) { 
    		action = args[0];   // SEARCH, CONVERT, ITEMS, LINKS or other stuff
    	} 
    	if (args != null && args.length >1) {
    		html = args[1].substring(args[1].lastIndexOf("/") + 1, args[1].length());
    	} 

//    	System.out.println(htmlDir + html);
//    	System.out.println(fileList[0]);
//    	System.exit(0);
    	
    	/*
		 *  populate triplestore
		 *  this block reads each html file, generates RDF (N3) and populates the triplestore
		 */
    	if (action.equals("CONVERT")) {
//			RdfLaw rdfLawStore = new RdfLaw(htmlDir + html);
	        for (int i=0;i < fileList.length;i++) {
	        	System.out.println(fileList[i]);
	    		RdfLaw rdfLawStore = new RdfLaw(fileList[i].toString());
				String n3File = rdfDir + rdfLawStore.getDocName() + ".n3";
				FileUtils.writeStringToFile(new File(n3File), rdfLawStore.rdfN3(),  "UTF-8");
	        }		
    	}
    	
    	    	
		/*
		 * search terms in the triplestore
		 * this block asks for search terms, queries the triplestore and shows the result set     	
		 */    	
    	if (action.equals("SEARCH")) {
	    	String filterText;
	    	filterText = JOptionPane.showInputDialog(null, "Pesquisar por", "Lex - Pesquisar", JOptionPane.PLAIN_MESSAGE);
	    	
	    	RdfLaw rdfLaw = new RdfLaw();
	    	String result = rdfLaw.sparqlQuery(filterText);
	
	    	JFrame frame = new JFrame( );
	    	frame.setTitle("Resultados para a pesquisa: " + filterText);
	        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	        Font font = new Font("Courier", Font.PLAIN, 14);
	
	        JTextArea tx = new JTextArea();
	        tx.setFont(font);
	        tx.setLineWrap(false);
	        tx.setText(result);
	        JScrollPane sp = new JScrollPane(tx);
	        frame.getContentPane().add( sp );
	        
	        frame.pack( );
	        frame.setVisible( true );
    	}

//    	System.err.println("\nGame over!");
         
    }


   
}


