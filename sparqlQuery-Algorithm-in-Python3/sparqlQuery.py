# SPARQLWrapper
# fonte https://rdflib.dev/sparqlwrapper/doc/latest/main.html
from SPARQLWrapper import SPARQLWrapper, JSON

# OpenPyXL
# fonte https://www.blog.pythonlibrary.org/2021/07/27/creating-spreadsheets-with-openpyxl-and-python/
from openpyxl import Workbook


def get_triples_query( search_terms ):
  # print("Mounting query for: ", search_terms)
  # mount select
  qy = """
    PREFIX lex: <http://localhost:8080/fuseki/>
    SELECT ?s ?p ?o 
    WHERE { 
      ?s lex:temConteudo ?o . 
      BIND(str('lex:temConteudo') AS ?p) . 
  """
  # mount filters
  for word in search_terms.split(" "):
    qy = qy + "  FILTER ( CONTAINS(lcase(str(?o)), '[WORD]') ) .".replace("[WORD]", word) + "\n  "
      # FILTER ( CONTAINS(?o, 'direito') ) .
      # FILTER ( CONTAINS(?o, 'consumidor') ) .
  # mount order and limit    
  qy = qy + """
    } 
    ORDER BY ?s ?p ?o 
    LIMIT 300
  """
  return qy


def get_neighbours_query( resource ):
  # print("Mounting query for neighbours of ", resource)
  qy = """  
    PREFIX lex: <http://localhost:8080/fuseki/>
    SELECT ?s ?p ?o
    { 
      { 
        <[RESOURCE]> ?p ?o . 
        BIND('<[RESOURCE]>' AS ?s) 
      } 
      UNION 
      { 
        ?s ?p <[RESOURCE]>
        BIND('<[RESOURCE]>' AS ?o) 
      } 
      FILTER(
      CONTAINS(STR(?p), 'remetePara') 
        || CONTAINS(STR(?p), 'revogou') 
        || CONTAINS(STR(?p), 'revogadoPor') 
        || CONTAINS(STR(?p), 'regulamenta') 
        || CONTAINS(STR(?p), 'regulamentadoPor') 
      ) . 
    } 
    ORDER BY ?s ?p ?o 
    LIMIT 300
  """.replace("[RESOURCE]", resource).replace("<<", "<").replace(">>", ">")
  return qy

def get_next_neighbours_query( resource ):
  # print("Mounting query for neighbours of ", resource)
  qy = """  
    PREFIX lex: <http://localhost:8080/fuseki/>
    SELECT ?s ?p ?o
    { 
      { 
        <[RESOURCE]> ?p ?o . 
        BIND('<[RESOURCE]>' AS ?s) 
      } 
      UNION 
      { 
        ?s ?p <[RESOURCE]>
        BIND('<[RESOURCE]>' AS ?o) 
      } 
      FILTER(
        CONTAINS(STR(?p), 'temConteudo') 
      ) . 
    } 
    ORDER BY ?s ?p ?o 
    LIMIT 300
  """.replace("[RESOURCE]", resource).replace("<<", "<").replace(">>", ">")
  return qy

def show_triples( results ):
  for triple in results:
    print(triple["s"]["value"].replace("http://localhost:8080/fuseki/", "lex:"), "\t", end="")
    print(triple["p"]["value"].replace("http://localhost:8080/fuseki/", "lex:"), "\t", end="")
    print(triple["o"]["value"].replace("http://localhost:8080/fuseki/", "lex:"))



# define the endpoint and return format
ENDPOINT = "http://localhost:3030/ds"
sparql = SPARQLWrapper(ENDPOINT)
sparql.setReturnFormat(JSON)


workbook = Workbook()
del workbook["Sheet"]


# queries
queries = ["desmembramento município", 
  "direito consumidor"]

for query in queries:
  results = []
  # set query to get triples satisfying search terms
  sparql.setQuery( get_triples_query(query) )
  # apply to triplestore
  triples = sparql.query().convert()
  # add rows to results
  results.extend(triples["results"]["bindings"])
  # search neighbours at distance 1 algorithm
  for triple in triples["results"]["bindings"]:
    sparql.setQuery( get_neighbours_query( triple["s"]["value"] ) )
    rows = sparql.query().convert()
    results.extend(rows["results"]["bindings"])
    for row in rows["results"]["bindings"]:
      sparql.setQuery( get_next_neighbours_query( row["s"]["value"] ) )
      nexts = sparql.query().convert()
      results.extend(nexts["results"]["bindings"]) 
      sparql.setQuery( get_next_neighbours_query( row["o"]["value"] ) )
      nexts = sparql.query().convert()
      results.extend(nexts["results"]["bindings"]) 
      
  workbook.create_sheet(title=query)

  sheet = workbook[query]
  k=0
  for triple in results:
    k=k+1
    sheet[f'A{k}'] = triple["s"]["value"].replace("http://localhost:8080/fuseki/", "lex:").replace("<", "").replace(">", "")
    sheet[f'B{k}'] = triple["p"]["value"].replace("http://localhost:8080/fuseki/", "lex:").replace("<", "").replace(">", "")
    sheet[f'C{k}'] = triple["o"]["value"].replace("http://localhost:8080/fuseki/", "lex:").replace("<", "").replace(">", "")
  #show_triples(results)

workbook.save("tri.xlsx")
# to-do:
# remove duplicate triples (same s,p,o)

print(results)