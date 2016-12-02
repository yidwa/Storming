package benchmark;

import org.apache.storm.tuple.Fields;

public class LineTopology extends BenchMarkTopology{
	 
	  public LineTopology(String topologyname, int numworkers, long rateperSecond) throws InterruptedException{
		  	super(topologyname, numworkers, rateperSecond);
		  	wireTopology(rateperSecond);
	  }

	  private void wireTopology(long rateperSecond) throws InterruptedException {
		  	String spoutId = "Spouting";
		    String aId = "appending_a";
		    String bId = "appending_b";
		    String cId = "appending_c";
		    String dId = "appending_d";
		    String lId = "removelast";
		    builder.setSpout(spoutId, new Spout(false, rateperSecond),2);
		    builder.setBolt(aId, new B_appendA(),2).fieldsGrouping(spoutId, new Fields("emiting"));
		    builder.setBolt(bId, new B_appendB(),2).fieldsGrouping(aId, new Fields("appendinga"));
		    builder.setBolt(cId, new B_appendC(),2).fieldsGrouping(bId, new Fields("appendingb"));
		    builder.setBolt(dId, new B_appendD(),2).fieldsGrouping(cId, new Fields("appendingc"));
		    builder.setBolt(lId, new B_removelast(),2).fieldsGrouping(dId, new Fields("appendingd"));
	  }
}
  
