package application.jdbc.repository.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import application.controller.ServerLog;
import application.jdbc.repository.vo.AdminTableVO;
import application.jdbc.repository.vo.UserTableVO;
import application.jdbc.util.JDBCUtil;
import application.protocol.vo.ObjectVO;
import application.protocol.vo.Room;

public class UserDAO implements IUserDAO {

	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	Properties pro = null;

	private UserDAO() {
		// Properties 객체 생성.
		pro = new Properties();
		try {
			// properties 를 사용한 키/value 형태 해쉬맵으로 쿼리문을 관리.
			pro.load(new FileInputStream("src/application/jdbc/repository/properties/usertable.properties"));
		} catch (FileNotFoundException e) {
			System.err.println("[ 서버 알림 ] 파일을 찾지 못했습니다. - UserDAO");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("[ 서버 알림 ] 파일을 불러오는 도중에 에러 발생. - UserDAO");
			e.printStackTrace();
		}
	}

	private static UserDAO dao = new UserDAO();

	// 싱글톤 형태로 DAO 객체를 관리하기 위한 메소드 getInstance()
	public static UserDAO getInstance() {
		if (dao == null) {
			dao = new UserDAO();
		}
		return dao;
	}

	// 관리자 테이블...

	@Override
	public AdminTableVO selectAdmin(String adminId) {
		AdminTableVO vo = null;
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("admintable_select"));
			pstmt.setString(1, adminId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				vo = new AdminTableVO(rs.getString("admin_id"), rs.getString("admin_pw"));
			}

		} catch (SQLException e) {
			System.err.println("[ 서버 알림 ] select One 도중 에러 발생 - UserDAO");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.close(rs, pstmt, conn);

		}
		return vo;
	}

	@Override
	public void insertUserTable(UserTableVO vo) {
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("usertable_insert"));
			pstmt.setString(1, vo.getUserId());
			pstmt.setString(2, vo.getUserPw());
			pstmt.setString(3, vo.getUserName());
			pstmt.setString(4, vo.getUserTel());
			pstmt.setString(5, vo.getUserEmail());

			pstmt.executeUpdate();
			ServerLog.db_sb.append("[ 서버 알림 ] 삽입을 완료 했습니다.- UserDAO -InsertUserTable " + new Date() + "\n");
		} catch (SQLException e) {
			System.err.println("[ 서버 알림 ] 삽입 도중 에러 발생 - UserDAO");
			ServerLog.db_sb.append("[ 서버 알림 ] 삽입 도중 에러 발생 - UserDAO -InsertUserTable " + new Date() + "\n");
			e.printStackTrace();
		}finally {
			JDBCUtil.close(pstmt, conn);
		}

	}

	@Override
	public void updateUserTable(UserTableVO vo) {
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("usertable_update"));
			pstmt.setString(1, vo.getUserTel());
			pstmt.setString(2, vo.getUserEmail());
			pstmt.setInt(3, vo.getUserNo());
			int result = pstmt.executeUpdate();

			ServerLog.db_sb
					.append("[업데이트 알림] " + result + "개의 행을 갱신했습니다. - UserDAO - updateUserTable - " + new Date() + "\n");

			ServerLog.db_sb.append("[유저정보 업데이트] " + vo.getUserId() + "님의 전화번호, 이메일을 업데이트 했습니다." + new Date() + "\n");
		} catch (SQLException e) {

			e.printStackTrace();
		}finally {
			JDBCUtil.close(pstmt, conn);
		}

	}

	@Override
	public void deleteUserTable(String userId) {
		conn = JDBCUtil.getConnection();
		try {
			int result = 0;
			pstmt = conn.prepareStatement(pro.getProperty("usertable_delete"));
			pstmt.setString(1, userId);
			result = pstmt.executeUpdate();
			ServerLog.db_sb.append("[유저정보 삭제] " + userId + "님의 정보를 삭제 했습니다.." + new Date() + "\n");
			ServerLog.msg_sb.append("[회원 탈퇴] " + userId + "님의 정보를 회원 탈퇴를했습니다.." + new Date() + "\n");

			pstmt = conn.prepareStatement(pro.getProperty("friendList_deleteUsers"));
			pstmt.setString(1, userId);
			result = pstmt.executeUpdate();
			ServerLog.db_sb.append(
					"[유저정보 삭제] 친구목록 테이블에서 " + userId + "님의 정보를 삭제 했습니다.." + result + "개의 행 삭제." + new Date() + "\n");

			pstmt = conn.prepareStatement(pro.getProperty("chat_deleteUsers"));
			pstmt.setString(1, userId);
			result = pstmt.executeUpdate();
			ServerLog.db_sb.append(
					"[유저정보 삭제] 채팅내용 테이블에서 " + userId + "님의 정보를 삭제 했습니다.." + result + "개의 행 삭제." + new Date() + "\n");

		} catch (SQLException e) {
			ServerLog.db_sb.append("[회원 탈퇴] " + userId + "님의 회원탈퇴중 에러가 발생 했습니다.." + new Date() + "\n");
			e.printStackTrace();
		}finally {
			JDBCUtil.close(pstmt, conn);
		}

	}

	@Override
	public UserTableVO selectOneUserTable(String userId) {
		UserTableVO vo = null;
		ResultSet rs2 = null;
		System.out.println(userId);
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("usertable_selectOne"));
			pstmt.setString(1, userId);
			rs2 = pstmt.executeQuery();
			if (rs2.next()) {
				System.err.println("Im here!!!");
				vo = new UserTableVO(rs2.getInt("user_no"), rs2.getString("user_id"), rs2.getString("user_pw"),
						rs2.getString("user_name"), rs2.getString("user_tel"), rs2.getString("user_email"));
			}
