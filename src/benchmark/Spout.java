package benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.io.WritableFactories;
import org.apache.storm.Config;
import org.apache.storm.metrics.hdrhistogram.HistogramMetric;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.starter.spout.TrendingTopicSpout;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.omg.CosNaming._NamingContextExtStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import benchmark.Methods.SentWithTime;

public class Spout extends BaseRichSpout{
	public static Logger LOG = LoggerFactory.getLogger(TrendingTopicSpout.class);
	boolean _isDistributed;
	SpoutOutputCollector _collector;
	long _periodNano;
	long _emitAmount;
	long nextEmitTime;
	long _emitsLeft;
	Random _rand;
	HistogramMetric _histo;
	
	public Spout(boolean _isDistributed, long ratePerSecond){
		this._isDistributed = _isDistributed;
		if(ratePerSecond > 0){
			// when the ratePersecond > 1000000000, emit amount >1, that is ratepersecond/1000000000, otherwise, emit amount is 1
			_periodNano = Math.max(1, 1000000000/ratePerSecond);
			_emitAmount = Math.max(1, (long)(ratePerSecond/ 1000000000.0) * _periodNano);
		}
		else {
			_periodNano = Long.MAX_VALUE - 1;
			_emitAmount = 1;
		}
	}
	
	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		if (_emitsLeft <= 0 && nextEmitTime <= System.nanoTime()){
			_emitsLeft = _emitAmount;
			nextEmitTime = nextEmitTime + _periodNano;
		}
		if (_emitsLeft > 0){
			final String emiting = "hello_world";
			_collector.emit(new Values(emiting), new SentWithTime(emiting, nextEmitTime-_periodNano));
//			System.out.println("emitting from spout ");
//			Methods.writeFile("spout_emit_"+emiting );
			_emitsLeft--;
		}
			
	}
	
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		// TODO Auto-generated method stub
		this._collector = collector;
		nextEmitTime = System.nanoTime();
		_emitsLeft = _emitAmount;
		
	}
	
	 public void fail(Object msgId) {
	     SentWithTime st = (SentWithTime)msgId;
	     _collector.emit(new Values(st.sending), msgId);
	  }
	   
	   
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		 declarer.declare(new Fields("word"));
	}
	

    @Override
    public Map<String, Object> getComponentConfiguration() {
        if(!_isDistributed) {
            Map<String, Object> ret = new HashMap<String, Object>();
            ret.put(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 1);
            return ret;
        } else {
            return null;
        }
    } 
	
//	public static class SentWithTime{
//		public final String sending;
//		public final long time;
//		public SentWithTime(String sentence, long time){
//			this.sending = sentence;
//			this.time = time;
//		}
//	}
}
