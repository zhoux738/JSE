package info.jultest.test.hosted.classes;

public class Settings {

	private static int value;
	
	public static void set(int value){
		Settings.value = value;
	}
	
	public static int get(){
		return Settings.value;
	}
	
}
