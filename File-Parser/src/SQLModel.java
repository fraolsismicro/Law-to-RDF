

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

 
public class SQLModel {
 
	String driver;
	String url;
	String user;
	String password;
	
	String nameSpace = "";
	
	String tripleStoreURI = "";
	String sparqlEndpoint = "";
	
	int queryResultsLimit;
	
	boolean sqlEnabled = false;  // 0 ou 1

	boolean debugging = false;  // 0 ou 1
	
	Connection conn = null;
	Statement st = null;
	ResultSet rs = null;

	
	public SQLModel() throws FileNotFoundException, IOException {
		
		Properties properties = new Properties();
		properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
	
		Logger log = Logger.getLogger("RdfLaw.log");
		
		this.driver = properties.getProperty("dbDriver");
		this.url = properties.getProperty("dbUrl");
		this.user = properties.getProperty("dbUser");
		this.password = properties.getProperty("dbPassword");

		this.nameSpace = properties.getProperty("nameSpace");
		this.tripleStoreURI = properties.getProperty("tripleStoreURI");
		this.sparqlEndpoint = properties.getProperty("sparqlEndpoint");
	
		this.queryResultsLimit = Integer.valueOf(properties.getProperty("queryResultsLimit")).intValue();

		debugging = Integer.valueOf(properties.getProperty("debug")).intValue() == 1;
	
		sqlEnabled = Integer.valueOf(properties.getProperty("sqlEnabled")).intValue() == 1;
		
		if (sqlEnabled) {
			try { 
				Class.forName(driver);
				this.debug("JDBC driver " + driver + " registered.");
			} catch (ClassNotFoundException e) { 
				System.err.println("JDBC connection error, please review your config settings.");
				e.printStackTrace();
			}
			
			try {
				this.conn = DriverManager.getConnection(url, user, password);
			} catch (SQLException e) {
				System.err.println("Connection failed. Check output console");
				e.printStackTrace();
			}
		}
	}

	public void addTriple(String sub, String pre, String obj) throws SQLException {

		debug("Subject: " + sub + "\n"
					+ "Predicate: " + pre + "\n"
					+ "Object: " + obj + "\n"
			);

		if (sqlEnabled) {
			try {
	
				this.st = conn.createStatement();
				try {
					
					String sql = " INSERT INTO triples ( sub, pre, obj ) "
							   + " SELECT '" + sub + "',"
							   + " '" + pre + "',"
							   + " '" + obj + "'"
							   + " FROM dual "
//							   + " WHERE NOT EXISTS (SELECT 1 FROM triples "
//							   + "                   WHERE  sub = '" + sub + "'"
//							   + "                   AND    pre = '" + pre + "'"
//							   + "                   AND    obj = '" + obj + "'"
//							   + " );";
							   + "; ";	
					debug("SQL: " + sql );
					this.st.executeUpdate(sql);
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
			finally {
				try {
					this.st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void deleteDoc( String doc ) throws SQLException {

		if (sqlEnabled) {
			try {
	
				this.st = conn.createStatement();
				try {
					
					String sql = " DELETE FROM contents WHERE doc = '" + doc + "';";
					sql += " DELETE FROM links WHERE doc = '" + doc + "';";
					debug("SQL: " + sql );
					this.st.executeUpdate(sql);
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
			finally {
				try {
					this.st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void addContent( Integer line, String doc, String level, String type, String parent, String name, String content) throws SQLException {

		if (sqlEnabled) {
			try {
	
				this.st = conn.createStatement();
				try {
					
					String sql = " INSERT INTO contents ( line, doc, level, type, parent, name, content ) "
							   + " VALUES ( " 
							   + "  " + line + ","
							   + " '" + doc + "',"
							   + "  " + level + ","
							   + " '" + type + "',"
							   + " '" + parent + "',"
							   + " '" + name + "',"
							   + " '" + content.replace("'", "´") + "'"
							   + " )";
					debug("SQL: " + sql );
					this.st.executeUpdate(sql);
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
			finally {
				try {
					this.st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void addLink( Integer line, String doc, String parent, String name, String link, String content) throws SQLException {

		if (sqlEnabled) {
			try {
	
				this.st = conn.createStatement();
				try {
					
					String sql = " INSERT INTO links ( line, doc, parent, name, link, content ) "
							   + " VALUES ( " 
							   + "  " + line + ","
							   + " '" + doc + "',"
							   + " '" + parent + "',"
							   + " '" + name + "',"
							   + " '" + link + "',"
							   + " '" + content.replace("'", "´") + "'"
							   + " )";
					debug("SQL: " + sql );
					this.st.executeUpdate(sql);
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
			finally {
				try {
					this.st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
		
	public void debug(String debugStr) {
		if (debugging) {
			System.out.println(new SimpleDateFormat("dd/MM HH:mm:ss").format(new Date()) + "h - debug: " +  debugStr);
		}
	}
	
	
}
	
 

