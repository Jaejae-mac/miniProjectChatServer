package application;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import application.controller.AlertController;
import application.controller.LoginDialog;
import application.controller.ServerMainController;
import application.protocol.Protocol;
import application.protocol.vo.ObjectVO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;

public class Main extends Application {
	private HashMap<String, ObjectOutputStream> hmUsers = null; // 현재 접속자 들을 관리할 해쉬맵.
	private HashMap<String, Socket> hmSockets = null;
	private Set<String> set = null;
	private Iterator<String> it = null;

	@Override
	public void start(Stage primaryStage) {
		hmUsers = new HashMap<String, ObjectOutputStream>();
		hmSockets = new HashMap<String, Socket>();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				if (AlertController.alertCon(AlertType.CONFIRMATION, "서버 종료", "서버 종료", "서버를 종료하시겠습니까?")) {
					set = hmUsers.keySet();
					it = set.iterator();

					while (it.hasNext()) {
						String key = it.next();
						synchronized (hmUsers) {
							ObjectVO tempVo = new ObjectVO(Protocol.LOGOUT, key);
							ObjectOutputStream oos = hmUsers.get(key);
							try {
								oos.writeObject(tempVo);
								oos.flush();

//										hmUsers.remove(key);
//										hmSockets.remove(key);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}

					Platform.exit();
					System.exit(0);

				} else {
					event.consume();
				}

			}
		});

		try

		{

			LoginDialog ld = new LoginDialog();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/application/files/fxml/serverView.fxml"));
			Parent root = loader.load();

			ServerMainController controller = loader.getController();
			controller.setHm(hmUsers, hmSockets);

			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/application/files/fxml/global.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
//			Parent root = FXMLLoader.load(getClass().getResource("/application/files/fxml/serverView.fxml"));
//			Scene scene = new Scene(root);
//			scene.getStylesheets().add(getClass().getResource("/application/files/fxml/global.css").toExternalForm());
//			primaryStage.setScene(scene);
//			primaryStage.show();
		} catch (

		Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		launch(args);
	}
}
