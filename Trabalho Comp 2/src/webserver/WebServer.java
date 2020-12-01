package webserver;

import java.io.*;
import java.net.*;
import java.lang.Thread;

public class WebServer {
	private static ServerSocket listenSocket;

	public static void main(String args[]) throws Exception {
		try {
			String modo = args[1];
			int porta = args.length == 4 ? Integer.parseInt(args[2]) : 8080;

			listenSocket = new ServerSocket(porta);

			ConexaoMySQL.getConexaoMySQL();

			System.err.println("Servidor rodando...");
			System.err.println("Porta: " + porta);

			while(true){
				switch (modo) {
					case "-f": // Cria Processos
						System.out.print("Não implementado para Java!");
						
						break;
					case "-t": // Cria Threads

						// Cria-se um objeto socket
						Client server = new Client(listenSocket.accept()); 
						
						// Cria-se Threads para realizar novas conexões para cada Client
						Thread clientThread = new Thread(server);

						// Inicia usando o método 'public void run()'
						clientThread.start();
						break;
				}
			}
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}
}
