package info.jultest.test.hosted.classes;

public class GTree implements GObj {

	private GObj[] objs;
	
	public GTree(GObj[] objs){
		this.objs = objs;
	}
	
	public int hash(){
		int total = 0;
		for(GObj o : objs){
			total += o.hash();
		}
		
		return total;
	}
	
}
