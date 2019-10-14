import java.lang.Thread;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Servidor{

	public static void main(String [] args){
		int port = 9999;

		try{
			ServerSocket ss = new ServerSocket(port);
			Historico h = new Historico();
			criaSalas(h);

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

	public static void criaSalas(Historico h){
		h.getSalas().put("Desporto", new Sala(new ArrayList<>(), "Desporto"));
		h.getSalas().put("Politica", new Sala(new ArrayList<>(), "Politica"));
		h.getSalas().put("Riczao", new Sala(new ArrayList<>(), "Riczao"));
	}
}

class Escritor extends Thread{
	private Socket cs;
	private Sala sala;

	public Escritor(Socket s, Sala sl){
		cs = s;
		sala = sl;
	}

	public void run(){
		try{
			PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
			int tam = 0;
			String enviar = "";

			while(!(this.cs.isClosed())){//Enquanto n for EOF continuar
				//Se o tamanho forem iguais recebeu tudo, caso contrario vai enviando mensagens
				if(this.sala.tamanho() > tam){
					out.println(this.sala.getMensagem(tam).toString());
					tam++;
				}
				else{
					this.sala.adormece();
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

			while((conteudo = in.readLine()) != null){
				resposta = menu.processaConteudo(conteudo);
				Matcher m = Pattern.compile("sala__:(.*)").matcher(resposta);
				if(m.matches()){
					Sala s = this.historico.getSala(m.group(1));
					//Ver qual é a sala escolhida
					(new Thread (new Escritor(this.sc, s))).start();
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