package benchmark;

import java.util.Map;
import java.util.Random;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Star_appendA extends BaseRichBolt{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger Log = LoggerFactory.getLogger(Star_appendA.class);
	@SuppressWarnings("unused")
	private OutputCollector collector;
	
    public Star_appendA() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	

	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		String coming = (String) tuple.getValue(0);
		coming = coming+"_a";
		
		String[] temp = {"addingb", "addingc", "addingd", "removea"};
		final Random rand = new Random();
        final String des = temp[rand.nextInt(temp.length)];
		collector.emit(des,new Values(coming));
		collector.ack(tuple);
//		Methods.writeFile("appenA_emit : "+coming + " to "+des);
		
		
		
	}

	@Override
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		this.collector = collector;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declare) {
		// TODO Auto-generated method stub
//		declare.declare(new Fields("appenda"));
		declare.declareStream("addingb", new Fields("B"));
		declare.declareStream("addingc", new Fields("C"));
		declare.declareStream("addingd", new Fields("D"));
		declare.declareStream("removea", new Fields("R"));
	}



}
	  