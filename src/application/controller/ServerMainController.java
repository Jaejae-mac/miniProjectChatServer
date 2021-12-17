package application.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import application.controller.therad.ServerThread;
import application.jdbc.repository.dao.IUserDAO;
import application.jdbc.repository.dao.UserDAO;
import application.protocol.Protocol;
//import application.protocol.vo.FriendMsgVO;
import application.protocol.vo.ObjectVO;
import application.protocol.vo.Room;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ServerMainController implements Initializable {
	@FXML
	private VBox fx_serverViewVBox;
	@FXML
	private TabPane fx_serverTab;
	@FXML
	private Tab fx_currentUserListTab;
	@FXML
	private ListView<String> fx_currentUserList;
	@FXML
	private Tab fx_loginOutTab;
	@FXML
	private Tab fx_messageTab;
	@FXML
	private TextArea fx_loginOutTextArea;
	@FXML
	private TextArea fx_messageTextArea;
	@FXML
	private Button fx_logoutBtn;
	@FXML
	private Tab fx_dbTab;
	@FXML
	private TextArea fx_dbTextArea;

	private int port = 7777;
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	private ServerMainModel model = null;

	private HashMap<String, ObjectOutputStream> hmUsers = null; // 현재 접속자 들을 관리할 해쉬맵.
	private HashMap<String, Socket> hmSockets = null;
//	private HashMap<String, ArrayList<FriendMsgVO>> hmMyChat = null; // 나 자신의 아이디를 키값으로 하고 밸류로 대화 대화목록을 가지고 있는다.
//	private ArrayList<FriendMsgVO> liMyChat = null; // 유저 1명이 가지는 대화목록들

	public ObservableList<String> user_list = FXCollections.observableArrayList();
	private static int iThreadCnt = 1;

	private StringBuilder sbMsg = null;

	private ServerThread st = null;
	private Thread t = null;

	private ContextMenu contextMenu;

	private String currentClickedId;
	
	
	public void setHm(HashMap<String, ObjectOutputStream> hmUsers, HashMap<String, Socket> hmSockets) {
		this.hmUsers = hmUsers;
		this.hmSockets = hmSockets;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		contextMenu = new ContextMenu();
		contextMenuView();
		userListAction();

		try {
			serverSocket = new ServerSocket(port);
//			hmUsers = new HashMap<String, ObjectOutputStream>();
//			hmSockets = new HashMap<String, Socket>();
//			hmMyChat = new HashMap<String, ArrayList<FriendMsgVO>>();
//			liMyChat = new ArrayList<FriendMsgVO>();
			model = ServerMainModel.getInstance();

			sbMsg = new StringBuilder();
			sbMsg.append("************** 서버 가동 시작 **************\n");
			sbMsg.append("********* 클라이언트 접속 대기중 *********\n");
			sbMsg.append("*********************************************\n");
			fx_loginOutTextArea.setText(sbMsg.toString());

			model.setHm(hmUsers);

			Thread thread = new Thread() {

				@Override
				public void run() {
					try {
						while (true) {
							socket = serverSocket.accept();

							OutputStream os = socket.getOutputStream();
							oos = new ObjectOutputStream(os);
							InputStream is = socket.getInputStream();
							ois = new ObjectInputStream(is);

							InetAddress inetAddr = socket.getInetAddress();
							sbMsg.append(inetAddr.getHostAddress() + "에서 접속을 하였습니다.\n");
							fx_loginOutTextArea.setText(sbMsg.toString());

							ObjectVO vo = null;
							try {
								vo = (ObjectVO) ois.readObject();
								setMsgTextArea(sbMsg, "ServerMainController[성공] - 객체를 읽어왔습니다.\n");
								ServerLog.login_sb.append("ServerMainController[성공] - 객체를 읽어왔습니다.\n");

								System.err.println(vo.toString());
							} catch (ClassNotFoundException e) {
								setMsgTextArea(sbMsg, "ServerMainController[에러] - 객체를 읽어오는 중에 에러 발생.\n");

								e.printStackTrace();
							}

							ObjectVO tvo = null;
							switch (vo.getProtocol()) {

							case Protocol.ID_CHECK: // 회원가입시 아이디 중복확인용.
								tvo = model.idcheck(vo);
								System.err.println(tvo.toString());
								// setMsgTextArea(sbMsg, tvo.getNoticeMsg());
								oos.writeObject(tvo);
								oos.flush();
								setMsgTextArea(sbMsg, "객체 데이터를 전달 하였습니다.\n");
								break;

							case Protocol.LOGIN:
								ServerLog.login_sb.append(vo.getUserId() + "님이 로그인을 시도했습니다. - " + new Date() + "\n");

								tvo = model.login(vo);
								if (tvo.getProtocol().equals(Protocol.LOGIN_SUCCESS)) {
									synchronized (hmUsers) {
										hmUsers.put(tvo.getUserId(), oos);
										fx_currentUserList.setItems(user_list);
										hmSockets.put(tvo.getUserId(), socket);
									}

									setMsgTextArea(sbMsg, tvo.getNoticeMsg());
									ArrayList<String> liFriend = model.getFriendList(tvo.getUserId());
									
									IUserDAO dao = UserDAO.getInstance();
									ArrayList<Room> roomList = dao.selectRooms(vo.getUserId());
									
									// 다시 클라이언트로 객체를 전달하기 전에 친구목록을 객체에 추가해준다.
									tvo.setLiFriend(liFriend);
									tvo.setRoomList(roomList);
									
									st = new ServerThread(socket, hmUsers, ois, oos, liFriend, model, hmSockets, roomList);
									t = new Thread(st);
									t.setName("UserNo-" + iThreadCnt);
									t.start();
									iThreadCnt++;
									tvo.setLiFriend(liFriend);
								}
								oos.writeObject(tvo);
								oos.flush();
								setMsgTextArea(sbMsg, "사용자에게 객체 데이터를 전달 하였습니다.\n");
								break;

							case Protocol.SIGNUP:
								tvo = model.signUp(vo);
								if (tvo.getProtocol().equals(Protocol.SIGNUP_SUCCESS)) {
									setMsgTextArea(sbMsg, tvo.getNoticeMsg());
								} else {
									ServerLog.login_sb.append("회원가입중 오류가 발생하여 회원가입에 실패하였습니다." + new Date() + "\n");
								}
								break;

							}

						}

					} catch (IOException e) {
						setMsgTextArea(sbMsg, "데이터를 클라이언트로 보내는 중에 문제가 발생하였습니다.\n");
						e.printStackTrace();
					}

				}
			};

			thread.setDaemon(true);
			thread.start();

			Thread t2 = new Thread() {
				@Override
				public void run() {
					Platform.runLater(() -> {
						fx_currentUserList.setItems(user_list);

					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@FXML
	public void fxLogOut(ActionEvent e) {
		if (AlertController.alertCon(AlertType.CONFIRMATION, "서버 종료", "서버 종료", "서버를 종료하시겠습니까?")) {
			Set<String> set = hmUsers.keySet();
			Iterator<String> it = set.iterator();

			while (it.hasNext()) {
				String key = it.next();
				synchronized (hmUsers) {
					ObjectVO tempVo = new ObjectVO(Protocol.LOGOUT, key);
					ObjectOutputStream oos = hmUsers.get(key);
					try {
						oos.writeObject(tempVo);
						oos.flush();
					} catch (IOException ioe) {
						// TODO Auto-generated catch block
						ioe.printStackTrace();
					}
				}

			}

			Platform.exit();
			System.exit(0);

		}
	}


	// 2초에 한번씩 서버의 로그를 업데이트 시켜 GUI에 프린트한다.
	public void setMsgTextArea(StringBuilder sb, String text) {
		sb.append(text + "\n");
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (true) {
					Platform.runLater(() -> {

						fx_loginOutTextArea.setText(ServerLog.login_sb.toString());
						fx_dbTextArea.setText(ServerLog.db_sb.toString());
						fx_messageTextArea.setText(ServerLog.msg_sb.toString());

						Set<String> userKeys = hmUsers.keySet();
						user_list.clear();
						for (String k : userKeys) {
							user_list.add(k);
						}
						user_list.sort(Comparator.naturalOrder());
						fx_currentUserList.setItems(user_list);
					});
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	public void contextMenuView() {
		MenuItem menuItem1 = new MenuItem("로그아웃(관리자 권한)");
		MenuItem menuItem2 = new MenuItem("닫기");
		menuItem1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String user = fx_currentUserList.getSelectionModel().getSelectedItem();
				System.out.println("clicked User : " + user);
				if (currentClickedId != null) {
					ObjectOutputStream tempOos = hmUsers.get(currentClickedId);
					ObjectVO tempVo = new ObjectVO(Protocol.LOGOUT, currentClickedId);
					try {
						tempOos.writeObject(tempVo);
						tempOos.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					synchronized (hmUsers) {
						hmUsers.remove(user);
						hmSockets.remove(user);
						ServerLog.login_sb.append("[ 관리자 권한 ] 관리자 권한으로 Client " + user + "를 강제 로그아웃 시켰습니다.");

					}
				}

			}

		});

		fx_currentUserList.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

			@Override
			public void handle(ContextMenuEvent event) {
				contextMenu.show(fx_currentUserList, event.getScreenX(), event.getScreenY());
			}

		});

		contextMenu.getItems().add(menuItem1);
		contextMenu.getItems().add(menuItem2);
	}

	public void userListAction() {
		fx_currentUserList.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() == 1) {
					if (fx_currentUserList.getSelectionModel() != null) {
						currentClickedId = fx_currentUserList.getSelectionModel().getSelectedItem();
						if (currentClickedId == null) {
							contextMenu.hide();
						}
						return;
					}

				}

			}
		});
	}

}
