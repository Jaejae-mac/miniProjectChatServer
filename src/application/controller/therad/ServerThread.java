package application.controller.therad;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import application.controller.ServerLog;
import application.controller.ServerMainModel;
import application.jdbc.repository.dao.IUserDAO;
import application.jdbc.repository.dao.UserDAO;
import application.jdbc.util.JDBCUtil;
import application.protocol.Protocol;
import application.protocol.vo.ObjectVO;
import application.protocol.vo.Room;

public class ServerThread implements Runnable {
	private Socket socket = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;

	private HashMap<String, ObjectOutputStream> hmUsers = null;
	private HashMap<String, Socket> hmSockets = null;

//	private HashMap<String, ArrayList<FriendMsgVO>> hmMyChat = null;
//	private ArrayList<FriendMsgVO> liMyChat = null;
	private ArrayList<String> liFriend = null;

	private ServerMainModel model = null;
	private ArrayList<Room> roomList = null;

	public ServerThread() {
	}

//	public ServerThread(Socket s, HashMap<String, ObjectOutputStream> h, ObjectInputStream ois, ObjectOutputStream oos,
//			ArrayList<String> liFriend, ServerMainModel m) {
//
//		this.socket = s;
//		this.hmUsers = h;
//		this.ois = ois;
//		this.oos = oos;
////		this.hmMyChat = hmMyChat;
////		this.liMyChat = liMyChat;
//		this.liFriend = liFriend;
//		this.model = m;
//
//		ServerLog.login_sb
//				.append(this.socket.getInetAddress().getHostAddress() + "님에 대응하는 스레드가 생성었습니다. - " + new Date() + "\n");
//
//	}

	public ServerThread(Socket s, HashMap<String, ObjectOutputStream> h, ObjectInputStream ois, ObjectOutputStream oos,
			ArrayList<String> liFriend, ServerMainModel m, HashMap<String, Socket> hmSockets,
			ArrayList<Room> roomList) {

		this.socket = s;
		this.hmUsers = h;
		this.ois = ois;
		this.oos = oos;
		this.liFriend = liFriend;
		this.model = m;
		this.hmSockets = hmSockets;
		this.roomList = roomList;

		ServerLog.login_sb
				.append(this.socket.getInetAddress().getHostAddress() + "님에 대응하는 스레드가 생성었습니다. - " + new Date() + "\n");

	}

