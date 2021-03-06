package ch.supsi.webapp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

public class Server {

	private static ServerSocket serverSocket;
	private final static int PORT = 8080;
	private final static String CONTENT_LENGTH_HEADER = "Content-Length";
	private final static String LINEBREAK = "\r\n";

	public static void main(String[] args) throws Exception {
		serverSocket = new ServerSocket(PORT);
		System.out.println("Server avviato sulla porta : " + PORT);
		System.out.println("-------------------------------------");

		while (true) {
			Socket clientSocket = serverSocket.accept();
			clientSocket.setSoTimeout(200);
			handleRequest(clientSocket);
			clientSocket.close();
		}
	}

	public static void handleRequest(Socket socket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStream out = socket.getOutputStream();

			Request request = readRequest(in);
			if (request == null) {
				return;
			}

			System.out.println(request.allRequest);

			Content responseBody = handleResponseContent(request);

			produceResponse(out, responseBody);

			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private static Request readRequest(BufferedReader input) throws IOException {
		String firstline = input.readLine();
		if (firstline != null) {
			System.out.println("----------------- " + new Date() + " --------------");
			boolean isPost = firstline.startsWith("POST");
			return getRequest(input, firstline, isPost);
		}
		return null;
	}

	private static Request getRequest(BufferedReader input, String line, boolean isPost) throws NumberFormatException, IOException {
		StringBuilder rawRequest = new StringBuilder();
		rawRequest.append(line);
		String resource = line.substring(line.indexOf(' ')+1, line.lastIndexOf(' '));
		int contentLength = 0;
		while (!(line = input.readLine()).equals("")) {
			rawRequest.append('\n').append(line);
			if (line.startsWith(CONTENT_LENGTH_HEADER))
				contentLength = Integer.parseInt(line.substring(CONTENT_LENGTH_HEADER.length()+2));
		}
		String body = "";
		if (isPost) {
			rawRequest.append("\n\n").append(getBody(input, contentLength));
		}
		return new Request(rawRequest.toString(), resource, body, isPost);
	}

	private static String getBody(BufferedReader bf, int length) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++)
			sb.append((char) bf.read());
		return sb.toString();
	}

	/*
	 * Usare questo metodo per gestire la richiesta ricevuta e produrre un 
	 * contenuto (txt, html, ...) da dare come corpo nella risposta
	 * 
	 */
	private static Content handleResponseContent(Request request) {

		if(request.resource.equals("/prova.html")){
			try {
				byte[] bytes = Files.readAllBytes(Paths.get("/tmp/prova.html"));
				return new Content(bytes);
			} catch (IOException e) {
				e.printStackTrace();
				return new Content("404 - Page not Found".getBytes(), 404);

			}
		} else if(request.resource.equals("/")){
			String currentTime = new Date().toString();
			return new Content(("<!DOCTYPE html>" +
					"<html>" +
					"<head>" +
					"<title>Main Page</title>" +
					"</head><body>" +
					"<h1>Welcome</h1>" +
					"<p>It's " + currentTime + "</p>" +
					"</body>" +
					"</html>").getBytes());
		}

		return new Content("File not found".getBytes(), 404);
	}

	/*
	 * Usare questo metodo per scrivere l'intera risposta HTTP (prima linea+headers+body)
	 * 
	 */
	private static void produceResponse(OutputStream output, Content responseContent) throws IOException 
	{
		// usare la variabile LINEBREAK per andare a capo

		switch(responseContent.code){
			case 200:
				output.write("HTTP/1.1 200 OK\r\n".getBytes());
			case 404:
				output.write("HTTP/1.1 404 Not Found\r\n".getBytes());
			case 418:
				output.write("HTTP/1.1 418 I'm a teapot\r\n".getBytes());
			default:
				output.write("HTTP/1.1 500 Internal Server Error\r\n".getBytes());
		}


		output.write(String.format("Content-Length: %d\r\n", responseContent.length).getBytes());
		output.write(String.format("Content-Type: text/html\r\n\r\n").getBytes());
		output.write(new String(responseContent.content).getBytes());
	}

}