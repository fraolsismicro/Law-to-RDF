HPARSER_HOME=/home/fco/disco0/fco/XML2RDF/Pesquisa/HParser
LIB=$HPARSER_HOME/lib
CP=$HPARSER_HOME/bin
CP=$CP:$CP/LIB/commons-io-2.4.jar
CP=$CP:$CP/LIB/commons-lang3-3.3.2.jar
CP=$CP:$CP/LIB/commons-logging-1.1.2.jar
CP=$CP:$CP/LIB/httpclient-4.5.2.jar
CP=$CP:$CP/LIB/httpclient-cache-4.5.2.jar
CP=$CP:$CP/LIB/httpcore-4.4.4.jar
CP=$CP:$CP/LIB/jcl-over-slf4j-1.7.21.jar
CP=$CP:$CP/LIB/jena-arq-3.3.0.jar
CP=$CP:$CP/LIB/jena-base-3.3.0.jar
CP=$CP:$CP/LIB/jena-cmds-3.3.0.jar
CP=$CP:$CP/LIB/jena-core-3.3.0.jar
CP=$CP:$CP/LIB/jena-fuseki-core-2.6.0.jar
CP=$CP:$CP/LIB/jena-iri-3.3.0.jar
CP=$CP:$CP/LIB/jena-rdfconnection-3.3.0.jar
CP=$CP:$CP/LIB/jena-shaded-guava-3.3.0.jar
CP=$CP:$CP/LIB/jena-spatial-3.3.0.jar
CP=$CP:$CP/LIB/jena-tdb-3.3.0.jar
CP=$CP:$CP/LIB/jena-text-3.3.0.jar
CP=$CP:$CP/LIB/jsoup-1.8.2.jar
CP=$CP:$CP/LIB/libthrift-0.9.3.jar
CP=$CP:$CP/LIB/log4j-1.2.17.jar
CP=$CP:$CP/LIB/lucene-analyzers-common-6.4.1.jar
CP=$CP:$CP/LIB/lucene-core-6.4.1.jar
CP=$CP:$CP/LIB/lucene-misc-6.4.1.jar
CP=$CP:$CP/LIB/lucene-queries-6.4.1.jar
CP=$CP:$CP/LIB/lucene-queryparser-6.4.1.jar
CP=$CP:$CP/LIB/lucene-sandbox-6.4.1.jar
CP=$CP:$CP/LIB/lucene-spatial-6.4.1.jar
CP=$CP:$CP/LIB/lucene-spatial-extras-6.4.1.jar
CP=$CP:$CP/LIB/lucene-spatial3d-6.4.1.jar
CP=$CP:$CP/LIB/postgresql-8.4-703.jdbc3.jar
CP=$CP:$CP/LIB/slf4j-api-1.7.21.jar
CP=$CP:$CP/LIB/slf4j-log4j12-1.7.21.jar
CP=$CP:$CP/LIB/spatial4j-0.6.jar
CP=$CP:$CP/LIB/xercesImpl-2.11.0.jar
CP=$CP:$CP/LIB/xml-apis-1.4.01.jar
cd $HPARSER_HOME/bin
java -Xmx2048m -cp $CP ToRDF %1 %2
cd $HPARSER_HOME


