import java.io.File;

public class FileWalker {
	
    String dirList = "";
    String fileList = "";
    String everything = "";	

    public FileWalker( String path ) {
    	buildList( path);
    }
    
    public void buildList( String path ) {
    	
    	
    	File root = new File( path );
        File[] list = root.listFiles();

    	everything += "[root]" + root.getAbsolutePath() + "\n";
        
        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	everything += "[dir]" + f.getAbsoluteFile() + "\n";
                dirList += f.getAbsoluteFile() + "\n";
                buildList( f.getAbsolutePath() );
            }
            else {
            	everything += "[file]" + f.getAbsoluteFile() + "\n";
                fileList += f.getAbsoluteFile() + "\n";
            }
        }
        
    }
    
    public void walk() {
    	System.out.println( this.everything );
    }

    public String[] getEverything() {
    	return this.everything.split("\n");
    }

    public String[] getDirList() {
    	return this.dirList.split("\n");
    }

    public String[] getFileList() {
    	return this.fileList.split("\n");
    }
    
    
    public static void main(String[] args) {
        FileWalker fw = new FileWalker("c:/tmp");
        fw.walk();
        //System.out.println( fw.everything );
    }

}
