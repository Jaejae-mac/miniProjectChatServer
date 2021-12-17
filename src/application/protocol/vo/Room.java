package application.protocol.vo;

import java.io.Serializable;
import java.util.ArrayList;

public class Room implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int roomNo;
	private String userId;
	private String chatText;
	private ArrayList<String> users;
	public Room() {
		users = new ArrayList<String>();
	}

	public Room(int roomNo, String userId, String chatText) {
		this();
		this.roomNo = roomNo;
		this.userId = userId;
		this.chatText = chatText;
	}

	public int getRoomNo() {
		return roomNo;
	}

	public void setRoomNo(int roomNo) {
		this.roomNo = roomNo;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getChatText() {
		return chatText;
	}

	public void setChatText(String chatText) {
		this.chatText = chatText;
	}
	
	public ArrayList<String> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<String> users) {
		this.users = users;
	}

	@Override
	public String toString() {
		return "Room [roomNo=" + roomNo + ", userId=" + userId + ", chatText=" + chatText + ", users=" + users + "]";
	}

	
	
	
}
