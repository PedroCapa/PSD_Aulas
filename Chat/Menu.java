
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.lang.Float.*;

/*
* Classe que implementa a interface para o cliente
*/

public class Menu {
    /** historico do leilão que guarda a informação */
    private Historico historico;
    /** Cliente a tratar  */
    private Utilizador cliente;
    /** Estado do menu  */
    private String fase;
    /** String para guardar valor anterior */
    private String tmp;

    private PrintWriter out;
    /*
    * Construtor parameterizado
    * @param sis historico que gere leilões
    * @param pw  Canal que está ligado ao cliente
    */
    public Menu(Historico sis, PrintWriter pw){
        this.historico = sis;
        this.cliente = null;
        this.fase = "0";
        this.out = pw;
    }
    /*
    * Método que verifica em que fase do menu o cliente está,
    recebe o input do cliente e calcula a resposta do servidor para o cliente
    * @param str Input do cliente
    * @return Resposta do servidor ao cliente
    */
    public String processaConteudo(String str){
        String resposta = null;
        System.out.println(fase);
        switch(fase){
            case "0" : 
                resposta = inicio();
                out.println(resposta);
                break;
            case "1" : 
                resposta = Menu.showEmail();
                if(str.equals("1")) fase = "email1";
                else if(str.equals("2"))fase = "email2";
                else resposta = inicio();
                out.println(resposta);
                break;
            case "email1" : 
                cliente = historico.getClientes().get(str);
                if(cliente == null){
                    resposta = "Email incorreto\n"+inicio();
                }else{
                    resposta = Menu.showPass();
                    fase = "pass1";
                    tmp = str;
                }
                out.println(resposta);
                break;
            case "email2" : 
                cliente = historico.getClientes().get(str);
                if(cliente != null){
                    resposta = "Email já existe\n"+inicio();
                }else{
                    resposta = Menu.showPass();
                    fase = "pass2";
                    tmp = str;
                }
                out.println(resposta);
                break;
            case "pass1" : 
                    historico.login(cliente.nome,str);
                    resposta = showMenu();
                    fase = "menu";
                    out.println(resposta);
                break;
            case "pass2" : 
                    this.cliente = historico.registar(tmp,str);
                    resposta = showMenu();
                    out.println(resposta);
                    fase = "menu";
                break;
            case "menu" : 
                    historico.adicionaMensagem(cliente.nome, str);
                    historico.acorda();
                    resposta = str;
                break;
        }
        return resposta;

    }
    /*
    * Método que mostra as opções ao cliente quando este se liga ao historico
    * @return String que mostra o menu inicial
    */
    public String inicio(){
        fase = "1";
        return Menu.showOpcoes();
    }
    /*
    * Método que devolve a resposta ao cliente quando pede email
    * @return String que pede ao cliente o email
    */
    public static String showEmail(){
        return "Email:";
    }
    /*
    * Método que devolve a resposta ao cliente quando pede a password
    * @return String que vai pedir ao cliente a password
    */
    public static String showPass(){
        return "Pass:";
    }
    /*
    * Método que contém as opções do menu principal
    * @return String que mostra o menu principal
    */
    private static String showMenu(){
	    String array = null;
	    return array;
	}
    /*
    * Método que contém as opções do menu inicial, quando o cliente acaba de se ligar ao historico
    * @return String que mostra o menu inicial
    */
    private static String showOpcoes(){
		String array = "[1]Quer fazer o login?\n[2]Quer se registar?\n\nEscolha uma das opçoes acima:";
		return array;
	}
}