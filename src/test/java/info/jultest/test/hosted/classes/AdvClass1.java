package info.jultest.test.hosted.classes;

// Single field class
public class AdvClass1 {
	
	private int value;
	
	public void increment(int val){
		value += val;
	}
	
	public int getResult(){
		return value;
	}
	
}