package info.jultest.test.hosted.classes;

// - return another type
public class NodeBox {
	
	public NodeBox(){
	}
	
	//------- instance -------//
	
	public Node getNode(int i) {
		return new Node(i, null);
	}
	
	public Node passNode(Node n) {
		return n;
	}
	
}