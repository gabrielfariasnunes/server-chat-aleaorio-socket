package server;

import java.net.ServerSocket;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class ChatServer extends Thread {

	private static final String ENDERECO_EM_USO = "EADDRINUSE";
    private static final Socket SEM_PARCEIRO = null;
    private static final int PORTA_MINIMA = 1024;
    private static final int PORTA_MAXIMA = 65535;
	private static final int TEMPO_SAIDA  = 3000;
	private static final int LISTA_DE_ESPERA_VAZIA = 0; 
	
	private static boolean isProcurar;
    private static ChatServer instance;

    private int porta;
	
    private List<ConnectionHandler> listaDeEspera;

    //Cria uma instancia unica do servidor
    public static ChatServer create(int porta) {
        return instance == null ? instance = new ChatServer(porta) : instance;
    }

    //Verifica se a porta e maior ou igual a 1024 e menor ou igual a 65535 e inicia o servidor
    ChatServer(int porta) {
        if (porta >= PORTA_MINIMA && porta <= PORTA_MAXIMA) {
            this.porta = porta;
            this.listaDeEspera = new ArrayList<>();
        } else {
            System.out.println("Não e possivel inicializar o servidor!");
            System.out.println("Tente usar portas entre 1024 a 65535.");
        }
    }

    //Thread
    @Override
    public void run() {
        try {
            ServerSocket servidor = new ServerSocket(porta);
			while (servidor.isBound()) {		
                new ConnectionHandler(servidor.accept(), listaDeEspera).start();
				if(!isProcurar){
					isProcurar = true;
					new Thread(procurarParceiro).start();
				}
            }
            servidor.close();
        } catch (IOException e) {
            if (e.getMessage().contains(ENDERECO_EM_USO)) {
                System.out.println("Essa porta já está em uso!");
            }
        }
    }

	//Gerar uma posição a aleatoria
	int posicaoAletoria() {
		int posicao = (int) Math.round(Math.random() * listaDeEspera.size());
		if(posicao > 0){
			return posicao-=1;
		}
		return posicao;
	}
	
	//Tempo de saida
	void timeout(long time){
		try {
			Thread.sleep(time);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//Encontrar um parceiro aleatorio.
	private Runnable procurarParceiro = new Runnable(){
		@Override
		public void run() {
			while (isProcurar && listaDeEspera.size() != LISTA_DE_ESPERA_VAZIA) {
				ConnectionHandler estranho = listaDeEspera.get(posicaoAletoria());
				ConnectionHandler estranha = listaDeEspera.get(posicaoAletoria());
				if (estranho.socket().getPort() != estranha.socket().getPort()
					&& estranho.getParceiro() == SEM_PARCEIRO
					&& estranha.getParceiro() == SEM_PARCEIRO) {
					estranho.setParceiro(estranha.socket());
					estranha.setParceiro(estranho.socket());
					System.out.println("Encontrei e criei uma conversa entre eles....");
				}
				System.out.println("Procurando novos pares aleátorios...");
				timeout(TEMPO_SAIDA);
			}
			System.out.println("Sem clientes no momento!");
			isProcurar = false;
		}
	};
}
