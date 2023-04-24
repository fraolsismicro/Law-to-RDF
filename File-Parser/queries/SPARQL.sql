
library(SPARQL)
# help(package="SPARQL") 

query <- "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbpprop: <http://dbpedia.org/property/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT ?label ?numEmployees ?netIncome  
WHERE {
  ?s dcterms:subject <http://dbpedia.org/resource/Category:Companies_in_the_Dow_Jones_Industrial_Average> ;
     rdfs:label ?label ;
     dbo:netIncome ?netIncomeDollars ;
     dbpprop:numEmployees ?numEmployees . 
     BIND(replace(?numEmployees,',','') AS ?employees)  # lose commas
     FILTER ( lang(?label) = 'en' )
     FILTER(contains(?netIncomeDollars,'E'))
     # Following because DBpedia types them as dbpedia:datatype/usDollar
     BIND(xsd:float(?netIncomeDollars) AS ?netIncome)
     # original query on following line had two slashes, but 
     # R needed both escaped
     FILTER(!(regex(?numEmployees,'\\\\d+')))
}
ORDER BY ?numEmployees"

endpoint <- "http://dbpedia.org/sparql"
resultList <- SPARQL(endpoint,query)

typeof(resultList)

summary(resultList)

queryResult <- resultList$results 
str(queryResult)

summary(queryResult)

var(queryResult$numEmployees)
sd(queryResult$numEmployees)
hist(queryResult$netIncome)

cor(queryResult$netIncome,queryResult$numEmployees)
myLinearModelData <- lm(queryResult$numEmployees~queryResult$netIncome)
plot(queryResult$netIncome,queryResult$numEmployees,xlab="net income",
   ylab="# of employees", main="Dow Jones Industrial Average companies")

abline(myLinearModelData)  

plot(queryResult$netIncome,queryResult$numEmployees,xlab="net income",
   ylab="# of employees", main="Dow Jones Industrial Average companies",
   sub=paste("correlation: ",cor(queryResult$numEmployees,
             queryResult$netIncome),sep=""))   
			 
			 
			 
AUTOMATING IT:

library(SPARQL) 

category <- "Companies_in_the_Dow_Jones_Industrial_Average"
#category <- "Electronics_companies_of_the_United_States"
#category <- "Financial_services_companies_of_the_United_States"

query <- "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbpprop: <http://dbpedia.org/property/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT ?label ?numEmployees ?netIncome  
WHERE {
  ?s dcterms:subject <http://dbpedia.org/resource/Category:DUMMY-CATEGORY-NAME> ;
     rdfs:label ?label ;
     dbo:netIncome ?netIncomeDollars ;
     dbpprop:numEmployees ?numEmployees . 
     BIND(replace(?numEmployees,',','') AS ?employees)  # lose commas
     FILTER ( lang(?label) = 'en' )
     FILTER(contains(?netIncomeDollars,'E'))
     # Following because DBpedia types them as dbpedia:datatype/usDollar
     BIND(xsd:float(?netIncomeDollars) AS ?netIncome)
     # Original query on following line had two 
     # slashes, but R needed both escaped.
     FILTER(!(regex(?numEmployees,'\\\\d+')))
}
ORDER BY ?numEmployees"

query <- sub(pattern="DUMMY-CATEGORY-NAME",replacement=category,x=query)

endpoint <- "http://dbpedia.org/sparql"
resultList <- SPARQL(endpoint,query)
queryResult <- resultList$results 
correlationLegend=paste("correlation: ",cor(queryResult$numEmployees,
                         queryResult$netIncome),sep="")
myLinearModelData <- lm(queryResult$numEmployees~queryResult$netIncome) 
plotTitle <- chartr(old="_",new=" ",x=category)
outputFilename <- paste("c:/temp/",category,".jpg",sep="")
jpeg(outputFilename)
plot(queryResult$netIncome,queryResult$numEmployees,xlab="net income",
     ylab="number of employees", main=plotTitle,cex.main=.9,
     sub=correlationLegend)
abline(myLinearModelData) 
dev.off()

source("/temp/myscript.R")
rscript /temp/myscript.R			 
