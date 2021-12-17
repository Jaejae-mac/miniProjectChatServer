package application.jdbc.repository.vo;

public class AdminTableVO {
	private String adminId;
	private String adminPw;
	
	public AdminTableVO() {
		// TODO Auto-generated constructor stub
	}
	
	public AdminTableVO(String adminId, String adminPw) {
		super();
		this.adminId = adminId;
		this.adminPw = adminPw;
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public String getAdminPw() {
		return adminPw;
	}

	public void setAdminPw(String adminPw) {
		this.adminPw = adminPw;
	}

	@Override
	public String toString() {
		return "AdminTableVO [adminId=" + adminId + ", adminPw=" + adminPw + "]";
	}

	
	
	
	
}
