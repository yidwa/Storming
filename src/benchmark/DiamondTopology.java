package benchmark;

public class DiamondTopology extends BenchMarkTopology{
	
	  public DiamondTopology(String topologyname, int numworkers, long rateperSecond) throws InterruptedException{
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
		    builder.setSpout(spoutId, new DiamondSpout(false, rateperSecond));
		    builder.setBolt(aId, new B_appendA(),2).shuffleGrouping(spoutId, "addinga");
		    builder.setBolt(bId, new B_appendB(),2).shuffleGrouping(spoutId, "addingb");
		    builder.setBolt(cId, new B_appendC(),2).shuffleGrouping(spoutId, "addingc");
		    builder.setBolt(dId, new B_appendD(),2).shuffleGrouping(spoutId, "addingd");
		    builder.setBolt(lId, new B_removelast(),2).shuffleGrouping(aId)
		    										  .shuffleGrouping(bId)
		    								    	  .shuffleGrouping(cId)
		    										  .shuffleGrouping(dId);
	  }
 }
	