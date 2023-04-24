import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.jsonldjava.core.RDFDataset.IRI;

import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;

public class RdfLaw {

	private String docURI = "";
	private String docName = "";
	private String basePath = "";
	private String htmlFile = "";
	private Document doc;
  	private Elements htmlParagraphs;
    private String[] textLine;
    private String parent = "";
    
    String nameSpace = "";
    
    private Elements quotedBlocks;
    
    @SuppressWarnings("rawtypes")
	private Hashtable<Integer,ArrayList> items;
    @SuppressWarnings("rawtypes")
	private Hashtable<Integer,ArrayList> links;
    
	private String lastType = "DocLegal";
	private String noType = "Texto";
	private String type = "";
	
	private String textContent = "";
	private String index = "";
	private String indexText = "";  
	private int level = 0;
	private String name = "";
	
	private String aspas = "\"";
	
	private SQLModel sqlModel;
		
	public RdfLaw() throws FileNotFoundException,IOException {
		this.sqlModel = new SQLModel();
		this.nameSpace = sqlModel.nameSpace;
	}
	
	public RdfLaw(String htmlFile ) throws IOException,SQLException {

		this.sqlModel = new SQLModel();

		this.nameSpace = sqlModel.nameSpace;
		
		this.htmlFile = cleanedFilename(htmlFile);
		
		this.basePath = this.htmlFile.substring(0,this.htmlFile.lastIndexOf("/"));
		this.docName = this.htmlFile.substring(this.htmlFile.lastIndexOf("/")+1,this.htmlFile.length());
		
		File input = new File( htmlFile );
		
		this.doc = Jsoup.parse(input,"ISO-8859-1","file://" + this.basePath);
		
		this.htmlParagraphs = this.doc.select("p");
		//this.htmlLinks      = this.doc.select("a");

		this.quotedBlocks = this.doc.select("blockquote");
		
		//sqlModel.deleteDoc(docName);
		
		this.setItemsAndLinks();
		
	}

	@SuppressWarnings("rawtypes")
	private void setItemsAndLinks() throws SQLException {
		
		items = new Hashtable<Integer,ArrayList>();
		links = new Hashtable<Integer,ArrayList>();
		
		//ROOT
		parent = "";
        ArrayList<String> item0 = new ArrayList<String>();
        item0.add(padInt5(0));    // line
        item0.add(docName);       // doc
        item0.add(padInt3(1));    // level
        item0.add("DocLegal");    // type
        item0.add(parent);        // parent
        item0.add(docName);       // name
        item0.add("");            // content
        this.items.put(1,item0);
        //sqlModel.addContent( 0,docName,padInt3(0), "DocLegal",parent,docName,docURI );
        
        int i=1; // 1 is reserver to root docURI
        int j=0;
        
        for (Element pe : this.htmlParagraphs) {
            
        	boolean validParag = true;
        	
        	for (Element anElement: pe.getAllElements()) {
            	if (anElement.tagName().equals("span")) {
            		validParag = (!anElement.toString().contains("line-through"));
            	}
        	}
        	
        	if (!validParag) {
        		continue;
        	}
        		
        	textLine = parseString(pe.text().trim(),lastType,noType);
        	        	
            indexText = textLine[2];                 // the text that indentifies a type,e.g. 1�,I,�nico,a),etc. 
            type = textLine[0]; 					 // type: Artigo,Paragrafo,Inciso,etc.  
            if (!indexText.trim().equals("")) {
            	lastType = type.trim();
            } else {
            	//
            }
        	//tree.addNode(index,father);
            textContent = textLine[1].trim().replace("<","").replace(">","");				 // text: the text contents of certain type
            level = new Integer(textLine[3]).intValue();
            name = (textLine[4] == null ? type + index : textLine[4]);
                
            if (type.equals("Artigo") && name.endsWith("o")) { name = name.substring(0,name.length()-1); }
            if (type.equals("Paragrafo") && name.endsWith("o") && name.indexOf("Unico") < 0) { name = name.substring(0,name.length()-1); }
            

            //System.err.println("textContent => " + textContent);
           
            // Simplificando a articulacao complexa
            // isto eh,soh processar artigos e descendentes
            if ((type.equals("Artigo")
            		|| type.equals("Caput")
            		|| type.equals("Paragrafo")
            		|| type.equals("Inciso")
            		|| type.equals("Alinea")
            		|| type.equals("Item")
            		|| type.equals("Pena")
            		) && (!pe.toString().contains("<strike>") || docName.equals("L7646"))) {
            	
                // get parent
                if (level == 1) { 
                	parent = docName;
                } else {
                	if (level == 05) {
                		if (!nameExists(name)) {
                			parent = docName;
                		}  
                	} else {
    	              for (int k=items.size(); k>0; k--) {
    	            	  ArrayList father1 = items.get(k);
    	            	  //System.err.println("k = " + k + ";  i = " + i + ": " + name + " => " + level + " ==> " + parent + " ===> " + father1.get(2));
    	            	  if ( (new Integer(father1.get(2).toString())).intValue() < level) { 
    	            		  parent = father1.get(4).toString() + "/" + father1.get(5).toString().trim(); k = 0;
    	            	  }
    	              }
                	}
                }
                
                // tentativa tratar casos de artigos duplicados (ex: D1808 Art 25,30 - um caducado),etc.
                // a expectativa � que ao declarar dois recursos com a mesma URI no dataset RDF o ultimo sobrescreva o primeiro.
      		    parent = parent.replace("/"+name,"").replace("'","");
                name = name.replace("'","").replace("\"","").replace("%","");
                textContent = textContent.replace("&" ,"&amp;");
            	
            	
                
	            if (type.equals("Pena") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"Artigo")) {
            		type = "ArtigoPena";
            		level = 10;	
            		name = "Artigo" + name;
	            }
	            
	            if (type.equals("Pena") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"Paragrafo")) {
            		type = "ParagrafoPena";
            		level = 15;	
            		name = "Paragrafo" + name;
            	}
	            
	            if (type.equals("Inciso") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"Paragrafo")) {
            		type = "ParagrafoInciso";
            		level = 15;	
            		name = "Paragrafo" + name;
            	}

