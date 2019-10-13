import java.lang.Thread;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Servidor{

	public static void main(String [] args){
		int port = 9999;

		try{
			ServerSocket ss = new ServerSocket(port);
			Historico h = new Historico();
			while(true){
				try{
					Socket cs = ss.accept();
					(new Thread (new Leitor(cs, h))).start();
				}
				catch(Exception e){
					System.out.println(e);
				}
			}
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
}

class Escritor extends Thread{
	private Socket cs;
	private Historico historico;

	public Escritor(Socket s, Historico h){
		cs = s;
		historico = h;
	}

	public void run(){
		try{
			PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
			int tam = 0;
			String enviar = "";

			while(!(this.cs.isClosed())){//Enquanto n for EOF continuar
				//Se o tamanho forem iguais recebeu tudo, caso contrario vai enviando mensagens
				if(this.historico.tamanho() > tam){
					out.println(this.historico.get(tam).toString());
					tam++;
				}
				else{
					this.historico.adormece();
				}
				//Deveria de n enviar nada para o outro lado no entanto ele continua a escrever no ecra a propria mensagem
			}
			System.out.println("O Escritor morreu\n");
		}
		catch(IOException e){
			System.out.println(e);
		}
		//Sera que a thread do escritor realmente acaba?
	}
}


class Leitor extends Thread{
	private Socket sc;
	private Historico historico;

	public Leitor(Socket s, Historico h){
		sc = s;
		historico = h;
	}

	public void run(){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			PrintWriter out = new PrintWriter(this.sc.getOutputStream(), true);
			Menu menu = new Menu(this.historico, out);

			String conteudo, resposta;
			resposta = menu.processaConteudo("0");
			//Adicionar a parte do login/ registo
			while((conteudo = in.readLine()) != null){
				resposta = menu.processaConteudo(conteudo);
				if(resposta == null){
					(new Thread (new Escritor(this.sc, this.historico))).start();
				}
			}
			out.close();
			in.close();
			sc.close();
		}
		catch(IOException exc){
			System.out.println(exc.getMessage());
		}
	}
}