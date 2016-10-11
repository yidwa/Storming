package general;

public class Spout {
	String id;
	String latency;
	Long transfered;
	
	public Spout(String id){
		this.id = id;
		this.latency = "";
		this.transfered = (long) 0;
	}

	public String getLatency() {
		return latency;
	}

	public void setLatency(String latency) {
		this.latency = latency;
	}

	public Long getTransfered() {
		return transfered;
	}

	public void setTransfered(Long transfered) {
		this.transfered = transfered;
	}
	
	
}
