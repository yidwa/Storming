package benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;


public class Methods {
	  public static class SentWithTime {
		    public final String sending;
		    public final long time;

		    public SentWithTime(String sending, long time) {
		        this.sending = sending;
		        this.time = time;
		    }
		    
	  }
	  
	 public static String formattime(){
			Instant instant = Instant.now (); // Current date-time in UTC.
			String output = instant.toString ();
			output = instant.toString ().replace ( "T" , " " ).replace( "Z" , "");
			return output;
		}
	  public static void writeFile(String sen){
			try {
				String path = "/Users/yidwa/Desktop/Records.txt";
//				String path = "/home/ubuntu/TopologyResult.txt";
				File f = new File(path);
				FileWriter fw = new FileWriter(f,true);
				String time = Methods.formattime();
				fw.write(time+" , "+ sen+"\n");
			
				fw.flush();
					
				fw.close();
				}
				catch (IOException e1) {
						// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}
//	  public static double format(String s){
//		  DecimalFormat formatter = new DecimalFormat("#0.000");
////		    System.out.println(formatter.format(t1*t2));
//		   double result = Double.valueOf(formatter.format(s));
//		   return result;
//	  }
}
