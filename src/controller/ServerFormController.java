package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import Thread.Thread1;
import java.net.Socket;
import java.util.HashMap;
import java.util.Timer;

public class ServerFormController {

    public TextField txtFldUserName;
    public AnchorPane root;

    Stage stage;
    Server server;
    Thread mainThread;
    Socket localSocket;
    public static HashMap<Integer, DataOutputStream> clients = new HashMap<>();

     public void initialize() throws IOException {
        //open up the server
         server = new Server(8080,5);

         mainThread = new Thread(() -> {
             // always waits for a client's request
             while (true) {
                 try {
                     localSocket = server.accept();
                     Timer timer = new Timer();
                     timer.schedule(new Thread1(new DataInputStream(localSocket.getInputStream()),timer),0,2000);
                     clients.put(localSocket.getPort(), new DataOutputStream(localSocket.getOutputStream()));
                 } catch (IOException e) {
                     System.out.println(e.getMessage());
                 }
             }
         });

         mainThread.start();

         Platform.runLater(() -> {
             stage.setOnCloseRequest(e -> {
                 mainThread.interrupt();

                 System.exit(0);
             });
         });
    }

    public void loginOnAction(ActionEvent actionEvent) throws IOException {
        // opening chat-area for client
        Stage clientStage = new Stage(StageStyle.DECORATED);
        FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("view/ClientForm.fxml"));
        Scene client = new Scene(loader.load());

        clientStage.setScene(client);
        clientStage.setTitle("User : " + txtFldUserName.getText());
        clientStage.setResizable(false);
        clientStage.sizeToScene();

        // passing data via the controller
        try {
            ClientFormController controller = loader.getController();
            controller.initData(txtFldUserName.getText(),clientStage);
        }catch (NullPointerException e){
            System.out.println(e.getMessage());
        }

        clientStage.show();
        txtFldUserName.clear();
    }

    public void initData (Stage stage) {
        this.stage = stage;
    }
}
