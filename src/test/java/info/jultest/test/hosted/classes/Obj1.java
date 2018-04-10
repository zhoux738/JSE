package info.jultest.test.hosted.classes;

public class Obj1 {

	@Override
	public String toString(){
		return "obj1-string";
	}
	
	@Override
	public int hashCode(){
		return 100;
	}
	
	@Override
	public boolean equals(Object another){
		return this == another ? true : false;
	}
	
}
