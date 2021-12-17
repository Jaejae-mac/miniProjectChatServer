package application.protocol;

public class Protocol {
	public static final String LOGIN = "100";
	public static final String LOGOUT= "101";
	
	public static final String LOGIN_SUCCESS = "102";
	public static final String LOGIN_FAIL = "103";
	public static final String SIGNUP = "104";
	public static final String SIGNUP_SUCCESS = "105";
	public static final String SIGNUP_FAIL = "106";
	public static final String ID_CHECK = "107";
	public static final String ID_EXIST = "108";
	public static final String ID_NOT_FOUND = "109";
	public static final String UPDATE_USERINFO = "110";
	public static final String DELETE_USER = "111";
	public static final String CREATE_ROOM = "200";
	public static final String ENTER_ROOM = "201";
	public static final String EXIT_ROOM = "202";
	public static final String GOTO_ROOM = "203";
	public static final String ENTER_ROOM_ALLOW = "204";
	public static final String EXIT_ROOM_DONE = "205";
	public static final String CREATE_GROUP_ROOM = "206";
	public static final String UPDATE_CHAT_TITLE = "207";
	public static final String SEND_MESSAGE = "300";
	public static final String SEND_FILE = "301";
	public static final String SEND_FILE_SUCCESS = "302";
	public static final String SEND_FILE_FAIL = "303";
	public static final String SEND_FILE_REQUEST = "304";
	public static final String SEND_FILE_RESPONSE_OK = "305";
	public static final String SEND_FILE_RESPONSE_NO = "306";
	public static final String CLEAR_CHAT_TEXT = "307";
	public static final String SEND_MESSAGE_ROOM = "308";
	public static final String ROOM_EXIST = "309"; 
	
	public static final String Add_FREIND = "400";
	public static final String GET_UESRINFO = "401";
	public static final String CANNOT_FIND_USER = "402";
	public static final String USER_EXIST = "403";
	
	
	public static final String INVITE_USER = "500";
	public static final String DELETE_FRIENDS = "501";
	public static final String DELETE_DONE = "502";
	
}
