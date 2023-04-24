
#base_dir = "C:/Program Files/Apache Software Foundation/apache-tomcat-8.0.23/webapps/fuseki"
base_dir = "/home/fco/disco0/fco/XML2RDF/testes/processar/htmls"
dest_dir = "/home/fco/disco0/fco/XML2RDF/testes/processar/rdfs"

k = 0
s = ""
files = Dir.entries("#{base_dir}")


puts "#{files.size} files selecteds" 

#
# assume que os htmls foram baixados
# e estao disponiveis em "#{base_dir}/HTMLs"
#
files.each do |file|
	if file.include? ".htm"
		rdf = "#{dest_dir}/"+file.split(".")[0]+".n3"
		if !File.exists?(rdf)
			k += 1
			puts "#{k} #{file}"
			system("/home/fco/disco0/fco/XML2RDF/Pesquisa/HParser/hparser.cmd CONVERT #{file}") #if file.include? "emc"
			break
		end
	end
end

puts "Fim - processdados #{k} arquivos"
