import java.net.*;
import java.io.*;


public class Cliente{
	public static void main(String[] args) throws IOException, InterruptedException, SocketException{
		Socket cs = new Socket("127.0.0.1", 9999);

		PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
		BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

		LeitorCliente l = new LeitorCliente(cs);
		Thread t = new Thread(l);
		t.start();

		String current;

		while((current = teclado.readLine()) != null){
			out.println(current);
		}

		System.out.println("Shutdown Output");
;
		cs.shutdownOutput();

		teclado.close();
		out.close();
	}
}


class LeitorCliente implements Runnable{
	private Socket cs;
	private Menu menu;

	public LeitorCliente(Socket s){
		this.cs = s;
	}

	public void run(){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
			while(!this.cs.isClosed()){
				String eco = in.readLine();
				if(eco != null)
					System.out.println(eco);
			}
			System.out.println("Fechei");
		}
		catch(Exception e){
			System.out.println();
		}
	}
}