import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        AuxFuncoes.verificarDiretorio();

        if (!new File("dataset/capitulos.db").exists()) {
        System.out.println("Arquivo de dados não encontrado. Importando do CSV...");
        ImportadorCSV.importarCSVParaBinario();
    }
        System.out.println("Deseja utilizar qual estrutura de índice?");
        System.out.println("1 - Árvore B+");
        System.out.println("2 - Hashing Estendido");

        int escolha = MyIO.readInt();

        if (escolha == 1) {
            Menu.setModoIndice("B+");
        } else if (escolha == 2) {
            Menu.setModoIndice("HASH");
        } else {
            System.out.println("Opção inválida. Encerrando programa.");
            System.exit(0);
        }

        Menu.menu();
    }
}