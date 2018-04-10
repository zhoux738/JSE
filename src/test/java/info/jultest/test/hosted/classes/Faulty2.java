package info.jultest.test.hosted.classes;

public class Faulty2 {

	public int getValue(int v){
		int value = getValue0(v);
		return value;
	}
	
	private int getValue0(int v) {
		try {
			return getValue1(v);
		} catch (NullPointerException npe){
			throw new IllegalArgumentException("Argument 'v' cannot be null.", npe);
		}
	}

	private int getValue1(int v) {
		if (v < 0) {
			throw new NullPointerException();
		}
		
		return 0;
	}
}

class NonPub {
	
}
