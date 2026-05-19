package pcd.lab13.rmi.example_01;

import java.io.Serializable;

public class Message implements Serializable   {
	
	private String content;
	
	public Message(String s){
		content = s;
	}
	
	public String getContent(){
		return content;
	}
}
