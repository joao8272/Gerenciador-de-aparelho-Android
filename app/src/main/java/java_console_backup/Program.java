/* java_console_backup;

import java.util.Scanner;

import Actions.ActionExecutor;
import Entities.ActionType;
import Entities.User;

public class Program {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ActionExecutor executor = new ActionExecutor();

        User usuario = new User("João", "comum");

        while (true) {
            System.out.println("\nEscolha a ação:");
            System.out.println("1 - Fazer chamada");
            System.out.println("2 - Abrir navegador");
            System.out.println("3 - WhatsApp normal");
            System.out.println("4 - WhatsApp Business");
            System.out.println("0 - Sair");

            int opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1: executor.execute(ActionType.CHAMADAS, usuario); break;
                case 2: executor.execute(ActionType.NAVEGADOR, usuario); break;
                case 3: executor.execute(ActionType.WHATSAPP_NORMAL, usuario); break;
                case 4: executor.execute(ActionType.WHATSAPP_BUSINESS, usuario); break;
                case 0: System.out.println("Saindo..."); return;
                default: System.out.println("Opção inválida!"); break;
            }
        }
    }
}
*/