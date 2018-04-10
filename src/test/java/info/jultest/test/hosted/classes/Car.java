package info.jultest.test.hosted.classes;

public class Car extends Vehicle {

	@Override
	public int getSpeed(){
		return 50;
	}
	
	@Override
	public String getName(String str){
		return "car:" + str;
	}
	
}
