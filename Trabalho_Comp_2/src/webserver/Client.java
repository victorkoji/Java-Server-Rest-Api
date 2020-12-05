package webserver;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import controllers.ActorController;
import controllers.MovieController;

import java.nio.file.*;


public class Client implements Runnable {
	 
		private Socket connectionSocket;

		/** Inicializa a conexão com o socket **/
		public Client(Socket connec) throws SocketException {
			connec.setSoTimeout(30 * 1000);
			this.connectionSocket = connec;
	    }
	    
	    public void run() {
			String requestMessageLine = null;
			String fileName = null;
			BufferedReader inFromClient = null;
			DataOutputStream outToClient = null;
			
			try {
				while(true){
					// Entrada da informação Client -> Server
					inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					// Saída da informação Server -> Client
					outToClient = new DataOutputStream(connectionSocket.getOutputStream());

					requestMessageLine = inFromClient.readLine();
					
					
					if(requestMessageLine != null) {
						StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);
						String method = tokenizedLine.nextToken();
						
						switch (method) {
							case "GET":
								fileName = tokenizedLine.nextToken();

								if(fileName.equals("/") || fileName.equals(""))
									fileName = "public/";
								else if (fileName.startsWith("/") == true)
									fileName = fileName.substring(1);
								
								try {
									File file = new File(fileName);

									/** Se for um diretório **/
									if (file.isDirectory()) {
										listarItensDiretorio(file, outToClient);
									}
									/** Se não for um diretório e possui o cgi-bin, quer dizer que está tentando executar um programa **/
									else if(file.getPath().indexOf("cgi-bin") != -1){
										executarProgramaCgiBin(file, outToClient);
									}
									/** Se for um arquivo **/
									else if(file.isFile()){
										abrirArquivo(file, outToClient);
									}else {
										
										/** Trata request para o banco **/
										treatmentRequestGet(fileName, outToClient);
									}
									
								}
								catch (IOException e) {
									outToClient.writeBytes("HTTP/1.1 404 File not found\r\n" +
										"Server: FACOMCD-2020/1.0\r\n" +
										"Content-Type: text/plain\r\n" + 
										"Nao pode encontrar essa url\r\n"
									);
								}
								
								break;
							case "POST":
								
								String line;
								while((line = inFromClient.readLine()) != null) {
									String[] teste = line.split("\\r\\n\\r\\n");
									System.out.println(teste[0]);
								}
//								StringBuilder response = new StringBuilder();
//							    String responseLine = null;
//							    while ((responseLine = inFromClient.readLine()) != null) {
//							        response.append(responseLine);
//							        System.out.println(responseLine);
//							    }
//							    
//							    JSONObject json = new JSONObject(response.toString());
//							    System.out.println(json.getString("title"));
		//
//							    System.out.println(response.toString());				
								break;
							case "PUT":
								
								break;
							case "DELETE":
								fileName = tokenizedLine.nextToken();
								fileName = fileName.substring(1);
								treatmentRequestDelete(fileName, outToClient);
								break;
	
							default:
								System.out.println("Bad Request Message");
								break;
						}	
					}
				}
			} catch (SocketTimeoutException ste) {
				try{
					connectionSocket.close();
					System.out.println("Sessão Expirada!");
				}catch (Exception e) {
					System.out.println("Não foi possível fechar!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * O que faz: Essa função irá executar um arquivo cgi e retorna a saida para o client
		 * 
		 * Usado em: 
		 * @param file - Recebe o um objeto da classe File
		 * @param outToClient - Recebe o um objeto da classe DataOutputStream
		 * 
		 */
		private void executarProgramaCgiBin(File file, DataOutputStream outToClient) throws IOException {
			PrintStream output = new PrintStream(connectionSocket.getOutputStream());
			String program = file.getPath(); // Me retorna tudo que está depois do localhost
			String query = "";

			/** Verifica se existe o ponto de interrogação, ou seja, possui parâmetros **/
			if(file.getPath().indexOf('?') != -1){
				String[] path = file.getPath().split("\\?");
				program = path[0]; // Antes do ponto de interrogação
				query = path[1]; // Depois do ponto de interrogação
			}

			ProcessBuilder pb = new ProcessBuilder(program);
			Map<String, String> env = pb.environment();
			env.put("QUERY_STRING", query);

			Process proc = pb.start();		
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			StringBuilder procOutput = new StringBuilder();
			
			String line;
			while ( (line = br.readLine()) != null){
				procOutput.append(line);
			}

			output.println("HTTP/1.0 200 Document Follows\r\n" +
				"Server: FACOMCD-2020/1.0\r\n" +
				"Content-Type: text/html\r\n" + 
				"Content-Length: "+procOutput.toString().length()+"\r\n"
			);

			output.println(procOutput);

			output.flush(); 
			br.close();
		}

		/**
		 * O que faz: Essa função irá abrir um arquivo
		 * 
		 * Usado em: 
		 * @param file - Recebe o um objeto da classe File
		 * @param outToClient - Recebe o um objeto da classe DataOutputStream
		 * 
		 */
		private void abrirArquivo(File file, DataOutputStream outToClient) throws IOException{
			/** Inicializa as variáveis para abrir o arquivo **/
			String fileName = file.getPath();
			int numOfBytes = (int) file.length();
			FileInputStream inFile = new FileInputStream(fileName);
			byte[] fileInBytes = new byte[numOfBytes];
			String contentType = Files.probeContentType(file.toPath());

			inFile.read(fileInBytes);

			outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n" +
				"Server: FACOMCD-2020/1.0\r\n" +
				"Content-Type: "+contentType+"\r\n"
			);

			/** Verifica qual é a extensão do arquivo e coloca o content type de acordo essa extensão. **/
			/** Adiciona o downlaod do arquivo txt **/
			if (fileName.endsWith(".txt") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif") || fileName.endsWith(".png"))
				outToClient.writeBytes("Content-Type: multipart/form-data; boundary=something\r\n");

			/** Adiciona o downlaod do arquivo pdf **/
			if (fileName.endsWith(".pdf"))
				outToClient.writeBytes("Content-Disposition: attachment; filename="+file.getName()+"\r\n");

			outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n\r\n");
			outToClient.write(fileInBytes, 0, numOfBytes);
		}

		/**
		 * O que faz: Essa função lista os itens de um diretório.
		 * 
		 * @param file - Recebe o um objeto da classe File
		 * @param outToClient - Recebe o um objeto da classe DataOutputStream
		 */
		private void listarItensDiretorio(File file, DataOutputStream outToClient) throws IOException{
			String line = "";
			String[] names = file.list();
			File[] caminhos = file.listFiles();

			/** Busca o caminho pai para podemos fazer o botão de voltar. **/
			String pathVoltar = file.getParent();
			if(pathVoltar == null)
				pathVoltar = "./public";
			
			/** Monta a header e o começo do body **/
			line = String.format(
				"<html>" +
				"<head>\r\n" +
				"<title>Linux/kernel/ - Linux Cross Reference - Free Electrons</title>\r\n" +
				"</head>\r\n" +
				"<body>\r\n" +
				"<table>\n" + 
				"<tr><th>Nome</th><th>Tipo</th></tr>\n"
			);
			
			/** Percorre os itens que estão dentro do diretório **/
			for (int i = 0; i < names.length; i++) {
				Path path = caminhos[i].toPath();
				String contentType = Files.probeContentType(path);

				/** Se for nulo, é uma pasta. Se não for, é um tipo de arquivo **/
				if(contentType == null)
					contentType = "Pasta";

				/** Cria os links para a pasta ou arquivos **/
				line += String.format("<tr><td><h2><a href=\"/%s/%s\">%s</a></h2></td><td><h3>%s</h3></td></tr>\n", file, names[i], names[i], contentType);
			}

			/** Adiciona o voltar **/
			line += String.format("<tr><td><h2><a href=\"/%s\">Voltar</a></h2></td></tr>\n", pathVoltar);
			line += String.format("</table>\n");
			line += String.format("</body>\r\n");

			outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n" +
				"Server: FACOMCD-2020/1.0\r\n" +
				"Content-Type: text/html\r\n" +
				"Content-Length: " + line.length() + "\r\n\r\n"
			);
			outToClient.writeBytes(line);
		}
		
		
		private void treatmentRequestGet(String request, DataOutputStream outToClient) throws IOException, Exception {
			
			String[] req = request.split("/");
			
			switch (req[0]) {
				case "actors": {
					ActorController actor = new ActorController();
					
					if(req.length == 2)
						responseJson(actor.getListActors(req[1]), outToClient);
					else
						responseJson(actor.getListActors(), outToClient);
					
					break;
				}
				case "movies": {
					
					MovieController movie = new MovieController();
					
					if(req.length == 3)
						responseJson(movie.getListActorsByMovies(req[2]), outToClient);
					else if(req.length == 2)
						responseJson(movie.getListMovies(req[1]), outToClient);
					else
						responseJson(movie.getListMovies(), outToClient);

					break;
				}
			}
		}
		
		private void treatmentRequestDelete(String request, DataOutputStream outToClient) throws IOException, Exception {
			
			String[] req = request.split("/");
			
			switch (req[0]) {
				case "movies": {
					
					MovieController movie = new MovieController();
					
					if(req.length == 2)
						responseJson(movie.deleteMovie(req[1]), outToClient);
					
					break;
				}
			}
		}
		

		private void responseJson(JSONArray json, DataOutputStream outToClient) throws IOException{
			String line = "";
			
			/** Monta a header e o começo do body **/
			line = String.format(
				"<html>" +
				"<head>\r\n" +
				"<title>Linux/kernel/ - Linux Cross Reference - Free Electrons</title>\r\n" +
				"</head>\r\n" +
				"<body>\r\n"
			);
			
			line += json;

			outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n" +
				"Server: FACOMCD-2020/1.0\r\n" +
				"Content-Type: text/html\r\n" +
				"Content-Length: " + line.length() + "\r\n\r\n"
			);
			outToClient.writeBytes(line);
		}
}
