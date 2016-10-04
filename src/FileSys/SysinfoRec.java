package FileSys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SysinfoRec {
	public File f;
	public static FileReader fr;
	public FileWriter fw;
	
	
	public SysinfoRec(String path) throws IOException{
		this.f = new File(path);
		this.fr = new FileReader(f);
		this.fw = new FileWriter(f);
	}
	
	public static void ReadInfo() {
		BufferedReader br = new BufferedReader(fr);
		String temp = "";
		try{
			while((temp = br.readLine())!= null){
				System.out.println(temp);
			}
		}
		catch(IOException e){
			System.out.println("something wrong when reading the sysinfo file");
		}
		finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}	
	}
	
	
	
}
