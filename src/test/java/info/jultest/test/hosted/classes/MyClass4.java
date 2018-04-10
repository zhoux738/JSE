package info.jultest.test.hosted.classes;

// - array ctor
// - array methods
public class MyClass4 {
	
	private int[] iarray;
	
	public MyClass4(int[] array){
		iarray = array;
	}
	
	//------- instance -------//
	
	public int[] getIntArray() {
		return iarray;
	}
	
	public void incrementIntArray(int val) {
		for(int i = 0; i < iarray.length; i++) {
			iarray[i] += val;
		}
	}
	
}