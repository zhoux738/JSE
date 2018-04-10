package info.jultest.test.hosted.classes;

public class Error1 {

	static int si;
	
	// java.lang.ExceptionInInitializerError
	static {
		int k = 5, j = 0;
		si = k / j;
	}
	
}
