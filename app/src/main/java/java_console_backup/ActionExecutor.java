/*package java_console_backup;

import java.util.Scanner;

import Entities.ActionType;
import Entities.User;
import Services.AuthorizationService;

public class ActionExecutor {

    private Scanner sc = new Scanner(System.in);

    public void execute(ActionType action, User user) {
        boolean requiresPassword = false;

        switch (action) {
            case CHAMADAS:
            case NAVEGADOR:
            case WHATSAPP_NORMAL:
                requiresPassword = true;
                break;
            case WHATSAPP_BUSINESS:
                requiresPassword = false;
                break;
        }

        if (requiresPassword) {
            System.out.print("Senha administrativa necessária: ");
            String senha = sc.nextLine();

            if (!AuthorizationService.requestAdminPassword(senha)) {
                System.out.println("Ação bloqueada! Senha incorreta ou acesso não permitido.");
                return;
            }
        }

        switch (action) {
            case CHAMADAS:
                System.out.println("Acessando app de chamadas...");
                break;
            case NAVEGADOR:
                System.out.println("Acessando navegador...");
                break;
            case WHATSAPP_NORMAL:
                System.out.println("Acessando WhatsApp normal...");
                break;
            case WHATSAPP_BUSINESS:
                System.out.println("Acessando WhatsApp Business...");
                break;
        }
    }
}
*/
