package info.jultest.test.hosted.classes;

public class Clustor {

	private Clustor[] clustors;
	
	private Node[] nodes;
	
	public Clustor(Clustor[] clustors, Node[] nodes){
		this.clustors = clustors;
		this.nodes = nodes;
	}
	
	public Clustor[] getClustors(){
		return clustors;
	}
	
	public Node[] getNodes(){
		return nodes;
	}
}
