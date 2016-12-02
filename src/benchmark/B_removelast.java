
package benchmark;

import java.util.Map;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B_removelast extends BaseRichBolt{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger Log = LoggerFactory.getLogger(B_appendA.class);
	@SuppressWarnings("unused")
	private OutputCollector collector;
	
    public B_removelast() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	

	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		String coming = (String) tuple.getValue(0);
		coming = coming.substring(0, coming.length()-2);
		collector.emit(new Values(coming));
//		System.out.println("emit "+coming);
//		Methods.writeFile("remove_emit :"+coming);
		collector.ack(tuple);
	}

	@Override
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		this.collector = collector;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declare) {
		// TODO Auto-generated method stub
		declare.declare(new Fields("removelast"));
	}



}
	  