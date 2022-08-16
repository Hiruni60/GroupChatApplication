package controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang.ArrayUtils;
import util.Client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import Thread.ListenerThread;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Timer;

import static org.apache.commons.lang.ArrayUtils.addAll;

public class ClientFormController {

    public AnchorPane root;

    public BufferedReader reader;
    public PrintWriter writer;
    public Socket socket;
    public Label lblName;
    public TextField txtFld;

    public ScrollPane scrollPane;
    public VBox msgBox;

    // TODO : hv to encapsulate client
    Client client;
    String txtFldUserName;
    ListenerThread listener;
    Stage stage;

    // file handling
    FileChooser fileChooser;

    // data handling
    byte[] payload;
    byte[] header;


    int mouseCounter = 0;


    public void initialize() throws IOException {

        Platform.runLater(() -> {
            // add a listener for scrollbar to be at the end
            msgBox.heightProperty().addListener(observable -> scrollPane.setVvalue(1D));
            stage.setOnCloseRequest(e -> {
                listener.stop();
            });
        });

        Connect();

    }

    private void Connect() throws IOException {
        client = new Client(txtFldUserName,"localhost",8080);

        Timer timer = new Timer();
        fileChooser = new FileChooser();

        // TODO : open up a listener for server
        try {
            listener = new ListenerThread(new DataInputStream(client.getInputStream()),msgBox,timer);
            timer.schedule(listener,0,1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendMsg(String txtFldUserName) throws IOException, InterruptedException {

        // TODO : bypass all the empty spaces after and before
        if (!txtFld.getText().equals("")){
            String msg = txtFldUserName + " : " + txtFld.getText();
            payload = msg.getBytes(StandardCharsets.UTF_16);
            int len = payload.length;

            header = ByteBuffer.allocate(4).putInt(len).array();
            byte[] frame = addAll(header,payload);


            client.getOut().write(0);
            client.getOut().write(frame);
            client.getOut().flush();

           /* Label msgLabel = new Label(txtFld.getText());
            msgLabel.setStyle("-fx-background-color: #778ca3;-fx-text-fill: #a5b1c2");
            HBox box = new HBox();
            box.getChildren().add(msgLabel);
            box.setAlignment(Pos.BASELINE_RIGHT);
            msgBox.getChildren().add(box);*/

            return true;
        } else return false;


    }
    public void initData(String name, Stage stage) {
        this.stage = stage;
        this.txtFldUserName= name;
    }


    public void uploadPhotoOnMouseClicked(MouseEvent event) throws IOException, InterruptedException {
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile!=null) {

            String[] res = selectedFile.getName().split("\\.");

            BufferedImage finalImage = ImageIO.read(selectedFile);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ImageIO.write(finalImage, res[1], bout);

            byte[] temp_payload = txtFldUserName.getBytes(StandardCharsets.UTF_16);
            byte[] temp_header = ByteBuffer.allocate(4).putInt(temp_payload.length).array();

            payload = bout.toByteArray();
            header = ByteBuffer.allocate(4).putInt(payload.length).array();

            byte[] frame = addAll(header,payload);

            String msg = txtFldUserName + " : ";
            byte[] payload2 = msg.getBytes(StandardCharsets.UTF_16);
            int len2 = payload.length;

            byte[] header2 = ByteBuffer.allocate(4).putInt(len2).array();
            byte[] frame2 = addAll(header2,payload2);

            client.getOut().write(0);
            client.getOut().write(frame2);
            client.getOut().flush();

            Thread.sleep(2000);

            client.getOut().write(-1);
            client.getOut().write(frame);
            client.getOut().flush();

           /* Image image = SwingFXUtils.toFXImage(finalImage,null);*/

           /* HBox box = new HBox();
            GridPane pane  = new GridPane();
            pane.getStyleClass().add("custom-image2");
            pane.setStyle("-fx-background-color: #5181b8;");

            ImageView view = new ImageView(image);
            view.setFitHeight(250);
            view.setFitWidth(250);
            view.setSmooth(true);
            view.setPreserveRatio(true);

            pane.getChildren().add(view);

            box.setAlignment(Pos.BASELINE_RIGHT);
            box.getChildren().add(pane);

            msgBox.getChildren().add(box);*/


        }
    }

    public void sendOnMouseClicked(MouseEvent event) throws IOException, InterruptedException {
        if(sendMsg(txtFldUserName)){
            txtFld.clear();
        }
    }
    public void sendOnEnter(KeyEvent keyEvent) throws IOException, InterruptedException {
        if(keyEvent.getCode().equals(KeyCode.ENTER)){
            if(sendMsg(txtFldUserName)){
                txtFld.clear();
            }
        }
    }

}
