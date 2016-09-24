package org.apache.storm.starter.spout;



import org.apache.storm.Config;
import org.apache.storm.topology.OutputFieldsDeclarer;
import java.util.Map;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import java.util.HashMap;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class testWord  extends BaseRichSpout {
    public static Logger LOG = LoggerFactory.getLogger(testWord.class);
    boolean _isDistributed;
    SpoutOutputCollector _collector;

    public testWord() {
        this(true);
    }

    public testWord(boolean isDistributed) {
        _isDistributed = isDistributed;
    }
        
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
    }
    
    public void close() {
        
    }
        
    public void nextTuple() {
        Utils.sleep(100);
        final String[] words = new String[] {"wang", "yi", "dan", "huang", "xiao","ran", "Beijing", "Melbourne"};
        final Random rand = new Random();
        final String word = words[rand.nextInt(words.length)];
        _collector.emit(new Values(word));
    }
    
    public void ack(Object msgId) {

    }

    public void fail(Object msgId) {
        
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