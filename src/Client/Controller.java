package Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class Controller {

    @FXML
    public Pane SignUpPane;

    @FXML
    public Pane LogInPane;
    @FXML
    public ImageView su2Back;
    @FXML
    public Button btnSignUP;
    @FXML
    public Button su2lgIn;
    @FXML
    public javafx.scene.control.TextField suUserID;
    @FXML
    public javafx.scene.control.TextField suPass;
    @FXML
    public javafx.scene.control.TextField suFullName;
    @FXML
    public javafx.scene.control.TextField suPhNo;
    @FXML
    public RadioButton male;
    @FXML
    public RadioButton female;
    @FXML
    public javafx.scene.control.Label controlSuLabel;
    @FXML
    public javafx.scene.control.Label successReg;
    @FXML
    public javafx.scene.control.Label goBack;
    @FXML
    public javafx.scene.control.TextField userName;
    @FXML
    public PasswordField passPhrase;
    @FXML
    public javafx.scene.control.Label loginError;
    @FXML
    public javafx.scene.control.Label invalidUID;
    public static String userID, password, gender;
    public static ArrayList<Client> loggedInClients = new ArrayList<>();
    public static ArrayList<Client> clients = new ArrayList<Client>();

    public void registration(ActionEvent actionEvent) {
        if (!suUserID.getText().equalsIgnoreCase("")
                && !suPass.getText().equalsIgnoreCase("")
                && !suFullName.getText().equalsIgnoreCase("")
                && !suPhNo.getText().equalsIgnoreCase("")
                && (male.isSelected() || female.isSelected())) {
            if (checkClient(suUserID.getText())) {
                Client newUser = new Client();
                newUser.userID = suUserID.getText();
                newUser.password = suPass.getText();
                newUser.fullName = suFullName.getText();
                newUser.phoneNo = suPhNo.getText();
                if (male.isSelected()) {
                    newUser.gender = "Male";
                } else {
                    newUser.gender = "Female";
                }
                clients.add(newUser);
                goBack.setOpacity(1);
                successReg.setOpacity(1);
                makeDefault();
                if (loginError.getOpacity() == 1) {
                    loginError.setOpacity(0);
                }
                if (invalidUID.getOpacity() == 1) {
                    invalidUID.setOpacity(0);
                }
            } else {
                invalidUID.setOpacity(1);
                setOpacity(successReg, goBack, loginError);
            }
        } else {
            loginError.setOpacity(1);
            setOpacity(successReg, goBack, invalidUID);
        }
    }

    private void setOpacity(Label successReg, Label goBack, Label error) {
        successReg.setOpacity(0);
        goBack.setOpacity(0);
        error.setOpacity(0);
    }

    private void makeDefault() {
        suUserID.setText("");
        suPass.setText("");
        suFullName.setText("");
        suPhNo.setText("");
        male.setSelected(true);
        setOpacity(successReg, invalidUID);

    }

    private void setOpacity(Label successReg, Label invalidUID) {
        successReg.setOpacity(0);
        invalidUID.setOpacity(0);
    }

    private boolean checkClient(String uID) {
        for (Client client : clients) {
            if (client.userID.equalsIgnoreCase(uID)) {
                return false;
            }
        }
        return true;
    }

    @FXML
    public void handleButtonAction(ActionEvent actionEvent) {
        if (actionEvent.getSource().equals(btnSignUP)) {
            SignUpPane.toFront();
        }
        if (actionEvent.getSource().equals(su2lgIn)) {
            LogInPane.toFront();
        }
        loginError.setOpacity(0);
        userName.setText("");
        passPhrase.setText("");
    }

    @FXML
    public void handleMouseEvent(javafx.scene.input.MouseEvent mouseEvent) {
        if (mouseEvent.getSource() == su2Back) {
            LogInPane.toFront();
        }
        suUserID.setText("");
        suPass.setText("");
        suPhNo.setText("");
        suFullName.setText("");

    }

    public void login(ActionEvent actionEvent) {
        userID = userName.getText();
        password = passPhrase.getText();
        boolean login = false;
        for (Client x : clients) {
            if (x.userID.equalsIgnoreCase(userID) && x.password.equalsIgnoreCase(password)) {
                login = true;
                loggedInClients.add(x);
                System.out.println(x.fullName);
                gender = x.gender;
                break;
            }
        }
        if (login) {
            changeUI();
        } else {
            loginError.setOpacity(1);
        }
    }

    private void changeUI() {
        try {
            Stage stage = (Stage) userName.getScene().getWindow();
            Parent root = FXMLLoader.load(this.getClass().getResource("Tori.fxml"));
            stage.setScene(new Scene(root, 700, 500));
            stage.setTitle(userID + "");
            stage.setOnCloseRequest(event -> {
                System.exit(0);
            });
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
