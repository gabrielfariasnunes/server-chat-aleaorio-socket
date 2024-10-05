package server;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.io.ByteArrayInputStream;
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConnectionHandler extends Thread {

    private static final int TEMPO_DE_ESPERA = 500;
	private static final int PING = 0;
	private static final Socket SEM_PARCEIRO = null;
	private static final int LEITURA_FINALIZADA = 0;
	private static final int SEM_LEITURA = -1;
	
	private BufferedInputStream entrada;
	private PrintStream saida;
	private Socket socket;
	private Socket socketParceiro;
	private List<ConnectionHandler> listaDeEspera;
	private ByteArrayOutputStream buffer;

	//Cria o handler e injeta o socket e a lista de espera
	public ConnectionHandler(Socket socket, List<ConnectionHandler> listaDeEspera) {
		this.socket = socket;
		this.listaDeEspera = listaDeEspera;
        listaDeEspera.add(this);
	}

	//Vincula o socket do parceiro de conversa
	public void setParceiro(Socket parceiro) {
		this.socketParceiro = parceiro;
	}

    //Retornar o socket
	public Socket socket() {
		return socket;
	}
	
	//Retorna o socket do parceiro
	public Socket getParceiro(){
		return socketParceiro;
	}

	@Override
	public void run() {
		try {	
			buffer  = new ByteArrayOutputStream();
			entrada = new BufferedInputStream(socket.getInputStream());
			saida   = new PrintStream(socket.getOutputStream());	
            esperandoUmParceiro();
        } catch (IOException e) {
			int portaParceiro = socketParceiro.getPort();
			int porta = socket.getPort();
            System.out.format("%s encerrou o chat com %s\n", porta, portaParceiro);
        }
    }
    
	//Espera até que tenha um parceiro
    void esperandoUmParceiro() throws IOException {
       	saida.println("Procurando um parceiro(a)...");
        while (!saida.checkError() && socketParceiro == SEM_PARCEIRO) {
            saida.write(PING);
            timeout(TEMPO_DE_ESPERA);
        }
        if (Objects.isNull(socketParceiro)) {
            System.out.format("Desistiu de iniciar uma conversa!\n", socket.getPort());
            listaDeEspera.remove(this);
            finalizarConversa();
        } else {
			listaDeEspera.remove(this);
            receberMensagens();
        }
    }

    //Recebe as mensagens do socket e escreve no socket do parceiro
    void receberMensagens() throws IOException {
        saida = new PrintStream(socketParceiro.getOutputStream());
		saida.println("Vocês estão conectados. Diga Oi!");
		int data;
		while((data = entrada.read()) != SEM_LEITURA){
			buffer.write(data);
			if(entrada.available() == LEITURA_FINALIZADA){
				String mensagem = buffer.toString().trim();
				if(!mensagem.isEmpty()){
					saida.format("Estranho: %s\n", mensagem);
				}
				buffer.reset();
			}
		}
        finalizarConversa();
    }

    //liberar recursos e fecha a conexão do socket
    void finalizarConversa() throws IOException {
        if (Objects.nonNull(socketParceiro)) {  
			  listaDeEspera.remove(this);
              socketParceiro.close();
        }
		buffer.close();
        socket.close();
    }

    //Tempo de espera
    void timeout(int time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}

