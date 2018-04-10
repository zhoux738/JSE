package info.jultest.test.hosted.classes;

public class Faulty1 {

	public int getValue(int v){
		int value = getValue0(v);
		return value;
	}
	
	private int getValue0(int v) {
		if (v < 0) {
			throw new IllegalArgumentException("Argument 'v' cannot be negative.");
		}
		return 0;
	}
}
