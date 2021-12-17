package application.jdbc.repository.dao;

import java.sql.Connection;
import java.util.ArrayList;

import application.jdbc.repository.vo.AdminTableVO;
import application.jdbc.repository.vo.UserTableVO;
import application.protocol.vo.ObjectVO;
import application.protocol.vo.Room;

public interface IUserDAO {
	//유저 테이블에서 쿼리수행.
	void insertUserTable(UserTableVO vo);
	void updateUserTable(UserTableVO vo);
	void deleteUserTable(String userId);
	UserTableVO selectOneUserTable(String userId);
	ArrayList<UserTableVO> selectAllUserTable();
	
	
	//관리자 테이블에서 쿼리 수행.
	AdminTableVO selectAdmin(String adminId);
	
	
//	//친구 테이블에서 쿼리 수행.
	void insertFriendTable(String userId, String receiverId);
	void deleteFriendTable(String uid, String fid);
//	void updateFriendTable();
	ArrayList<String> selectFriendTable(String userId);
	
//	
//	//대화내용 쿼리 수행.
	void insertChatTable(String userId, String receiverId,String msg);
	void updateChatTable(String userId, String receiverId, String msg);
	String selectChatTable(String userId, String receiverId);
	
	void insertChatRoom(int flag, String userId, String chatText, Connection conn);
	ArrayList<Room> selectRooms(String userId);
	void insertInviteUser(int roomNo, String userId);
	void updateRoomChat(int roomNo, String userId, String chatText);
	void deleteRoomUser(int roomNo, String userId);
	int countUser(String userid,String friendId);
	String getChatText(int roomNo, String userId);
	ArrayList<String> selectRoomUser(int roomNum);
	
}
