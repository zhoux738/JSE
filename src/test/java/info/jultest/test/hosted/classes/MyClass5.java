package info.jultest.test.hosted.classes;

// - 2D array ctor
// - 2D array methods
public class MyClass5 {
	
	private String[][] sarray;
	
	public MyClass5(String[][] array){
		sarray = array;
	}
	
	//------- instance -------//
	
	public String[][] getStringArray() {
		return sarray;
	}
	
	public void appendStringArray(char c) {
		for(int i = 0; i < sarray.length; i++) {
			for(int j = 0; j < sarray[i].length;j++) {
				sarray[i][j] += c;
			}
		}
	}
	
}