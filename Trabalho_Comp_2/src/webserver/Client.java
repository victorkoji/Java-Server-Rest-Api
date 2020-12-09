package webserver;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import controllers.ActorController;
import controllers.MovieController;

import java.nio.file.*;


public class Client implements Runnable {
	 
		private Socket connectionSocket;

		/** Inicializa a conex�o com o socket **/
		public Client(Socket connec) throws SocketException {
			connec.setSoTimeout(30 * 1000);
			this.connectionSocket = connec;
	    }
	    
	    public void run() {
			String requestMessageLine = null;
			BufferedReader inFromClient = null;
			DataOutputStream outToClient = null;
			
			try {
				while(true){
					// Entrada da informa��o Client -> Server
					inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					// Sa�da da informa��o Server -> Client
					outToClient = new DataOutputStream(connectionSocket.getOutputStream());

					requestMessageLine = inFromClient.readLine();
					
					
					if(requestMessageLine != null) {
						StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);
						String method = tokenizedLine.nextToken();
						String fileName = tokenizedLine.nextToken();
						StringBuilder requestBuilder = new StringBuilder();
						StringBuilder bodyRequestBuilder = new StringBuilder();
						String line;
						String body;
						JSONObject obj;
						
						switch (method) {
							case "GET":

								if(fileName.equals("/") || fileName.equals(""))
									fileName = "public/";
								else if (fileName.startsWith("/") == true)
									fileName = fileName.substring(1);
								
								try {
									File file = new File(fileName);

									/** Se for um diret�rio **/
									if (file.isDirectory()) {
										listarItensDiretorio(file, outToClient);
									}
									/** Se n�o for um diret�rio e possui o cgi-bin, quer dizer que est� tentando executar um programa **/
									else if(file.getPath().indexOf("cgi-bin") != -1){
										executarProgramaCgiBin(file, outToClient);
									}
									/** Se for um arquivo **/
									else if(file.isFile()){
										abrirArquivo(file, outToClient);
									}else {
										
										/** Trata request para o banco **/
										treatmentRequestGet(fileName, method, outToClient);
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
							case "PUT":
								fileName = fileName.substring(1);
								
								/** Guarda a header da request **/
								while(!(line = inFromClient.readLine()).isEmpty()) {
									requestBuilder.append(line + "\r\n");
								}
								
								/** Pega o body da request**/
								while (inFromClient.ready()) {
									bodyRequestBuilder.append((char) inFromClient.read());
				                }
								body = bodyRequestBuilder.toString();
								obj = new JSONObject(body);
								
								/** Trata request para o banco **/
								treatmentRequestPostPut(fileName, method, obj, outToClient);
								
								break;
							
							case "DELETE":
								fileName = fileName.substring(1);
								treatmentRequestDelete(fileName, method, outToClient);
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
					System.out.println("Sess�o Expirada!");
				}catch (Exception e) {
					System.out.println("N�o foi poss�vel fechar!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * O que faz: Essa fun��o ir� executar um arquivo cgi e retorna a saida para o client
		 * 
		 * Usado em: 
		 * @param file - Recebe o um objeto da classe File
		 * @param outToClient - Recebe o um objeto da classe DataOutputStream
		 * 
		 */
		private void executarProgramaCgiBin(File file, DataOutputStream outToClient) throws IOException {
			PrintStream output = new PrintStream(connectionSocket.getOutputStream());
			String program = file.getPath(); // Me retorna tudo que est� depois do localhost
			String query = "";

			/** Verifica se existe o ponto de interroga��o, ou seja, possui par�metros **/
			if(file.getPath().indexOf('?') != -1){
				String[] path = file.getPath().split("\\?");
				program = path[0]; // Antes do ponto de interroga��o
				query = path[1]; // Depois do ponto de interroga��o
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
		 * O que faz: Essa fun��o ir� abrir um arquivo
		 * 
		 * Usado em: 
		 * @param file - Recebe o um objeto da classe File
		 * @param outToClient - Recebe o um objeto da classe DataOutputStream
		 * 
		 */
		private void abrirArquivo(File file, DataOutputStream outToClient) throws IOException{
			/** Inicializa as vari�veis para abrir o arquivo **/
			String fileName = file.getPath();
			int numOfBytes = (int) file.length();

			byte[] fileInBytes = new byte[numOfBytes];
			String contentType = Files.probeContentType(file.toPath());

			outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n" +
				"Server: FACOMCD-2020/1.0\r\n" +
				"Content-Type: "+contentType+"\r\n"
			);

			/** Verifica qual � a extens�o do arquivo e coloca o content type de acordo essa extens�o. **/
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
		 * O que faz: Essa fun��o lista os itens de um diret�rio.
		 * 
		 * @param file - Recebe o um objeto da classe File
		 * @param outToClient - Recebe o um objeto da classe DataOutputStream
		 */
		private void listarItensDiretorio(File file, DataOutputStream outToClient) throws IOException{
			String line = "";
			String[] names = file.list();
			File[] caminhos = file.listFiles();

			/** Busca o caminho pai para podemos fazer o bot�o de voltar. **/
			String pathVoltar = file.getParent();
			if(pathVoltar == null)
				pathVoltar = "./public";
			
			/** Monta a header e o come�o do body **/
			line = String.format(
				"<html>" +
				"<head>\r\n" +
				"<title>Linux/kernel/ - Linux Cross Reference - Free Electrons</title>\r\n" +
				"</head>\r\n" +
				"<body>\r\n" +
				"<table>\n" + 
				"<tr><th>Nome</th><th>Tipo</th></tr>\n"
			);
			
			/** Percorre os itens que est�o dentro do diret�rio **/
			for (int i = 0; i < names.length; i++) {
				Path path = caminhos[i].toPath();
				String contentType = Files.probeContentType(path);

				/** Se for nulo, � uma pasta. Se n�o for, � um tipo de arquivo **/
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
		
		/** Tratamento do GET**/
		private void treatmentRequestGet(String request, String method, DataOutputStream outToClient) throws IOException, Exception {
			
			int verificador = request.indexOf(".xml");
			boolean textoEspecificado = false;
			
			if(verificador > 0) {
				textoEspecificado = true;
				request = request.replace(".xml", "");
			}
			
			String[] req = request.split("/");
			
			
			switch (req[0]) {
				case "actors": {
					ActorController actor = new ActorController();
					
					if(req.length == 2)
						//Se nao foi especificado, sera um json!
						if(!textoEspecificado)
							responseJson(actor.getListActors(req[1]), outToClient);
						else
							responseXML(actor.getListActors(req[1]), outToClient);
					else
						if(!textoEspecificado)
							responseJson(actor.getListActors(), outToClient);
						else
							responseXML(actor.getListActors(), outToClient);
					
					break;
				}
				case "movies": {
					
					MovieController movie = new MovieController();
					
					if(req.length == 3)
						if(!textoEspecificado)
							responseJson(movie.getListActorsByMovies(req[2]), outToClient);
						else 
							responseXML(movie.getListActorsByMovies(req[2]), outToClient);
					else if(req.length == 2)
						if(!textoEspecificado)							
							responseJson(movie.getListMovies(req[1]), outToClient);
						else 
							responseXML(movie.getListMovies(req[1]), outToClient);
					else
						if(!textoEspecificado)		
							responseJson(movie.getListMovies(), outToClient);
						else
							responseXML(movie.getListMovies(), outToClient);
					break;
				}
			}
		}
		
		/** Tratamento do POST e PUT**/
		private void treatmentRequestPostPut(String request, String method, JSONObject obj, DataOutputStream outToClient) throws IOException, Exception {

			int verificador = request.indexOf(".xml");
			boolean textoEspecificado = false;
			
			if(verificador > 0) {
				textoEspecificado = true;
				request = request.replace(".xml", "");
			}
			
			MovieController movie = new MovieController();
			
			switch (method) {
			case "POST":
				if(!textoEspecificado)
					responseJson(movie.cadastrarMovie(obj), outToClient);
				else
					responseXML(movie.cadastrarMovie(obj), outToClient);
				break;
			case "PUT":
				if(!textoEspecificado)
					responseJson(movie.atualizarMovie(obj), outToClient);
				else
					responseXML(movie.cadastrarMovie(obj), outToClient);
				break;
			
			default:
				System.out.println("Metodo nao implementado.");
				break;
			}
		}
		
		/** Tratamento do Delete**/
		private void treatmentRequestDelete(String request, String method, DataOutputStream outToClient) throws IOException, Exception {
			int verificador = request.indexOf(".xml");
			boolean textoEspecificado = false;
			
			if(verificador > 0) {
				textoEspecificado = true;
				request = request.replace(".xml", "");
			}
			
			String[] req = request.split("/");

			switch (req[0]) {
				case "movies": {
					
					MovieController movie = new MovieController();
					
					if(req.length == 2)
						if(!textoEspecificado)
							responseJson(movie.deleteMovie(req[1]), outToClient);
						else
							responseXML(movie.deleteMovie(req[1]), outToClient);
					
					break;
				}
			}
		}
		
		/** M�todo para enviar a resposta em JSON**/
		private void responseJson(JSONArray json, DataOutputStream outToClient) throws IOException{
			String line = "";
			
			/** Monta a header e o come�o do body **/
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
		
		/** M�todo para enviar a resposta json em XML**/
		private void responseXML(JSONArray json, DataOutputStream outToClient) throws IOException{
			String line = "";	
			String xml = XML.toString(json);
			
			line += "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
			line +="\n"+"<objetos>"+"\n"; 
			line += xml;
			line +="\n</objetos>";
			byte[] lineByte = line.getBytes("UTF-8");
			
			outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n" +
				"Server: FACOMCD-2020/1.0\r\n" +
				"Content-Type: 	text/xml\r\n" +
				"Content-Length: " + lineByte.length + "\r\n\r\n"
			);
			
			
			outToClient.write(lineByte);
		}
}
