
base_dir = "/home/fco/disco0/fco/XML2RDF/testes/htmls/medidas-provisorias"
olds_dir = "/home/fco/disco0/fco/XML2RDF/Backup/HTMLs"
dest_dir = "/home/fco/disco0/fco/XML2RDF/testes/processar/htmls"

files = Dir.entries("#{base_dir}")

puts "#{files.size} files selecteds" 

k=0

files.each do |file|
	if file.include? ".htm"
    old_file = "#{olds_dir}/#{file}"
    new_file = "#{dest_dir}/#{file}"
    bas_file = "#{base_dir}/#{file}"
		if !File.exists?(old_file)
      puts "#{bas_file}"
			system("cp #{bas_file} #{new_file}")
      k += 1
		end
	end
end

puts "Fim - processados #{k} arquivos"
