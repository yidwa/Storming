package FileSys;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerSide implements Runnable {
	public static ServerSocket ss;
//	public File f;
	public Socket s;
	
	public ServerSide() throws IOException{
		this.ss = new ServerSocket(25345);
	
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {

			try {
				s = ss.accept();

				new Thread(new ServerThread(s)).start();

			} catch (BindException e) {

			} catch (IOException e) {
				// TODO Auto-generated catch block

			}
		}
	}
}
