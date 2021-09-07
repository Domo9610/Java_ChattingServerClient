package Pack;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Client extends Application {

	Socket socket = new Socket();
	TextArea textArea;

	//클라이언트 동작 메소드
	
	public void startClient(String IP, int port) {
		
		Thread startThread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
				}catch (Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
					}
					return;
				}
			}
		};
		
		startThread.start();
		
	}
	
	
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				Platform.exit();
				socket.close();
			}
		}catch (Exception e ) {
			e.printStackTrace();
		}
	}

	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] data = new byte[512];
				int size = in.read(data);
				
				String message = new String(data, 0, size);
				Platform.runLater(()-> {
					textArea.appendText(message);
				});
	
			}catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	public void send(String message) {
		try{
			//OutStream을 이용해 데이터 전송
			OutputStream out = socket.getOutputStream();
			byte[] data = message.getBytes();
			out.write(data);
			out.flush();
			
		}catch (Exception e) {
			stopClient();
		}
	}

	
	

	public void start(Stage arg0) throws Exception {

		VBox root = new VBox();
		root.setPrefSize(400, 250);
		root.setSpacing(5);

		TextField username = new TextField();
		username.setText("닉네임을 입력하세요.");
		
		TextField IP = new TextField("220.119.14.217");
		TextField Port = new TextField("5001");
		Port.setPrefWidth(70);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		
		Button sendButton = new Button("Send");
		sendButton.setDisable(true);
		
		TextField input = new TextField();
		input.setDisable(true);
		
		Button connectButton = new Button("Connect") ;
		
		input.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				send(username.getText()+": "+ input.getText() + "\n");
				input.setText("");
			}
		});

		sendButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				send(username.getText()+": "+ input.getText() + "\n");
				input.setText("");
			}
		});
		
		connectButton.setOnAction(new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent arg0) {
			if(connectButton.getText().equals("Connect")) {
				int Portnum = 0;
				try {
					Portnum = Integer.parseInt(Port.getText());
					socket.connect(new InetSocketAddress(IP.getText(), Portnum));
				} catch(Exception e) {
					e.printStackTrace();
				}
				startClient(IP.getText(),Portnum);
				send(username.getText()+"님이 입장하셨습니다.\n");
		
				connectButton.setText("DisConnect");
				
				username.setDisable(true);
				IP.setDisable(true);
				Port.setDisable(true);
				input.setDisable(false);
				sendButton.setDisable(false);
			} else {
				send(username.getText()+"님이 퇴장하셨습니다.\n");
				stopClient();
			
				connectButton.setText("Connect");
				
				username.setDisable(false);
				IP.setDisable(false);
				Port.setDisable(false);
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		}
		});
		
		BorderPane pane1 = new BorderPane();
		pane1.setLeft(username);
		pane1.setCenter(IP);
		pane1.setRight(Port);
		
		BorderPane pane2 = new BorderPane();
		pane2.setLeft(connectButton);
		pane2.setCenter(input);
		pane2.setRight(sendButton);

		root.getChildren().addAll(pane1,textArea,pane2);

		//------------------------------------------------
		Scene scene = new Scene(root);
		arg0.setOnCloseRequest(event -> stopClient());
		arg0.setTitle("Client");
		arg0.setScene(scene);
		arg0.show();

	}
	
	public static void main(String[] args) {
		launch(); //blocking
	}
	
}
