package Pack;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

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



public class Server extends Application {

	ServerSocket serverSocket;
	Vector<Socket> sockets = new Vector<Socket>();
	Socket socket;
	TextArea textArea;

	//서버를 시작하는 함수
	public void startServer(String IP, int port) {
		
		System.out.println("[서버 Start]");
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
			
		} catch (Exception e) {
			if(!serverSocket.isClosed()) {
				stopServer(socket);
			}
			return;
		}

		//클라이언트 접속 기다리는 쓰레드
		Thread connetThread = new Thread() {

			public void run() {
				System.out.println("[클라이언트 접속 대기중]");
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						sockets.add(socket);
						receive(socket);
						
						System.out.print("[클라이언트 접속]" 
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName()+"\n");
						
					}catch(Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer(socket);
						}
						break;
					}
				}
			}
		};
		connetThread.start();
		
	}

	//서버를 멈추는 함수
	public void stopServer(Socket socket) {
		
		System.out.println("[서버 Stop]");
		
		try {
			// 현재 작동 중인 모든 소켓 닫기
			Iterator<Socket> iterator = sockets.iterator();
			while(iterator.hasNext()) {
				Socket socket1 = iterator.next();
				socket1.close();
				iterator.remove();
				System.out.println("[소켓 Remove : "+ socket1 +"]");
			}
			
			// 서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				System.out.println("[서버 소켓 Close]");
			}
			
		}catch (Exception e){
		}
	}
	

	// 클라이언트로부터 채팅을 받아오는 함수
	public void receive(Socket socket) {
		
		
		Thread receiveThread = new Thread() {
				
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] data = new byte[512];
						int size = in.read(data);

						System.out.println("[메세지 수신 성공]" 
								+ socket.getRemoteSocketAddress()
								+ ":" + Thread.currentThread().getName()+"\n");

						String msg = new String(data, 0, size);

						send(msg, socket);

					}
				} catch (Exception e) {
					try {
						System.out.println("[메세지 수신 오류] "
								+ socket.getRemoteSocketAddress()
								+ ":  " + Thread.currentThread().getName()+"\n");
						sockets.remove(socket);
						socket.close();
					}catch(Exception e2)
					{
					}
				}
			}
		};
		receiveThread.start();
		
	}


	// 클라이언트에게 채팅을 보내는 함수
	public void send(String message, Socket socket) {

		Thread sendThread = new Thread() {
			public void run() {
				textArea.appendText(message);
				for (Socket socket:sockets) {
					try {

						OutputStream out = socket.getOutputStream();
						byte[] data = message.getBytes();
						out.write(data);
						out.flush();
					}

					catch(Exception e){
						try {
							System.out.println("[메세지 송신 오류] ");
							sockets.remove(socket);
							socket.close();
						}catch(Exception e2) {
						}				
					}
				}
			}
		};
		sendThread.start();
		
	}

	public void start(Stage arg0) throws Exception {

		VBox root = new VBox();
		root.setPrefSize(400, 250);
		root.setSpacing(5);
		
		

		TextField IP = new TextField();
		IP.setText("220.119.14.217");
//		IP.setText("192.168.4.111");
		TextField port = new TextField();
		port.setText("5001");
		
		int portnum = Integer.parseInt(port.getText());
		String IPnum = IP.getText();
		
		
		textArea = new TextArea();
		textArea.setEditable(false);
		
		Button sendButton = new Button("Send");
		sendButton.setDisable(true);
		
		TextField input = new TextField();
		input.setDisable(true);

		
		Button startButton = new Button("Server Start");


		startButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if(startButton.getText().equals("Server Start")) {
					startServer(IPnum, portnum);
					Platform.runLater(()-> {
						String message = String.format("[Server Start]\n",IPnum, portnum);
						textArea.appendText(message);
						startButton.setText("Server Stop");
					});
					IP.setDisable(true);
					port.setDisable(true);
					input.setDisable(false);
					sendButton.setDisable(false);
				} 
				else {
					stopServer(socket);
					Platform.runLater(()-> {
						String message = String.format("[Server Stop]\n",IPnum, portnum);
						textArea.appendText(message);
						startButton.setText("Server Start");
					});
					IP.setDisable(false);
					port.setDisable(false);
					input.setDisable(true);
					sendButton.setDisable(true);
				}
			}
		});

		input.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				send("Server: "+ input.getText() + "\n", socket);
				input.setText("");
			}
		});

		sendButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				send("Server: "+ input.getText() + "\n", socket);
				input.setText("");
			}
		});
		
		BorderPane pane1 = new BorderPane();
		pane1.setLeft(IP);
		pane1.setCenter(port);
		pane1.setRight(startButton);
		
		BorderPane pane2 = new BorderPane();
		pane2.setCenter(input);
		pane2.setRight(sendButton);
		
		
		root.getChildren().addAll(pane1,textArea,pane2);

		//------------------------------------------------
		Scene scene = new Scene(root);
		arg0.setOnCloseRequest(event -> stopServer(socket));
		arg0.setTitle("Server");
		arg0.setScene(scene);
		arg0.show();


	}

	public static void main(String[] args) {
		launch(args);
	}

}