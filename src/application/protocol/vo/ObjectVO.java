package application.protocol.vo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import application.jdbc.repository.vo.UserTableVO;

public class ObjectVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String protocol;
	private String userId;
	private String userPw;
	private String receiver;
	private String message;
	private UserTableVO uVo = null;
	private ArrayList<String> liFriend;
	private String noticeMsg = null;
	private File file = null;
	private String ipAddr;
	private int port;
	private String fileName;
	private Room room;
	private ArrayList<Room> roomList;
	private int roomNo;
	public ObjectVO() {
		// TODO Auto-generated constructor stub
	}

	// Login or sign in
	public ObjectVO(String protocol, String userId, String userPw) {
		super();
		this.protocol = protocol;
		this.userId = userId;
		this.userPw = userPw;
	}

	// Sign UP
	public ObjectVO(String protocol, UserTableVO uVo) {
		super();
		this.protocol = protocol;
		this.uVo = uVo;
	}

	// sendMessage
	public ObjectVO(String protocol, String userId, String receiver, String message) {
		super();
		this.protocol = protocol;
		this.userId = userId;
		this.receiver = receiver;
		this.message = message;
	}

	// 아이디 중복 체크.
	public ObjectVO(String protocol, String userId) {
		super();
		this.protocol = protocol;
		this.userId = userId;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserPw() {
		return userPw;
	}

	public void setUserPw(String userPw) {
		this.userPw = userPw;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public UserTableVO getuVo() {
		return uVo;
	}

	public void setuVo(UserTableVO uVo) {
		this.uVo = uVo;
	}

	public ArrayList<String> getLiFriend() {
		return liFriend;
	}

	public void setLiFriend(ArrayList<String> liFriend) {
		this.liFriend = liFriend;
	}

	public String getNoticeMsg() {
		return noticeMsg;
	}

	public void setNoticeMsg(String noticeMsg) {
		this.noticeMsg = noticeMsg;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public int getPort() {
		return port;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public ArrayList<Room> getRoomList() {
		return roomList;
	}

	public void setRoomList(ArrayList<Room> roomList) {
		this.roomList = roomList;
	}

	public void setPort(int port) {
		this.port = port;
	}

	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	

	public int getRoomNo() {
		return roomNo;
	}

	public void setRoomNo(int roomNo) {
		this.roomNo = roomNo;
	}

	@Override
	public String toString() {
		return "ObjectVO [protocol=" + protocol + ", userId=" + userId + ", userPw=" + userPw + ", receiver=" + receiver
				+ ", message=" + message + ", uVo=" + uVo + ", liFriend=" + liFriend + ", noticeMsg=" + noticeMsg
				+ ", file=" + file + ", ipAddr=" + ipAddr + ", port=" + port + ", fileName=" + fileName + ", room="
				+ room + "]";
	}

	

	
}
