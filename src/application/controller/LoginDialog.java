package application.controller;

import java.util.Optional;

import application.jdbc.repository.dao.IUserDAO;
import application.jdbc.repository.dao.UserDAO;
import application.jdbc.repository.vo.AdminTableVO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class LoginDialog {
	boolean stop = false;
	

	public LoginDialog() {
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Login Dialog");
		dialog.setHeaderText("Server Login");

		// Set the icon (must be included in the project).
//		dialog.setGraphic(new ImageView(this.getClass().getResource("img/login.png").toString()));

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField username = new TextField();
		username.setPromptText("Username");
		PasswordField password = new PasswordField();
		password.setPromptText("Password");

		grid.add(new Label("Username:"), 0, 0);
		grid.add(username, 1, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(password, 1, 1);

		// Enable/Disable login button depending on whether a username was entered.

		while (!stop) {
			Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
			loginButton.setDisable(true);
			username.textProperty().addListener((observable, oldValue, newValue) -> {
				loginButton.setDisable(newValue.trim().isEmpty());
			});

			dialog.getDialogPane().setContent(grid);

			// Request focus on the username field by default.
			Platform.runLater(() -> username.requestFocus());

			// Convert the result to a username-password-pair when the login button is
			// clicked.
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == loginButtonType) {
					return new Pair<>(username.getText(), password.getText());
				} else {
					System.exit(0);
				}
				return null;
			});

			Optional<Pair<String, String>> result = dialog.showAndWait();

			result.ifPresent(usernamePassword -> {
//				System.out
//						.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());

//				ServerMainModel smm = ServerMainModel.getInstance();
				ServerMainModel smm = ServerMainModel.getInstance();
				IUserDAO dao = UserDAO.getInstance();
				AdminTableVO vo = dao.selectAdmin(String.valueOf(usernamePassword.getKey()));
				boolean flag = false;
				if (vo != null) {
					flag = smm.idAndPwCheck(vo, usernamePassword.getValue().toString());
				}

				if (flag) {
					this.stop = true;
				}
			});
		}

		// Do some validation (using the Java 8 lambda syntax).

	}
}