//			System.out.println(vo.toString());
//			vo.setNoticeMsg("[ 서버 알림 ] 검색 완료했습니다.");
			if (vo != null) {
				ServerLog.db_sb.append(userId + " - [ 서버 알림 ] 해당유저의 검색을 완료했습니다. - " + new Date() + "\n");
			} else {
				ServerLog.db_sb.append(userId + " - [ 서버 알림 ] 해당유저의 검색을 완료했습니다.- 없는 유저입니다. - " + new Date() + "\n");
			}

		} catch (SQLException e) {
			System.err.println("[ 서버 알림 ] select One 도중 에러 발생 - UserDAO");
			ServerLog.db_sb.append(userId + " - [ 서버 알림 ] select One 도중 에러 발생 - UserDAO - " + new Date() + "\n");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.close(rs2, pstmt, conn);

		}

		return vo;
	}

	@Override
	public ArrayList<UserTableVO> selectAllUserTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> selectFriendTable(String userId) {
		ArrayList<String> list = new ArrayList<String>();
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("friendList_select"));
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(1));
				list.add(rs.getString("friend_id"));

			}
			ServerLog.db_sb.append(userId + " - [ 서버 알림 ] 친구목록 검색이 완료되었습니다. - " + new Date() + "\n");

		} catch (SQLException e) {
			list.add(userId + " - [ 서버 알림 ] select One 도중 에러 발생 - UserDAO");
			ServerLog.db_sb.append(userId + " - [ 서버 알림 ] select One 도중 에러 발생 - UserDAO - " + new Date() + "\n");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.close(rs, pstmt, conn);

		}
		return list;
	}

	public void insertChatTable(String userId, String receiverId, String msg) {
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("chat_insert"));
			pstmt.setString(1, userId);
			pstmt.setString(2, receiverId);
			pstmt.setString(3, msg);
			int result = pstmt.executeUpdate();
			ServerLog.db_sb.append("chat 테이블에 " + result + "개의 행을 삽입 완료했습니다.- " + new Date() + "\n");
		} catch (SQLException e) {
			ServerLog.db_sb.append("chat 테이블에 행을 삽입하는데 실패했습니다.- " + new Date() + "\n");
			e.printStackTrace();
		} finally {
			JDBCUtil.close(pstmt, conn);

		}

	}

	public void updateChatTable(String userId, String receiverId, String msg) {
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("chat_update"));
			pstmt.setString(1, msg);
			pstmt.setString(2, userId);
			pstmt.setString(3, receiverId);

			int result = pstmt.executeUpdate();
			ServerLog.db_sb.append("chat 테이블에 " + result + "개의 행을 갱신 완료했습니다.- " + new Date() + "\n");

		} catch (SQLException e) {
			ServerLog.db_sb.append("chat 테이블에 행을 갱신하는데 실패했습니다.- " + new Date() + "\n");
			e.printStackTrace();
		} finally {
			JDBCUtil.close(pstmt, conn);
		}
	}

	public String selectChatTable(String userId, String receiverId) {
		conn = JDBCUtil.getConnection();
		String chatText = "";
		try {
			pstmt = conn.prepareStatement(pro.getProperty("chat_select"));
			pstmt.setString(1, userId);
			pstmt.setString(2, receiverId);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				chatText = rs.getString("chat_text");
			}
			ServerLog.db_sb.append("chat 테이블에서 행 조회를 완료했습니다.- " + new Date() + "\n");
		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			JDBCUtil.close(rs, pstmt, conn);
		}
		return chatText;

	}

	public void insertFriendTable(String userId, String receiverId) {
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("friendList_insert"));
			pstmt.setString(1, userId);
			pstmt.setString(2, receiverId);
			pstmt.executeUpdate();
			ServerLog.db_sb
					.append("[친구 추가 알림]" + userId + "님의 친구목록에 " + receiverId + "님을 추가하였습니다. - " + new Date() + "\n");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			JDBCUtil.close(rs, pstmt, conn);
			
			
		}
	}

	// 방 관련.
	@Override
	public ArrayList<Room> selectRooms(String userId) {
		ArrayList<Room> li = new ArrayList<Room>();
		conn = JDBCUtil.getConnection();

		try {
			pstmt = conn.prepareStatement(pro.getProperty("select_room_user"));
			pstmt.setString(1, userId);
			pstmt.setString(2, userId);
			rs = pstmt.executeQuery();

			boolean flag = false;
			while (rs.next()) {
				int rn = rs.getInt("room_no");
				String user_id = rs.getString("user_id");
				String ct = rs.getString("chat_text");
				Room room = new Room(rn, userId, ct);
				room.getUsers().add(user_id);
				
				if(li.size()>0) {
					for (Room tr : li) {
						if (tr.getRoomNo() == rn) {
							tr.getUsers().add(user_id);
							flag = true;
						} else {
							flag = false;
						}
					}
				}
				
				
				if(!flag) {
					li.add(room);
					
				}
				

//				Room room = new Room(rs.getInt("room_no"),rs.getString("user_id"),rs.getString("chat_text"));
//				
			}
			for(Room r : li) {
				System.out.println(r.getUsers());
			}

		} catch (

		SQLException e) {
			System.err.println("방 목록을 불러오다가 에러발생.");
			e.printStackTrace();
		} finally {
			JDBCUtil.close(rs, pstmt, conn);
		}
		return li;
	}

	@Override
	public void insertChatRoom(int flag, String userId, String chatText, Connection conn2) {
//		conn = JDBCUtil.getConnection();
		try {
			// 내가 유저를 클릭해서 방을 만들면 같은 방번호를 갖는 상대유저에게도 방이 만들어져야 하기 때문에 이렇게 만듬.
			if(flag == 0) {
				// 나에게 만드는방.
				pstmt = conn2.prepareStatement(pro.getProperty("insert_chat_room_me"));
			}
			else
			{
				// 상대에게 반드는 방.
				pstmt = conn2.prepareStatement(pro.getProperty("insert_chat_room_other"));
			}

			pstmt.setString(1, userId);
			pstmt.setString(2, chatText);

			int result = pstmt.executeUpdate();
			ServerLog.msg_sb.append("[ 방 생성 알림 ]" + " [ " + userId + " ]" + "님이 방을생성하셨습니다.");

		} catch (SQLException e) {
			System.err.println("방을 생성하는 도중 에러 발생.");
			e.printStackTrace();
		} finally {
			if(flag != 0) {
			JDBCUtil.close(pstmt, conn2);
			}
		}

	}

	@Override
	public void insertInviteUser(int roomNo, String userId) {
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("insert_invite_user"));
			pstmt.setInt(1, roomNo);
			pstmt.setString(2, userId);
			pstmt.setString(3, "Lorem Ipsum..:");

			int result = pstmt.executeUpdate();
			ServerLog.msg_sb.append("[ 방 초대 알림 ]" + " [ " + userId + " ]" + "님을 방에 초대하였습니다.");
		} catch (SQLException e) {
			System.err.println("유저 추가하는 도중에 에러 발생.");
			e.printStackTrace();
		} finally {
			JDBCUtil.close(pstmt, conn);

		}

	}

	@Override
	public void updateRoomChat(int roomNo, String userId, String chatText) {
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("update_chat_text"));
			pstmt.setString(1, chatText);
			pstmt.setInt(2, roomNo);
			pstmt.setString(3, userId);
			int result = pstmt.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCUtil.close(pstmt, conn);
		}

	}

	@Override
	public void deleteRoomUser(int roomNo, String userId) {
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("delete_room_user"));
			pstmt.setInt(1, roomNo);
			pstmt.setString(2, userId);
			int result = pstmt.executeUpdate();
			ServerLog.msg_sb.append("[ 방퇴실 알림 ]" + " [ " + userId + " ]" + "님을 방을 떠났습니다.");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCUtil.close(pstmt, conn);
		}

	}

	@Override
	public int countUser(String userid, String friendId) {
		conn = JDBCUtil.getConnection();
		int result = 0;
		try {
			pstmt = conn.prepareStatement(pro.getProperty("count_user"));
			pstmt.setString(1, userid);
			pstmt.setString(2, friendId);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				result = rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			JDBCUtil.close(rs, pstmt, conn);
		}
		return result;
	}

	@Override
	public String getChatText(int roomNo, String userId) {
		conn = JDBCUtil.getConnection();
		String ct = "";
		try {
			pstmt = conn.prepareStatement(pro.getProperty("select_chat_text"));
			pstmt.setInt(1, roomNo);
			pstmt.setString(2, userId);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				ct = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			JDBCUtil.close(rs, pstmt, conn);
			
			
		}
		return ct;
	}

	@Override
	public void deleteFriendTable(String uid, String fid) {
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("friendList_deleteUsers"));
			pstmt.setString(1, uid);
			pstmt.setString(2, fid);
			
			int result = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}finally {
			JDBCUtil.close(pstmt, conn);
		}
		
	}

	@Override
	public ArrayList<String> selectRoomUser(int roomNum) {
		ArrayList<String> li = new ArrayList<String>();
		conn = JDBCUtil.getConnection();
		try {
			pstmt = conn.prepareStatement(pro.getProperty("select_room_users"));
			pstmt.setInt(1, roomNum);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				li.add(rs.getString("user_id"));
			}
		
		} catch (SQLException e) {	
			e.printStackTrace();
		}finally {
			JDBCUtil.close(rs, pstmt, conn);
		}
		
		return li;
	}

}
