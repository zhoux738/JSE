package info.jultest.test.hosted.classes;

public class Node {

	private Node next;
	
	private int value;
	
	public Node(int value, Node next){
		this.value = value;
		this.next = next;
	}
	
	public int getValue(){
		return value;
	}
	
	public Node getNext(){
		return next;
	}
	
	public void setNext(Node next){
		this.next = next;
	}
}
