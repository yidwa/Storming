package org.apache.storm.starter.spout;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import org.HdrHistogram.Histogram;
import org.apache.storm.Config;
import org.apache.storm.metrics.hdrhistogram.HistogramMetric;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.starter.TrendingTopic.SentWithTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrendingTopicSpout extends BaseRichSpout  {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Logger LOG = LoggerFactory.getLogger(TrendingTopicSpout.class);
    boolean _isDistributed;
    SpoutOutputCollector _collector;
    long _periodNano;
    long _emitAmount;
    Random _rand;
    long _nextEmitTime;
    long _emitsLeft;
    HistogramMetric _histo;
    
  
    public TrendingTopicSpout(boolean isDistributed, long ratePerSecond) {
        _isDistributed = isDistributed;
        if (ratePerSecond > 0) {
        	 _periodNano = Math.max(1, 1000000000/ratePerSecond);
             _emitAmount = Math.max(1, (long)((ratePerSecond / 1000000000.0) * _periodNano));
        }
        else {
        	_periodNano = Long.MAX_VALUE - 1;
        	_emitAmount = 1;
        }
    }
    	
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
        _rand = ThreadLocalRandom.current();
        _nextEmitTime = System.nanoTime();
        _emitsLeft = _emitAmount;
        _histo = new HistogramMetric(3600000000000L, 3);
        context.registerMetric("comp-lat-histo", _histo, 10); 
    }
  
    @Override
    public void nextTuple() {
      if (_emitsLeft <= 0 && _nextEmitTime <= System.nanoTime()) {
          _emitsLeft = _emitAmount;
          _nextEmitTime = _nextEmitTime + _periodNano;
      }

      if (_emitsLeft > 0) {
    	  final String[] words = new String[] {"wang", "yi", "dan", "huang", "xiao"};
          final Random rand = new Random();
          final String word = words[rand.nextInt(words.length)];
        //  String sentence = SENTENCES[_rand.nextInt(SENTENCES.length)];
          _collector.emit(new Values(word), new SentWithTime(word, _nextEmitTime - _periodNano));
          _emitsLeft--;
      }
    }
    public void close() {
        
    }

    
    public void ack(Object msgId) {
    	 long end = System.nanoTime();
         SentWithTime st = (SentWithTime)msgId;
         _histo.recordValue(end-st.time);
    }

    public void fail(Object msgId) {
    	 SentWithTime st = (SentWithTime)msgId;
         _collector.emit(new Values(st.sentence), msgId);
    }
    
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
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
}

   
    
 
    

