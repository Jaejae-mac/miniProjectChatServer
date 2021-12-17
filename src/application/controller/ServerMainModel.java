package application.controller;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import application.jdbc.repository.dao.IUserDAO;
import application.jdbc.repository.dao.UserDAO;
import application.jdbc.repository.vo.AdminTableVO;
import application.jdbc.repository.vo.UserTableVO;
import application.protocol.Protocol;
import application.protocol.vo.ObjectVO;
import application.protocol.vo.Room;

public class ServerMainModel {
	private IUserDAO dao = null;
	private HashMap<String, ObjectOutputStream> hm;

	private ServerMainModel() {

	}

	private static ServerMainModel smm = new ServerMainModel();

	public static ServerMainModel getInstance() {
		if (smm == null) {
			smm = new ServerMainModel();
		}
		return smm;
	}

	public HashMap<String, ObjectOutputStream> getHm() {
		return hm;
	}

	public void setHm(HashMap<String, ObjectOutputStream> hm) {
		this.hm = hm;
	}

	// 서버에 최초 로그인시 패스워드 비교.
	// 서버 관리자용.
	public boolean idAndPwCheck(AdminTableVO vo, String pw) {
		System.out.println(vo.getAdminPw());
		System.out.println(pw);
		if (vo.getAdminPw().equals(pw)) {
			return true;
		}
		return false;
	}

	// 사용자가 입력한 아이디의 존재여부 확인.
	// vo 에는 userId/ protocol 만 들어있음.
	// 친구 추가할 때 확인용도 혹은 아이디 중복체크 같은 곳에 사용.
	public ObjectVO idcheck(ObjectVO vo) {
		dao = UserDAO.getInstance();
		ObjectVO tvo = vo;
		UserTableVO uvo = null;
		uvo = dao.selectOneUserTable(tvo.getUserId());

		if (uvo != null) {
			tvo.setProtocol(Protocol.ID_EXIST);
			tvo.setuVo(uvo);
			ServerLog.login_sb.append("ServerMainModel - 존재하는 회원입니다. - " + new Date() + "\n");
			System.out.println(uvo.toString());
		} else {
			tvo.setProtocol(Protocol.ID_NOT_FOUND);
			ServerLog.login_sb.append("ServerMainModel - 존재하지 않는 회원입니다. - " + new Date() + "\n");
			System.err.println("존재XXXX");
		}

		return tvo;

	}

	public ObjectVO login(ObjectVO vo) {
		ObjectVO oVo = idcheck(vo);
		if (oVo.getProtocol().equals(Protocol.ID_EXIST)) {
			String uPw = oVo.getuVo().getUserPw();
			if (oVo.getUserPw().equals(uPw)) {
				oVo.setProtocol(Protocol.LOGIN_SUCCESS);
				
				ServerLog.login_sb.append("[ 로그인 알림 ]" + oVo.getUserId() + "님이 로그인 하셨습니다. - " + new Date() + "\n");
			} else {
				oVo.setProtocol(Protocol.LOGIN_FAIL);
				ServerLog.login_sb.append("[ 로그인 알림 ]" + oVo.getUserId() + "님이 로그인에 실패했습니다. - " + new Date() + "\n");
			}
		}

		return oVo;
	}

	// 유저에게 객체를 전달할 메소드
	public void sendMsg(ObjectVO vo, ObjectOutputStream oos) {
		try {
			oos.writeObject(vo);
			oos.flush();
			ServerLog.msg_sb.append("[ 알림 ] 유저에게 객체전송을 완료하였습니다. - " + new Date() + "\n");
		} catch (IOException e) {
			ServerLog.msg_sb.append("[ 알림 ] 유저에게 객체전송을 실패하였습니다. - " + new Date() + "\n");
			e.printStackTrace();
		}
	}

	// 로그인시도할때 친구목록을 불러온다.
	public ArrayList<String> getFriendList(String userId) {
		System.out.println(userId);
		ArrayList<String> list = null;
		dao = UserDAO.getInstance();
		list = dao.selectFriendTable(userId);
		System.err.println(list.toString());
		return list;
	}

	// 회원가입.
	public ObjectVO signUp(ObjectVO vo) {
		ObjectVO oVo = vo;
		UserTableVO uVo = vo.getuVo();
		dao = UserDAO.getInstance();
		dao.insertUserTable(uVo);
		oVo.setProtocol(Protocol.SIGNUP_SUCCESS);
		ServerLog.login_sb.append("[ 회원가입 ]" + uVo.getUserId() + "님이 회원가입하셨습니다." + new Date() + "\n");
		return oVo;
	}

	public void updateUser(ObjectVO vo) {
		ServerLog.msg_sb.append("[ 업데이트 ]" + vo.getUserId() + "의 정보를 업데이트 합니다. - " + new Date() + "\n");
		ServerLog.db_sb.append("[ 업데이트 ]" + vo.getUserId() + "의 정보를 갱신 합니다. - " + new Date() + "\n");
		dao.updateUserTable(vo.getuVo());
	}

	public void deleteUser(ObjectVO vo) {
		ServerLog.msg_sb.append("[ 탈퇴 ]" + vo.getUserId() + "의 정보를 삭제 합니다. - " + new Date() + "\n");
		ServerLog.db_sb.append("[ 삭제 ]" + vo.getUserId() + "의 정보를 삭제 합니다. - " + new Date() + "\n");
		dao.deleteUserTable(vo.getUserId());
		
	}

	public void insertFirstChat(IUserDAO dao, ObjectVO vo, String chatText) {
		dao = UserDAO.getInstance();
		dao.insertChatTable(vo.getUserId(), vo.getReceiver(), chatText);
		// 그리고 반대의 상황도 대화내용을 저장해야함 보내는이 / 받는이 1번
		// 받는이 / 보내는이 1번
		// 하여 총 2번 저장되어야한다.
		dao.insertChatTable(vo.getReceiver(), vo.getUserId(), chatText);
	}

	public void updateChat(IUserDAO dao, ObjectVO vo, String chatText) {
		dao.updateChatTable(vo.getUserId(), vo.getReceiver(), chatText);
		dao.updateChatTable(vo.getReceiver(), vo.getUserId(), chatText);
	}

	public ObjectVO findFriend(ObjectVO vo) {
		ObjectVO tempVo = vo;
		UserTableVO uvo = dao.selectOneUserTable(vo.getReceiver());
		if (uvo != null) {
			tempVo.setProtocol(Protocol.USER_EXIST);
			dao.insertFriendTable(vo.getUserId(), vo.getReceiver());
			dao.insertFriendTable(vo.getReceiver(), vo.getUserId());
		} else {
			tempVo.setProtocol(Protocol.CANNOT_FIND_USER);
		}

		return tempVo;

	}

}
