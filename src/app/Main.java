package app; //precisamos de um pacote para agrupar as classes

import model.ContaBancaria;
import model.Usuario;
import service.ContaService;
import service.UsuarioService;

public class Main { //Só pra testar
    public static void main(String[] args) {
        UsuarioService usuarioService = new UsuarioService();
        ContaService contaService = new ContaService();

        //Cadastrando um usuario
        Usuario nick = usuarioService.cadastrar("Nick B)", "12345678900", "nick@email.com", "85999999999");  //85 + 9 Noves;
        System.out.println("Usuário criado:" + nick );

        //criando uma conta pro mlk
        ContaBancaria contaNick = contaService.criarConta("006-7", nick);
        System.out.println("Conta criada:" + contaNick);

        //associando uma chave pix
        boolean associou = contaService.associarChavePix("006-7", "nick@email.com", "EMAIL");
        System.out.println("Chave Associada?" + associou);

        //Recebe um valor via Pix usando chave
        boolean recebeu = contaService.receberViaPix("nick@email.com", 100.0);
        System.out.println("Recebeu R$100 via PIX?" + recebeu);
        System.out.println("Saldo atual: "+contaNick.getSaldo());

        //Criamos um segundo usuário para testar transferência  
        Usuario maria = usuarioService.cadastrar("Maria :0", "23456789000", "maria@email.com", "85988888888");  //859 + 8 Oitos;
        ContaBancaria contaMaria = contaService.criarConta("006-9", maria);
        boolean associouMaria = contaService.associarChavePix("006-9", "maria@email.com", "EMAIL");
        System.out.println("Chave de Maria foi Associada? " + associouMaria);

        //Agora vamos transferir do Nick pra Marai
        boolean transferiu = contaService.transferirViaPix("nick@email.com", "maria@email.com", 30.0);
        System.out.println("Transferência feita? " + transferiu);
        System.out.println("Saldo João: " + contaNick.getSaldo());
        System.out.println("Saldo Maria: " + contaMaria.getSaldo());
    }

    //comando pra rodar no terminal
//     javac -d out src/model/*.java src/service/*.java src/app/*.java
//     java -cp out app.Main
}