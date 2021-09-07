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

	//������ �����ϴ� �Լ�
	public void startServer(String IP, int port) {
		
		System.out.println("[���� Start]");
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
			
		} catch (Exception e) {
			if(!serverSocket.isClosed()) {
				stopServer(socket);
			}
			return;
		}

		//Ŭ���̾�Ʈ ���� ��ٸ��� ������
		Thread connetThread = new Thread() {

			public void run() {
				System.out.println("[Ŭ���̾�Ʈ ���� �����]");
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						sockets.add(socket);
						receive(socket);
						
						System.out.print("[Ŭ���̾�Ʈ ����]" 
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

	//������ ���ߴ� �Լ�
	public void stopServer(Socket socket) {
		
		System.out.println("[���� Stop]");
		
		try {
			// ���� �۵� ���� ��� ���� �ݱ�
			Iterator<Socket> iterator = sockets.iterator();
			while(iterator.hasNext()) {
				Socket socket1 = iterator.next();
				socket1.close();
				iterator.remove();
				System.out.println("[���� Remove : "+ socket1 +"]");
			}
			
			// ���� ���� ��ü �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				System.out.println("[���� ���� Close]");
			}
			
		}catch (Exception e){
		}
	}
	

	// Ŭ���̾�Ʈ�κ��� ä���� �޾ƿ��� �Լ�
	public void receive(Socket socket) {
		
		
		Thread receiveThread = new Thread() {
				
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] data = new byte[512];
						int size = in.read(data);

						System.out.println("[�޼��� ���� ����]" 
								+ socket.getRemoteSocketAddress()
								+ ":" + Thread.currentThread().getName()+"\n");

						String msg = new String(data, 0, size);

						send(msg, socket);

					}
				} catch (Exception e) {
					try {
						System.out.println("[�޼��� ���� ����] "
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


	// Ŭ���̾�Ʈ���� ä���� ������ �Լ�
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
							System.out.println("[�޼��� �۽� ����] ");
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