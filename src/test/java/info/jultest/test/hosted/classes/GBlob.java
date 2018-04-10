package info.jultest.test.hosted.classes;

public class GBlob implements GObj {

	private int hash;
	
	public GBlob(int hash){
		this.hash = hash;
	}
	
	public int hash(){
		return hash;
	}
	
}