	@Override
	public void run() {
		ServerLog.login_sb
				.append(this.socket.getInetAddress().getHostAddress() + "님에 대응하는 스레드를 시작합니다. - " + new Date() + "\n");
		try {
			while (true) {
				ObjectVO oVo = (ObjectVO) ois.readObject();
				switch (oVo.getProtocol()) {

				case Protocol.SEND_MESSAGE_ROOM:
					Room tRoom = null;
					tRoom = oVo.getRoom();

					System.out.println(tRoom.toString());
					String getMsg_1 = oVo.getMessage().trim();
					String msg_1 = "[ " + tRoom.getUserId() + " ] " + getMsg_1;
					String newMsg = tRoom.getChatText() + msg_1 + ":";
					tRoom.setChatText(newMsg);
					ArrayList<Room> newRoomList = null;
					ArrayList<Room> myNewRoomList = null;
					
					for (String receiverId : tRoom.getUsers()) {
						synchronized (hmUsers) {
							if (hmUsers.get(receiverId) == null) {
								IUserDAO dao1 = UserDAO.getInstance();
								dao1.updateRoomChat(tRoom.getRoomNo(), oVo.getUserId(), newMsg);
								myNewRoomList = dao1.selectRooms(oVo.getUserId());
								oVo.setRoomList(myNewRoomList);
								oVo.setMessage(newMsg);
								model.sendMsg(oVo, oos);

							} else {
								IUserDAO dao2 = UserDAO.getInstance();
								dao2.updateRoomChat(tRoom.getRoomNo(), tRoom.getUserId(), newMsg);
							//나에게
								oVo.setMessage(newMsg);
								myNewRoomList = dao2.selectRooms(oVo.getUserId());
								oVo.setRoomList(myNewRoomList);
								model.sendMsg(oVo, oos);
								
								//친구들에게
								ObjectOutputStream sendOos = hmUsers.get(receiverId);
								newRoomList = dao2.selectRooms(receiverId);
								oVo.setRoomList(newRoomList);
								model.sendMsg(oVo, sendOos);
								

							}
						}
						IUserDAO dao3 = UserDAO.getInstance();
						dao3.updateRoomChat(tRoom.getRoomNo(), tRoom.getUserId(), newMsg);
						dao3.updateRoomChat(tRoom.getRoomNo(), receiverId, newMsg);
					}

					break;

				case Protocol.SEND_MESSAGE:
					ServerLog.login_sb.append(oVo.getUserId() + "님으로부터 보낼 메세지를 수신하였습니다. - " + new Date() + "\n");
					liFriend.clear();
					liFriend.addAll(oVo.getLiFriend());
					if (hmUsers.get(oVo.getReceiver()) == null) {
						IUserDAO dao = UserDAO.getInstance();
						String chatText = dao.selectChatTable(oVo.getUserId(), oVo.getReceiver());
						String getMsg1 = oVo.getMessage().trim();
						String msg = "[ " + oVo.getUserId() + "] " + getMsg1 + ":";
						// 테이블에 등록된 대화내용이 없다면.
						if (chatText.trim().length() <= 0) {
							// 방을 생성.

							// 디비에 유저에2명에관한 데이터와 채팅 내용 저장.
							chatText = chatText + msg;
							model.insertFirstChat(dao, oVo, chatText);
						}

						chatText = chatText + msg;
						model.updateChat(dao, oVo, chatText);
						// 자기자신에게도 상대와 똑같은 메세지화면을 보여줘야 하기 때문에
						// 자신에게도 뿌린다.
						ObjectOutputStream oos1 = hmUsers.get(oVo.getUserId());

						oVo.setMessage(chatText);
						model.sendMsg(oVo, oos1);

					} else {
						IUserDAO dao = UserDAO.getInstance();
						String chatText = dao.selectChatTable(oVo.getUserId(), oVo.getReceiver());

						String msg = "[ " + oVo.getUserId() + "] " + oVo.getMessage() + ":";
						// 테이블에 등록된 대화내용이 없다면.
						if (chatText.trim().length() <= 0) {
							// 방을 생성.

							// 디비에 유저에2명에관한 데이터와 채팅 내용 저장.
							chatText = chatText + msg;
							model.insertFirstChat(dao, oVo, chatText);
						}

						chatText = chatText + msg;
						model.updateChat(dao, oVo, chatText);
						// 자기자신에게도 상대와 똑같은 메세지화면을 보여줘야 하기 때문에
						// 자신에게도 뿌린다.
						ObjectOutputStream oos1 = hmUsers.get(oVo.getUserId());
						ObjectOutputStream oos2 = hmUsers.get(oVo.getReceiver());
						oVo.setMessage(chatText);
						model.sendMsg(oVo, oos1);
						ServerLog.msg_sb.append("[ 메세지 전송 ] " + oVo.getUserId() + " -> " + oVo.getUserId() + "전송 - "
								+ new Date() + "\n");
						model.sendMsg(oVo, oos2);
						ServerLog.msg_sb.append("[ 메세지 전송 ] " + oVo.getUserId() + " -> " + oVo.getReceiver() + "전송 - "
								+ new Date() + "\n");
					}

					break;

				case Protocol.ENTER_ROOM:
					// 유저를 더블클릭 혹은 방을 더블클릭하면 채팅창으로 이동하며 서버로 메세지를 전송하게끔.
					ServerLog.msg_sb.append("[ 알림 ]" + oVo.getUserId() + "이 " + oVo.getReceiver()
							+ "와 대화할 수 있는 방에 입장하였습니다." + new Date() + "\n");
					IUserDAO dao = UserDAO.getInstance();
//					String chatText = dao.selectChatTable(oVo.getUserId(), oVo.getReceiver());
//					String ct = dao.getChatText(oVo.getRoomNo(), oVo.getUserId());
//					Room tRoom2 = null;
//					for (Room tr : oVo.getRoomList()) {
//						if (tr.getRoomNo() == oVo.getRoomNo()) {
//							tRoom2 = tr;
//							
//						}
//					}
//					oVo.setMessage(ct);

//					oVo.setMessage(chatText.toString());
//					oVo.setProtocol(Protocol.ENTER_ROOM);
					System.out.println("ENTER ROOM ROOM NUMBER : " + oVo.getRoomNo());

				case Protocol.CREATE_ROOM:
					System.out.println("Create ROOM!!!!!");
					IUserDAO dao2 = UserDAO.getInstance();
					int rst = dao2.countUser(oVo.getUserId(), oVo.getReceiver());
					System.out.println("rst : " + rst);
					if (oVo.getRoomNo() != 0) {
//						if(rst == 1) {
//							System.out.println("방을 만듭니다.");
//							oVo.setProtocol(Protocol.CREATE_ROOM);
//							Connection conn = JDBCUtil.getConnection();
//							dao2.insertChatRoom(0, oVo.getUserId(), "- Room -:", conn);
//							dao2.insertChatRoom(1, oVo.getReceiver(), "- Room -:", conn);
//							ArrayList<Room> arr = dao2.selectRooms(oVo.getUserId());
//							oVo.setRoomList(arr);
//			
////							for()
//							System.out.println("현재 내가 가진 방들은 ?"+arr);
//							model.sendMsg(oVo, oos);
//						}
						System.out.println("이미방이존재합니다.");
						oVo.setProtocol(Protocol.ENTER_ROOM_ALLOW);
						ArrayList<Room> arr1 = dao2.selectRooms(oVo.getUserId());
						System.err.println("- ENter room - " + arr1);
						System.out.println("arr = " + arr1);
						Room roomMy = null;
						for (Room rr : arr1) {
//							if (rr.getUsers().size() == 1 && rr.getUsers().get(0).equals(oVo.getReceiver())) {
//								oVo.setRoomNo(rr.getRoomNo());
//								roomMy = rr;
//							}
							if(rr.getRoomNo() == oVo.getRoomNo()) {
								roomMy = rr;
							}
						}

						if (roomMy != null) {
							oVo.setRoom(roomMy);
						}
						System.out.println(roomMy + "~~~!~~~~~!!!~~~~~!!!!~~");

						String ct = dao2.getChatText(oVo.getRoomNo(), oVo.getUserId());
						System.out.println("USER ID : =-------- " + oVo.getUserId());
						System.out.println("ROOM NUMBER = " + oVo.getRoomNo());
						System.out.println("chat Text : " + ct + "\n\n");
						oVo.setRoomList(arr1);
						oVo.setMessage(ct);
						model.sendMsg(oVo, oos);

					} else {
						System.out.println("방을 만듭니다.");
						oVo.setProtocol(Protocol.CREATE_ROOM);
						Connection conn = JDBCUtil.getConnection();
						dao2.insertChatRoom(0, oVo.getUserId(), "- Room -:", conn);
						dao2.insertChatRoom(1, oVo.getReceiver(), "- Room -:", conn);
						ArrayList<Room> arr2 = dao2.selectRooms(oVo.getUserId());
						System.err.println("- create room - " + arr2);
						oVo.setRoomList(arr2);

//						for()
						System.out.println("현재 내가 가진 방들은 ?" + arr2);
						model.sendMsg(oVo, oos);
					}
					break;

				case Protocol.CREATE_GROUP_ROOM:

					break;

				case Protocol.INVITE_USER:
					IUserDAO inviteDAO = UserDAO.getInstance();
					String invitedMsg = "[알림]" + oVo.getReceiver() + "님이 초대되었습니다.:";
					inviteDAO.insertInviteUser(oVo.getRoomNo(), oVo.getReceiver());
					ArrayList<Room> newInvitedRoom = inviteDAO.selectRooms(oVo.getUserId());
					oVo.setRoomList(newInvitedRoom);

					oos.writeObject(oVo);
					oos.flush();

					// 해당방의 모든 유저에게 뿌려야함.새로운 방제목을.
					ArrayList<String> userList = inviteDAO.selectRoomUser(oVo.getRoomNo());
					for (String tuser : userList) {
						if (hmUsers.get(tuser) != null) {
							ArrayList<Room> tempInvitedRoom = inviteDAO.selectRooms(tuser);
							oVo.setProtocol(Protocol.UPDATE_CHAT_TITLE);
							ObjectOutputStream tempOut = hmUsers.get(tuser);
							oVo.setUserId(tuser);
							oVo.setRoomList(tempInvitedRoom);
							tempOut.writeObject(oVo);
							tempOut.flush();
						}
					}	

					break;

				case Protocol.EXIT_ROOM:
					IUserDAO exitRoomDAO = UserDAO.getInstance();
					exitRoomDAO.deleteRoomUser(oVo.getRoomNo(), oVo.getUserId());
					if (oVo.getReceiver() != null) {
						exitRoomDAO.deleteRoomUser(oVo.getRoomNo(), oVo.getReceiver());
						// 본인의 새로운 방리스트.
						ArrayList<Room> newMyRoomList = exitRoomDAO.selectRooms(oVo.getUserId());
						// 삭제된 친구의 새로운 방리스트.
						ArrayList<Room> newFriendRoomList = exitRoomDAO.selectRooms(oVo.getReceiver());
						oVo.setProtocol(Protocol.EXIT_ROOM_DONE);

						if (hmUsers.get(oVo.getReceiver()) != null) {
							oVo.setRoomList(newFriendRoomList);
							ObjectOutputStream exitRoomOos = hmUsers.get(oVo.getReceiver());
							exitRoomOos.writeObject(oVo);
							exitRoomOos.flush();
						}

						oVo.setRoomList(newMyRoomList);
						oos.writeObject(oVo);
						oos.flush();
					} else {
						for (String user : oVo.getRoom().getUsers()) {
							ArrayList<Room> newRoomli = exitRoomDAO.selectRooms(user);
							oVo.setRoomList(newRoomli);
							synchronized (hmUsers) {
								if (hmUsers.get(user) != null) {
									ObjectOutputStream exitRoomOos2 = hmUsers.get(oVo.getReceiver());
									exitRoomOos2.writeObject(oVo);
									exitRoomOos2.flush();
								}
							}
						}

					}

					break;

				case Protocol.UPDATE_USERINFO:
					model.updateUser(oVo);
					break;

				case Protocol.DELETE_USER:
					ServerMainModel.getInstance().deleteUser(oVo);

					synchronized (hmUsers) {
						hmUsers.remove(oVo.getUserId());
						ServerLog.login_sb.append(
								"[ 회워탈퇴 알림 ] [ " + oVo.getUserId() + " ]" + " 님이 탈퇴 하셨습니다. - " + new Date() + "\n");
						socket.close();
					}
					break;

				case Protocol.DELETE_FRIENDS:
					IUserDAO deleteDAO = UserDAO.getInstance();
					deleteDAO.deleteFriendTable(oVo.getUserId(), oVo.getReceiver());
					deleteDAO.deleteFriendTable(oVo.getReceiver(), oVo.getUserId());
					oVo.setProtocol(Protocol.DELETE_DONE);
					ArrayList<String> afterDelete = deleteDAO.selectFriendTable(oVo.getUserId());
					System.out.println("AFTER DELETE : " + afterDelete);
					if (hmUsers.get(oVo.getReceiver()) != null) {
						ArrayList<String> afterDeleteFri = deleteDAO.selectFriendTable(oVo.getReceiver());
						oVo.setLiFriend(afterDeleteFri);
						ObjectOutputStream deleteOos = hmUsers.get(oVo.getReceiver());
						deleteOos.writeObject(oVo);
						deleteOos.flush();
					}
					oVo.setLiFriend(afterDelete);
					oos.writeObject(oVo);
					oos.flush();
					break;

				case Protocol.ID_CHECK:
					ObjectVO tempVo = ServerMainModel.getInstance().idcheck(oVo);
					ServerMainModel.getInstance().sendMsg(tempVo, oos);
					break;

				// 친구추가.
				case Protocol.GET_UESRINFO:
					ObjectVO tvo = model.findFriend(oVo);

					oos.writeObject(tvo);
					oos.flush();
					break;

				// 파일전송
				case Protocol.SEND_FILE_REQUEST:
					System.out.println(oVo.getUserId() + "로 부터 파일 전송 확인 요청이 들어왔습니다.");
					ObjectVO sendFileObj = null;
					if (hmUsers.get(oVo.getReceiver()) == null) {
						sendFileObj = new ObjectVO(Protocol.SEND_FILE_FAIL, oVo.getUserId(), oVo.getReceiver(),
								"유저를 찾을 수 없습니다");
						model.sendMsg(sendFileObj, oos);
					} else {
						sendFileObj = new ObjectVO(Protocol.SEND_FILE_REQUEST, oVo.getUserId(), oVo.getReceiver(),
								null);
						sendFileObj.setFileName(oVo.getFileName());
						System.out.println(sendFileObj.toString());
						model.sendMsg(sendFileObj, hmUsers.get(oVo.getReceiver()));
					}

					break;

				case Protocol.CLEAR_CHAT_TEXT:
					dao = UserDAO.getInstance();
//					dao.updateChatTable(oVo.getUserId(), oVo.getReceiver(), "- CLEAR -:");
					dao.updateRoomChat(oVo.getRoomNo(), oVo.getUserId(), "- CLEAR -:");
					String newTxt = dao.getChatText(oVo.getRoomNo(), oVo.getUserId());
					oVo.setMessage(newTxt);

					for (Room teR : oVo.getRoomList()) {
						if (teR.getRoomNo() == oVo.getRoomNo()) {
							teR.setChatText(newTxt);
						}
					}

					// 자신에게도 뿌린다.
					oVo.setMessage("- CLEAR -:");
					model.sendMsg(oVo, oos);
					ServerLog.msg_sb.append("[ 메세지 전송 - 채팅창 클리어 ] " + oVo.getUserId() + " -> " + oVo.getUserId()
							+ "전송 - " + new Date() + "\n");
					break;

				case Protocol.SEND_FILE_RESPONSE_OK:
					System.out.println(oVo.getUserId() + "님이 파일요청을 승낙했습니다.");
					ObjectVO okFile = new ObjectVO(Protocol.SEND_FILE_RESPONSE_OK, oVo.getUserId(), oVo.getReceiver(),
							null);
					okFile.setIpAddr(this.socket.getInetAddress().getHostAddress());
					okFile.setPort(9865);
					ObjectOutputStream okOos = hmUsers.get(oVo.getReceiver());
					okOos.writeObject(okFile);
					okOos.flush();
					break;
				case Protocol.SEND_FILE_RESPONSE_NO:
					System.out.println(oVo.getUserId() + "님이 파일요청을 거절했습니다.");
					break;
				case Protocol.LOGOUT:
					synchronized (hmUsers) {
						hmUsers.remove(oVo.getUserId());
						ServerLog.login_sb.append(
								"[ 로그아웃 알림 ] [ " + oVo.getUserId() + " ]" + " 님이 로그아웃하셨습니다. - " + new Date() + "\n");
						socket.close();
						return;
					}

				default:
					break;

				}
			}

		} catch (ClassNotFoundException e) {
			ServerLog.msg_sb.append("[ 알림 ] 해당하는 파일을 찾을 수 없습니다. - ServerThread - " + new Date() + "\n");
			e.printStackTrace();
		} catch (IOException e) {
			ServerLog.msg_sb.append("[ 알림 ] 전송하고 읽어오는 과정에서 문제가 발생했습니다. - ServerThread - " + new Date() + "\n");
			e.printStackTrace();
		}

	}
}
