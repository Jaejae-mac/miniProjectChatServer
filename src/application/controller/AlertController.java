package application.controller;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

public class AlertController {

	public static boolean alertCon(AlertType type, String title, String headerMessage, String contentMessage) {
		Alert alert = null;
		switch (type) {
		case CONFIRMATION:
			alert = new Alert(type);
			alert.setTitle(title);
			alert.setHeaderText(headerMessage);
			alert.setContentText(contentMessage);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				return true;
			} else if(result.get() == ButtonType.CANCEL){
				alert.close();
				return false;
			}

		case ERROR:
			alert = new Alert(type);
			alert.setTitle(title);
			alert.setContentText(contentMessage);
			alert.showAndWait();
			break;

		case INFORMATION:

			break;

		case NONE:
			alert = new Alert(type);
			alert.setTitle(title);
			alert.setContentText(contentMessage);
			alert.show();
			break;
		default:
			break;
		}
		return false;
	}
}