	            if (type.equals("Alinea") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"Inciso")) {
            		type = "IncisoAlinea";
            		level = 15;	
            		name = "Inciso" + name;
            	}
	            
            	if (type.equals("Pena") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"Inciso")) {
            		type = "IncisoPena";
            		level = 15;	
            		name = "Inciso" + name;
            	}
            	
            	if (type.equals("Pena") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"Alinea")) {
            		type = "AlineaPena";
            		level = 20;	
            		name = "Alinea" + name;
            	}

            	if (type.equals("Pena") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"ParagrafoInciso")) {
            		type = "ParagrafoIncisoPena";
            		level = 20;	
            		name = "ParagrafoInciso" + name;
            	}

            	if (type.equals("Pena") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"IncisoAlinea")) {
            		type = "IncisoAlineaPena";
            		level = 20;	
            		name = "IncisoAlinea" + name;
            	}
            	
            	if (type.equals("Item") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"IncisoAlinea")) {
            		type = "IncisoAlineaItem";
            		level = 20;	
            		name = "IncisoAlinea" + name;
            	}

            	if (type.equals("Pena") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"Item")) {
            		type = "ItemPena";
            		level = 25;	
            		name = "ItemPena" + name;
            	}

            	if (type.equals("Pena") && StringUtils.startsWith(parent.split("/")[parent.split("/").length-1],"IncisoAlineaItem")) {
            		type = "IncisoAlineaItemPena";
            		level = 25;	
            		name = "IncisoAlineaItemPena" + name;
            	}
            	
            	if(StringUtils.startsWith(parent,"/")) {
            		parent = parent.substring(1);
            	}
            	
            	// add one item
	            if (!textContent.trim().equals("")) {
	            	index = padInt5(i++);
		            ArrayList<String> item = new ArrayList<String>();
		            item.add(index);           // line
		            item.add(docName);         // doc
		            item.add(padInt3(level));  // level
		            item.add(type);            // type
		            item.add(parent);          // parent
		            item.add(name);            // name
		            item.add(textContent);     // content
		            items.put(i,item);
		            //sqlModel.addContent( new Integer(index).intValue(),docName,padInt3(level),type, parent,name,textContent );
	            }
            };
            
            // add links
            Elements pLinks = pe.select("a[href]");
            for (Element pLink : pLinks) {
            	sqlModel.debug("Link: " + pLink);
            	try {
            		String linkURI = getLinkURI(pLink);
            		if (!linkURI.equals("")) {
	            		String[] tokens1 = StringUtils.split(linkURI,"#");
	            		String[] tokens2 = tokens1.length>1?StringUtils.split(tokens1[1],"�"):StringUtils.split("","�"); 
	            		String toDoc = tokens1.length>0?tokens1[0]:""; 
	            		String toArtigo = tokens2.length>0?tokens2[0].replace("art","Artigo"):"";
	            		String toParagrafo = tokens2.length>1?tokens2[1]:"";
	            	    if (!toParagrafo.equals("")) { toParagrafo = "Paragrafo" + toParagrafo; }
	            	    
	            		// limpar incisos e etc do fim do artigo
	            		for (int m=toArtigo.length(); m>0; m--) {
	            			if ("0123456789".indexOf(toArtigo.substring(m-1,m)) < 0) {
	            				toArtigo = toArtigo.substring(0,m-1);
	            			} else {
	            				break;
	            			}
	            		}
	            		//toArtigo = StringUtils.''
	            		
	            		//System.err.println(parent + " => " + linkURI);
	            		
	            		if (parent.equals("")) { parent = docName; }
	            		
	            		if (!parent.equals("")) {
		            		ArrayList<String> link = new ArrayList<String>();
		            		link.add(parent);              // parent
		            		link.add(name);                // name
		            		link.add(linkURI);             // link
		            		link.add(toDoc);               // toDoc
		            		link.add(toArtigo);            // toArtigo
		            		link.add(toParagrafo);         // toParagrafo
		            		link.add(pLink.text().trim().replace("<","").replace(">",""));        // text
		            		links.put(j++,link);           
		            		//sqlModel.addLink( i,docName,parent,name,linkURI,pLink.text());
	            		}
            		}
            	} catch (Exception ex) { 
            		if (sqlModel.debugging) { ex.printStackTrace(); }
            	}
            }
            
            if (textContent.startsWith("Este texto n�o substitui o publicado no DOU")) { break; }
        }
        
	}

	public String getDocURI() {
		return this.docURI;
	}
	
	public String getDocName() {
		return docName;
	}

	@SuppressWarnings("rawtypes")
	public Hashtable<Integer,ArrayList> getItems() {
		return items;
	}

	@SuppressWarnings("rawtypes")
	public Hashtable<Integer,ArrayList> getLinks() {
		return links;
	}

	public void storeN3(String n3File) throws SQLException {

		DatasetAccessor accessor;
		accessor = DatasetAccessorFactory.createHTTP(sqlModel.tripleStoreURI);
		
		FileManager fm = FileManager.get();
		fm.addLocatorClassLoader(RdfLaw.class.getClassLoader());
		Model m = fm.loadModel(n3File,null,"TURTLE");
		accessor.add(m); 
		
	}
	
   public String rdfN3() throws SQLException {

	   // vide https://www.w3.org/RDF/Validator/
	   // vide http://rdfvalidator.mybluemix.net/
	   // vide scalar.usc.edu/works/guide/rdf-syntax 

	   // RDF Validator and Converter => http://rdfvalidator.mybluemix.net/
	   	   
	   String n3 = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
			   	 + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			   	 + "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n"
			   	 + "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n"
			   	 + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
			   	 + "@prefix lex: <" + this.nameSpace + "> .\n"
			   	 + "@prefix : <" + this.nameSpace + "> .\n\n";

	 	   
	   String thisType        = "";
	   String thisParent      = "";
	   String thisName        = "";
	   String thisText        = "";
	   String thisToDoc       = "";
	   String thisToArtigo    = "";
	   String thisToParagrafo = "";

	   n3 += "lex:" + this.docName + "    lex:ehUm    lex:DocLegal .\n";
	   n3 += "lex:" + this.docName + "    lex:temTextoIntegral    lex:HTMLs\\/" + this.docName.replace("/", "\\/") + ".htm .\n";
	   
	   String subject = "";
	   String predicate = "";
	   String object = "";

	   // items - a partir de 2 pois 1 � o root = DocLegal
       for (int k=2; k<=items.size(); k++) {
    	       	   
    	   thisType       = items.get(k).get(3).toString();
    	   thisParent     = items.get(k).get(4).toString().trim();
    	   thisName       = items.get(k).get(5).toString().trim();
    	   thisText       = items.get(k).get(6).toString().trim().replace("'","�");
    	   
    	   predicate = "lex:tem" + thisType;
    	   
    	   n3 += "lex:" + cleanedURI(thisParent + "/" + thisName).replace("/","\\/") + "    lex:ehUm    " + "lex:" + thisType + " .\n";
    	   if (thisParent != "") { n3 += "lex:" + cleanedURI(thisParent).replace("/","\\/") + "    " + predicate + "    " + "lex:" + cleanedURI(thisParent + "/" + thisName).replace("/","\\/") + " .\n"; }
    	   if (thisText   != "") { n3 += "lex:" + cleanedURI(thisParent  + "/" + thisName).replace("/","\\/") + "    lex:temConteudo    " + aspas + thisText.replace("\\","").replace("\"","'") + aspas + " .\n"; }
    	      	   
       }
       
       //System.err.println(links);
       for (int k=0; k<links.size(); k++) {
	       
    	   thisParent      = links.get(k).get(0).toString();
    	   thisName        = links.get(k).get(1).toString();
    	   thisToDoc       = links.get(k).get(3).toString();
	       thisToArtigo    = links.get(k).get(4).toString();
	       thisToParagrafo = links.get(k).get(5).toString();
	       thisText        = links.get(k).get(6).toString();
	  	
	       //definir o predicado do link
	       predicate = "remetePara";
	       if (StringUtils.contains(thisText.toLowerCase(),"regulament" )) { predicate = "regulamentadoPor"; } 
	       if (StringUtils.contains(thisText.toLowerCase(),"revogad"    )) { predicate = "revogadoPor"; }
	       
	       //definir sujeito e objeto do link
           subject = thisParent + "/" + thisName;
           object  = thisToDoc; 
	       if (thisToArtigo    != "") { object += "/" + thisToArtigo; }
	       if (thisToParagrafo != "") { object += "/" + thisToParagrafo; }

	       // escrever a tripla do link e as inversas,se for o caso.
	       
	       n3 += "lex:" + cleanedURI(subject).replace("/","\\/") + "    lex:" + predicate + "    " + "lex:" + cleanedURI(object).replace("/","\\/") + " .\n";
	       
	       //System.err.println(thisText);
	       
	       if (StringUtils.contains(predicate,"regulamentadoPor")) { n3 += "lex:" + cleanedURI(object).replace("/","\\/") +  "    lex:regulamenta    " + "lex:" + cleanedURI(subject).replace("/","\\/") + " .\n"; }
	       if (StringUtils.contains(predicate,"revogadoPor"     )) { n3 += "lex:" + cleanedURI(object).replace("/","\\/") +  "    lex:revogou    "     + "lex:" + cleanedURI(subject).replace("/","\\/") + " .\n"; }
       }
       
       return n3;
       
   }

   public void createTriplesIntoSqlDB() throws SQLException {

	   // vide https://www.w3.org/RDF/Validator/
	   // vide http://rdfvalidator.mybluemix.net/
	   // vide scalar.usc.edu/works/guide/rdf-syntax 

	   // RDF Validator and Converter => http://rdfvalidator.mybluemix.net/
	   
	   String thisType        = "";
	   String thisParent      = "";
	   String thisName        = "";
	   String thisText        = "";
	   String thisToDoc       = "";
	   String thisToArtigo    = "";
	   String thisToParagrafo = "";

	   
	   String subject = "";
	   String predicate = "";
	   String object = "";

	   // items - a partir de 2 pois 1 � o root = DocLegal
       for (int k=2; k<=items.size(); k++) {
    	   
    	   thisType       = items.get(k).get(3).toString();
    	   thisParent     = items.get(k).get(4).toString().trim();
    	   thisName       = items.get(k).get(5).toString().trim();
    	   thisText       = items.get(k).get(6).toString().trim()
    			   .replace("'","�")
    			   ;
    	   
    	   predicate = "tem" + thisType;
    	   
    	   sqlModel.addTriple(thisParent + "/" + thisName,"ehUm",thisType);
    	   if (thisParent != "") { sqlModel.addTriple(thisParent,predicate,thisParent + "/" + thisName); }
    	   if (thisText   != "") { sqlModel.addTriple(thisParent + "/" + thisName,"tem_texto",thisText); }
    	      	   
       }
       
       //System.err.println(links);
       for (int k=0; k<links.size(); k++) {
	       
    	   thisParent      = links.get(k).get(0).toString();
    	   thisName        = links.get(k).get(1).toString();
    	   thisToDoc       = links.get(k).get(3).toString();
	       thisToArtigo    = links.get(k).get(4).toString();
	       thisToParagrafo = links.get(k).get(5).toString();
	       thisText        = links.get(k).get(6).toString();
	  	
	       //definir o predicado do link
	       predicate = "remetePara";
	       if (StringUtils.contains(thisText.toLowerCase(),"regulament" )) { predicate = "regulamentadoPor"; } 
	       if (StringUtils.contains(thisText.toLowerCase(),"revogad"    )) { predicate = "revogadoPor"; }
	       
	       //definir sujeito e objeto do link
           subject = thisParent + "/" + thisName;
           object  = thisToDoc; 
	       if (thisToArtigo    != "") { object += "/" + thisToArtigo; }
	       if (thisToParagrafo != "") { object += "/" + thisToParagrafo; }

	       // escrever a tripla do link e as inversas,se for o caso.
	       sqlModel.addTriple(subject,predicate,object);
	       
	       if (StringUtils.contains(predicate,"regulamentadoPor")) { sqlModel.addTriple(object,"regulamenta",subject);  }
	       if (StringUtils.contains(predicate,"revogadoPor"     )) { sqlModel.addTriple(object,"revogou",    subject); }
       }
   }

   public Elements getParagraphs() {
	   return this.htmlParagraphs;
   }

   public Elements getQuotedBlocks() {
	   return this.quotedBlocks;
   }
   
   public String sparqlQuery(String filterText) throws MalformedURLException {

	   String result = "Resultados para a pesquisa: " + filterText + "\n\n"
			   + "Sujeito\tPredicado\tObjeto\n\n";
	   
	   int kount = 0;
	   
	   ResultSet results = this.getQueryResults(filterText);
	   
	   while (results.hasNext()) {
		   kount++;
           QuerySolution s = results.nextSolution();
           
           result += s.get("s").toString().replace(this.nameSpace,"") 
        		   + "\t" + s.get("p").toString().replace(this.nameSpace,"") 
        		   + "\t" + s.get("o").toString().replace(this.nameSpace,"")
        		   + "\n";
           
           try {  // agregate neighbours 1d
        	   ResultSet neighbours = this.getNeighbours(s.get("s").toString().replace(this.nameSpace,":").replace("/","\\/"));
        	   while (neighbours.hasNext()) {
        		   kount++;
        		   QuerySolution n = neighbours.nextSolution();
                   result += n.get("s").toString().replace(this.nameSpace,"").replace(":","") 
                		   + "\t" + n.get("p").toString().replace(this.nameSpace,"").replace(":","") 
                		   + "\t" + n.get("o").toString().replace(this.nameSpace,"").replace(":","")
                		   + "\n";
                   try {
                	   if (!n.get("p").toString().replace(this.nameSpace,"").replace(":","").equals("temTextoIntegral") 
                			   && !n.get("p").toString().replace(this.nameSpace,"").replace(":","").equals("temConteudo")) { 
	                	   ResultSet nextNeighbours = this.getNextNeighbours(n.get("o").toString().replace(this.nameSpace,":").replace("/","\\/"));
	                	   while (nextNeighbours.hasNext()) {
	                		   kount++;
	                		   QuerySolution n1 = nextNeighbours.nextSolution();
	                           result += n1.get("s").toString().replace(this.nameSpace,"").replace(":","") 
	                        		   + "\t" + n1.get("p").toString().replace(this.nameSpace,"").replace(":","") 
	                        		   + "\t" + n1.get("o").toString().replace(this.nameSpace,"").replace(":","")
	                        		   + "\n";     
	                	   }
                	   }
                   } finally {
                	   
                   }
        	   }
           } finally {
        	   
           }
	   }
	   result += "\n" + String.valueOf(kount) + " dispositivos encontrados.\n\n\n";
	   
	   return result;
   }

   private ResultSet getQueryResults(String filterText) {

	   ResultSet results;
	   
	   // licen�a programa computador
	   String[] filterArguments = filterText.split(" ");
	   
	   String query = " SELECT ?s ?p ?o \n"
			   + " WHERE { \n"
			   + "           ?s lex:temConteudo ?o . \n"
			   + "           BIND(str('temConteudo') AS ?p) . \n";
	   
	   		   // filter arguments
			   for (String search : filterArguments) {
				   query += "           FILTER ( CONTAINS(?o,'" + search + "') ) . \n";
			   }

			   query +=  " } \n"
			   + " ORDER BY ?s ?p ?o \n"
			   + " LIMIT " + String.valueOf(sqlModel.queryResultsLimit) + "\n";
	   
	   ParameterizedSparqlString pss = new ParameterizedSparqlString();
	   
	   pss.setBaseUri(sqlModel.sparqlEndpoint);
	   
	   pss.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
	   pss.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"); 
	   pss.setNsPrefix("xml", "http://www.w3.org/XML/1998/namespace");
	   pss.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
	   pss.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
	   pss.setNsPrefix("lex", "http://localhost:8080/fuseki/");  
	   pss.setNsPrefix("",    "http://localhost:8080/fuseki/");  
	   
	   pss.setCommandText(query);
	   
	   System.out.println("\n#getQueryResults for: " + filterText + ":\n#-------------------------------\n" + pss.toString() + "\n");
	   
	   Query  q = QueryFactory.create(pss.toString());
	   QueryExecution qexec = QueryExecutionFactory.sparqlService(sqlModel.sparqlEndpoint,q);

	   try {
		   results = qexec.execSelect();
	   } finally {
		   //qexec.close();
	   }
	   return results;
   }
   
   private ResultSet getNeighbours(String resource) throws MalformedURLException {
	   IRI resourceIri = new IRI(resource);
	   
	   resource = resource.replace(this.nameSpace,"");
	   System.out.println("resource => " + resource);
	   ResultSet results;
	   String query = " SELECT ?s ?p ?o \n"
			   + "      { \n"
			   + "        { \n"
			   + "            " + resourceIri.getValue() + " ?p ?o . \n"
			   + "             BIND('" + resource.replace("\\", "")  + "' AS ?s) \n"
			   + "             FILTER(CONTAINS(STR(?p),'temTextoIntegral') \n"
			   + "                 || CONTAINS(STR(?p),'remetePara') \n"
			   + "                 || CONTAINS(STR(?p),'revogou') \n"
			   + "                 || CONTAINS(STR(?p),'revogadoPor') \n"
			   + "                 || CONTAINS(STR(?p),'regulamenta') \n"
			   + "                 || CONTAINS(STR(?p),'regulamentadoPor') \n"
			   + "             ) . \n"
			   + "        } \n"
			   + "      UNION \n"
			   + "        { \n"
			   + "            ?s ?p " + resourceIri.getValue() + "\n"
			   + "             BIND('" + resource.replace("\\", "") + "' AS ?o) \n"
			   + "             FILTER(CONTAINS(STR(?p),'temConteudo') \n" 
			   + "                 || CONTAINS(STR(?p),'temTextoIntegral') \n"
			   + "                 || CONTAINS(STR(?p),'remetePara') \n"
			   + "                 || CONTAINS(STR(?p),'revogou') \n"
			   + "                 || CONTAINS(STR(?p),'revogadoPor') \n"
			   + "                 || CONTAINS(STR(?p),'regulamenta') \n"
			   + "                 || CONTAINS(STR(?p),'regulamentadoPor') \n"
			   + "             ) . \n"
			   + "        } \n"
			   + "      } \n"
			   + "      ORDER BY ?s ?p ?o \n"
			   + " LIMIT " + String.valueOf(sqlModel.queryResultsLimit) + "\n";
	   
	   ParameterizedSparqlString pss = new ParameterizedSparqlString();
	   
	   pss.setBaseUri(sqlModel.sparqlEndpoint);
	   
	   pss.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
	   pss.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"); 
	   pss.setNsPrefix("xml", "http://www.w3.org/XML/1998/namespace");
	   pss.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
	   pss.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
	   pss.setNsPrefix("lex", "http://localhost:8080/fuseki/");  
	   pss.setNsPrefix("",    "http://localhost:8080/fuseki/");  
	   
	   pss.setCommandText(query);
	   
	   System.out.println("\n#getNeighbours for: " + resource + "\n#-------------------------------\n" + pss.toString() + "\n");
	   
	   Query  q = QueryFactory.create(pss.toString());
	   QueryExecution qexec = QueryExecutionFactory.sparqlService(sqlModel.sparqlEndpoint,q);

	   try {
		   results = qexec.execSelect();
	   } finally {
		   //qexec.close();
	   }
	   return results;
   }
   
   private ResultSet getNextNeighbours(String resource) {
	   ResultSet results;
	   String query = " SELECT ?s ?p ?o \n"
			   + "      { \n"
			   + "        { \n"
			   + "            " + resource + " lex:temConteudo ?o . \n"
			   + "             BIND('" + resource.replace("\\", "")  + "' AS ?s) \n"
			   + "             BIND(str('temConteudo') AS ?p) . \n"
			   + "        } \n"
			   + "      } \n"
			   + "      ORDER BY ?s ?p ?o \n"
			   + " LIMIT " + String.valueOf(sqlModel.queryResultsLimit) + "\n";
	   
	   ParameterizedSparqlString pss = new ParameterizedSparqlString();
	   
	   pss.setBaseUri(sqlModel.sparqlEndpoint);
	   
	   pss.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
	   pss.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"); 
	   pss.setNsPrefix("xml", "http://www.w3.org/XML/1998/namespace");
	   pss.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
	   pss.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
	   pss.setNsPrefix("lex", "http://localhost:8080/fuseki/");  
	   pss.setNsPrefix("",    "http://localhost:8080/fuseki/");  
	   
	   pss.setCommandText(query);
	   
	   System.out.println("\n#getNextNeighbours for: " + resource + "\n#-------------------------------\n"  + pss.toString() + "\n");
	   
	   Query  q = QueryFactory.create(pss.toString());
	   QueryExecution qexec = QueryExecutionFactory.sparqlService(sqlModel.sparqlEndpoint,q);

	   try {
		   results = qexec.execSelect();
	   } finally {
		   //qexec.close();
	   }
	   return results;
   }
    
   private String getLinkURI(Element link) {
		String res = link.attr("abs:href"); 
		if (!StringUtils.contains(res,"OpenDocument") 
				&& !StringUtils.contains(res,".pdf") 
				&&  !StringUtils.contains(res,".asp")
				&&  StringUtils.contains(res,".htm") 
			) { 
			res = cleanedFilename(res);
			res = res.substring(res.lastIndexOf("/") + 1,res.length()).replace("To-RDF",this.docName);
		} else {
			res = "";
		}		
		return res;
	}

    private String[] parseString(String s,String lastType,String noType) {
    	
    	String[] result = new String[5];

    	//System.err.println( "str s => " + s);
    	    	
    	result[0] = noType;
    	result[1] = s.replaceAll(Character.toString((char) 150)," ");
    	result[1] = result[1].replaceAll(Character.toString((char) 160)," ");
    	result[1] = result[1].replace("Art .","Art.");
    	result[3] = "777";
    	    	
    	try {
    		result[2] = " ";
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[2] = result[1].trim().split(" ")[1].trim();
    		};
    	} catch (Exception ex) {
    		result[2] = " ";
    	}
    	
    	// start will serve as base to define which type the line is
    	String start = result[1].trim().toLowerCase().split(" ")[0].trim();
    	start = start.replace("-a","");
        start = start.replace("-b","");
        start = start.replace("-c","");
        start = start.replace("-","");
        start = start.replace(".","");
        start = start.replaceAll("� ","");
            	
    	// if is the (first) end of main text
    	result[1] = result[1].replaceAll("(?i)(Este texto n�o substitui)(.+?)(o publicado)(.+?)(no DOU)","Este texto n�o substitui o publicado no DOU");

    	// Fig. 4.15 - Ontologia Sistematica Externa Legal
    	
    	if (result[0].equals(noType) && start.equals("ep�grafe")) {
    		result[0] = "Epigrafe"; result[3] = "10"; result[4] = result[0];
    	}
    	if (result[0].equals(noType) && start.equals("ementa")) {
    		result[0] = "Ementa"; result[3] = "10"; result[4] = result[0];
    	}
    	if (result[0].equals(noType) && start.equals("pre�mbulo")) {
    		result[0] = "Preambulo"; result[3] = "10"; result[4] = result[0];
    	}
    	if (result[0].equals(noType) && (start.equals("parte") || result[1].toLowerCase().startsWith("p a r t e") )) {
    		result[0] = "Parte"; result[3] = "5"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	if (result[0].equals(noType) && start.equals("livro")) {
    		result[0] = "Livro"; result[3] = "10"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	if (result[0].equals(noType) && start.equals("t�tulo")) {
    		result[0] = "Titulo"; result[3] = "15"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	if (result[0].equals(noType) && start.equals("subt�tulo")) {
    		result[0] = "Subtitulo"; result[3] = "10"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	if (result[0].equals(noType) && start.equals("cap�tulo")) {
    		result[0] = "Capitulo"; result[3] = "15"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	if (result[0].equals(noType) && start.equals("subcap�tulo")) {
    		result[0] = "Subcapitulo"; result[3] = "20"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	if (result[0].equals(noType) && start.equals("se��o")) {
    		result[0] = "Secao"; result[3] = "15"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	if (result[0].equals(noType) && start.equals("subse��o")) {
    		result[0] = "Subsecao"; result[3] = "20"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	if (result[0].equals(noType) && start.equals("art")) {
        	//System.err.println( "start (art) => " + start);
    		result[0] = "Artigo"; result[3] = "5"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	if (result[0].equals(noType) && start.equals("caput")) {
    		result[0] = "Caput"; result[3] = "10"; result[4] = result[0];
    	}
    	
    	if (result[0].equals(noType) && (start.startsWith("�") || start.equals("par�grafo") || start.endsWith("�") )) {
    		result[0] = "Paragrafo"; result[3] = "10"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}
    	
    	// check is we're leading with Inciso
    	if (result[0].equals(noType)) { 
	    	try {
	    		if (start.length() > 0) {
//		    		RomanNumeral roman = new RomanNumeral(start);
//		    		if (roman.toInt() > 0) {
	    			if (isRoman(start)) {
		    			result[0] = "Inciso"; result[3] = "15";
		    			result[2] = result[1].trim().split(" ")[0].trim();
		        		result[4] = result[0];
		        		if (StringUtils.contains(result[1].trim()," ")) {
		        			result[4] += encodedName(result[1].trim().split(" ")[0]);
		        		}
		    		}
	    		}
	    	} catch (Exception ex) {
	    		if (sqlModel.debugging) { ex.printStackTrace(); }
	    	}
    	}
    	
    	// check we're leading with Alinea
    	if (result[0].equals(noType)) {
	    	for (int i2=97; i2 <= 122; i2++) {
	    		//System.err.println((char)(i2)+")");
	    		if (start.startsWith((char)(i2)+")")) {
	    			result[0] = "Alinea"; result[3] = "20"; result[4] = result[0]; 
	    			result[2] = result[1].trim().split(" ")[0].trim();
	        		result[4] = result[0];
	        		if (StringUtils.contains(result[1].trim()," ")) {
	        			result[4] += encodedName(result[1].trim().split(" ")[1]);
	        		}
	    			i2 = 999; // bummmm!
	    		}
	    	}
    	}

    	// check is we're leading with Item
       if (result[0].equals(noType) && (lastType.equals("Alinea") || lastType.equals("Item"))) {
	   	    if ("0123456789".contains(start)) {
    			result[0] = "Item"; result[3] = "25"; result[4] = result[0]; 
    			result[2] = result[1].trim().split(" ")[0].trim();
        		result[4] = result[0];
        		if (StringUtils.contains(result[1].trim()," ")) {
        			result[4] += encodedName(result[1].trim().split(" ")[1]);
        		}
    		}
	   }

       /* extensoes inicio */
	   	if (result[0].equals(noType) && start.equals("pena")) {
			result[0] = "Pena"; result[3] = "99"; result[4] = result[0];
		}
	
	   	if (result[0].equals(noType) && start.equals("altera��o")) {
			result[0] = "Pena"; result[3] = "99"; result[4] = result[0];
		}
	   
	   	if (result[0].equals(noType) && start.equals("agrupador")) {
			result[0] = "Pena"; result[3] = "99"; result[4] = result[0];
		}
	   /* extensoes fim    */
	   	
	   	if (result[0].equals(noType) && (start.equals("ParteFinal") || start.startsWith("bras�lia,") || start.startsWith("rio de janeiro,"))) {
    		result[0] = "ParteFinal"; result[3] = "5"; result[4] = result[0];
    	}
    	
    	if (result[0].equals(noType) && start.equals("anexo")) {
    		result[0] = "Anexo"; result[3] = "10"; 
    		result[4] = result[0];
    		if (StringUtils.contains(result[1].trim()," ")) {
    			result[4] += encodedName(result[1].trim().split(" ")[1]);
    		}
    	}  	

    	if (result[0].equals(noType) && lastType.equals("DocLegal")) {
    		result[0] = "ParteInicial"; result[3] = "5"; result[4] = result[0];
    	}
    	if (result[0].equals(noType) && lastType.equals("ParteFinal")) {
    		result[0] = "Fecho"; result[3] = "5"; result[4] = result[0];
    	}
    	
//    	if (result[0].equals("art")) {
//    		System.err.println(result[0] + " => " + start);
//    	}
    	
    	return result;
    }

	private String cleanedFilename(String filename) {
		return filename.replace(".html","")
				.replace(".htm","")
				.replace("impresao","")
				.replace("impressao","")
				.replace("htmimpressao","")
				.replace("consolidado","")
				.replace("cons.","")
				.replace("htmcompilado","")
				.replace("htmcompilada","")
				.replace("htmCompilado","")
				.replace("htmCompilada","")
				.replace("compilado","")
				.replace("compilada","")
				.replace("Compilado","")
				.replace("Compilada","")
				.replace("orig","")
				.replace(" ","")
				;
	}

	private String cleanedURI(String uri) {
		return uri.replace(",","")
					.replace("\"","")
					.replace("%","")
					.replace(";","")
					.replace("(","")
					.replace(")","")
					.replace("$","S")
					.replace(".","")
					.replace("=","")
					.replace("�","")
					.replace("|","")
					.replace("[","")
					.replace("]","")
					.replace("<","")
					.replace(">","")
					.replace("*","")
					.replace("!","")
					.replace("L8212consart95�2","L8212consol/Artigo95/Paragrafo2")
					.replace("L10836Sart2�12i","L10836/Artigo2/Paragrafo12/IncisoI")
					.replace("L10836Sart2�12ii","L10836/Artigo2/Paragrafo12/IncisoII") 
					.replace("L10836Sart2�12iii","L10836/Artigo2/Paragrafo12/IncisoIII")
					.replace("L10833art58f�3","L10833/Artigo58/Paragrafo3")
					.replace("L7150art1�2","L7150/Artigo1/Paragrafo2")
					.replace("Del0227art90�1","Del0227/Artigo90/Paragrafo1")
					.replace("L9082art49�4","L9082/Artigo49/Paragrafo4")
					.replace("Del9797art672�1","Del9797/Artigo672/Paragrafo1")
					.replace("Lcp101art9�5","Lcp101/Artigo9/Paragrafo5")
					.replace("Lcp101art4�3","Lcp101/Artigo4/Paragrafo3")
					.replace("Lcp101art42","Lcp101/Artigo42")
					.replace("L4771�6","L4771")
					.replace("L9615consolart5�3","L9615consol/Artigo5/Paragrafo3")
					.replace("L9615consolart5","L9615consol/Artigo5")
					.replace("mailto:L8069@art243","L8069/Artigo243")
					.replace("mailto:L6830","L6830")
					.replace("mailto:L8167","L8167")
					.replace("�L9008/Artigo1/Paragrafo1","L9008/Artigo1/Paragrafo1")
					.replace("L8383art21�4","L8383/Artigo21/Paragrafo4")
					.replace("L4345@art10","L4345/Artigo10")
				;
	}
	   
   private String padInt3(int x) {
       return String.format("%03d",x);  
   }

   private String padInt5(int x) {
       return String.format("%05d",x);  
   }    
    
    private String encodedName(String str) {
 	   String ret = Normalizer.normalize(str,Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","");
 	   ret = ret.replace(".","");
 	   return WordUtils.capitalize(ret);
 	}
		
	private boolean nameExists(String nameToSearch ) {
		boolean ret = false;
        for (int k=1; k<items.size(); k++) {
      	  @SuppressWarnings("rawtypes")
      	  ArrayList item = items.get(k);
          ret = item.get(5).toString().equals(nameToSearch);
          if (ret) {
        	  break; 
          }
        }
        return ret;
	}
	
	public boolean isRoman(String str) {
		String romanStr = "";  // remove blank between numbers
		romanStr += "I,II,III,IV,V,VI,VII,VIII,IX,X,";
		romanStr += "XI,XII,XIII,XIV,XV,XVI,XVII,XVIII,XIX,XX,";
		romanStr += "XXI,XXII,XXIII,XXIV,XXV,XXVI,XXVII,XXVIII,XXIX,XXX,";
		romanStr += "XXXI,XXXII,XXXIII,XXXIV,XXXV,XXXVI,XXXVII,XXXVIII,XXXIX,XL,";
		romanStr += "XLI,XLII,XLIII,XLIV,XLV,XLVI,XLVII,XLVIII,XLIX,L,";
		romanStr += "LI,LII,LIII,LIV,LV,LVI,LVII,LVIII,LIX,LX,";
		romanStr += "LXI,LXII,LXIII,LXIV,LXV,LXVI,LXVII,LXVIII,LXIX,LXX,";
		romanStr += "LXXI,LXXII,LXXIII,LXXIV,LXXV,LXXVI,LXXVII,LXXVIII,LXXIX,LXXX,";
		romanStr += "LXXXI,LXXXII,LXXXIII,LXXXIV,LXXXV,LXXXVI,LXXXVII,LXXXVIII,LXXXIX,XC,";
		romanStr += "XCI,XCII,XCIII,XCIV,XCV,XCVI,XCVII,XCVIII,XCIX,C,";
		romanStr += "CI,CII,CIII,CIV,CV,CVI,CVII,CVIII,CIX,CX,";
        String[] romans = romanStr.split(",");
//        for (int i=0; i < romans.length; i++) {
//        	System.err.println(romans[i]);
//        }
        return (Arrays.stream(romans).anyMatch(str::equals));
	}
}



/*
Problemas verificados com o parser:
-----------------------------------

PROBLEMA: Como identificar Artigos repetidos dentro de um mesmo texto legal?
Isso � necess�rio para poder referenci�-los unicamente depois
Ex: Constitui��o,Lei 0556 de 1850,etc. 

Em http://localhost:8080/fuseki/HTMLs/L0556-1850.htm
----------------------------------------------------
soh apresenta a linha do artigo e perde as numeradas

Art. 461 - O registro deve conter:

1 - a declara��o do lugar onde a embarca��o foi constru�da,o nome do construtor,e a qualidade das madeiras principais;

2 - as dimens�es da embarca��o em palmos e polegadas; e a sua capacidade em toneladas,comprovadas por certid�o de arquea��o com refer�ncia � sua data;

3 - a arma��o de que usa,e quantas cobertas tem;

4 - o dia em que foi lan�ada ao mar;

5 - o nome de cada um dos donos ou compartes,e os seus respectivos domic�lios;

6 - men��o especificada do quinh�o de cada comparte,se for de mais de um propriet�rio,e a �poca da sua respectiva aquisi��o,com refer�ncia � natureza e data do t�tulo,que dever� acompanhar a peti��o para o registro. O nome da embarca��o registrada e do seu propriet�rio ostensivo ou armador ser�o publicados por an�ncios nos peri�dicos do lugar.


Em http://localhost:8080/fuseki/HTMLs/D92627.htm
------------------------------------------------
No cabecalho ha um link perdido: 
Revogado pelo Decreto n� 94.331,de 1987 


*/
