package general;

public class Bolt {
	String id;
	String capacity;
	String executedelay;
	String processdelay;
	Long emit;
	
	public Bolt(String id){
		this.id = id;
		this.capacity = "";
		this.executedelay = "";
		this.processdelay = "";
		this.emit = (long)0;
	}

	public Long getEmit() {
		return emit;
	}

	public void setEmit(Long emit) {
		this.emit = emit;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getExecutedelay() {
		return executedelay;
	}

	public void setExecutedelay(String executedelay) {
		this.executedelay = executedelay;
	}

	public String getProcessdelay() {
		return processdelay;
	}

	public void setProcessdelay(String processdelay) {
		this.processdelay = processdelay;
	}
	
	
}
