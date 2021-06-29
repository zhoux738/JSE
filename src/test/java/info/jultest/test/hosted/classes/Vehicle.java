package info.jultest.test.hosted.classes;

public class Vehicle {

	private int speed = 0;
	
	public int getSpeed(){
		return speed;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public String getName(String str){
		return "veh:" + str;
	}
}
