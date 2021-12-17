package application.jdbc.repository.vo;

import java.io.Serializable;

public class UserTableVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int userNo;
	private String userId;
	private String userPw;
	private String userName;
	private String userTel;
	private String userEmail;
	private String noticeMsg;

	public UserTableVO() {
		// TODO Auto-generated constructor stub
	}

	public UserTableVO(int userNo, String userId, String userPw, String userName, String userTel, String userEmail) {
		super();
		this.userNo = userNo;
		this.userId = userId;
		this.userPw = userPw;
		this.userName = userName;
		this.userTel = userTel;
		this.userEmail = userEmail;
	}

	public UserTableVO(String userId, String userPw, String userName, String userTel, String userEmail) {
		super();
		this.userId = userId;
		this.userPw = userPw;
		this.userName = userName;
		this.userTel = userTel;
		this.userEmail = userEmail;
	}

	public int getUserNo() {
		return userNo;
	}

	public void setUserNo(int userNo) {
		this.userNo = userNo;
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserTel() {
		return userTel;
	}

	public void setUserTel(String userTel) {
		this.userTel = userTel;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getNoticeMsg() {
		return noticeMsg;
	}

	public void setNoticeMsg(String noticeMsg) {
		this.noticeMsg = noticeMsg;
	}

	@Override
	public String toString() {
		return "UserTableVO [userNo=" + userNo + ", userId=" + userId + ", userPw=" + userPw + ", userName=" + userName
				+ ", userTel=" + userTel + ", userEmail=" + userEmail + "]";
	}

}
