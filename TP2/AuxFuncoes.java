import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AuxFuncoes {

    // Separa o texto CSV
    public static String[] separarPorVirgula(String texto) {
        String[] campos = texto.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    
        for (int i = 0; i < campos.length; i++) {
            campos[i] = campos[i].replaceAll("^\"|\"$", "");
        }
    
        return campos;
    }
    

    // Converte a data
    public static String formatarData(String data) throws ParseException {
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        SimpleDateFormat formatoSaida = new SimpleDateFormat("dd/MM/yyyy");
        Date date = formatoEntrada.parse(data);
        return formatoSaida.format(date);
    }

    // Pergunta a quantidade de IDs e os coleta
    public static int[] PerguntaQTD_ID() throws IOException {
        RandomAccessFile RAF = new RandomAccessFile("dataset/capitulos.db", "rw");

        RAF.seek(0);  
        int ultimoId = RAF.readInt();  

        System.out.println("\nDigite a quantidade de capitulos que deseja pesquisar: ");
        int qtdIds = MyIO.readInt();
        int[] ids = new int[qtdIds];

        for (int i = 0; i < qtdIds; i++) {
            do {
                MyIO.println("Qual o ID do capitulo?");
                ids[i] = MyIO.readInt();

                if (ids[i] > ultimoId) {
                    MyIO.println("ID invalido. O ultimo ID registrado é " + ultimoId + ". Digite novamente.");
                }
            } while (ids[i] > ultimoId);  
        }
        RAF.close();  
        return ids;  
    }

    // Reescreve o último ID inserido no arquivo
    public static void IncrementaUltimoIdInserido() throws IOException {
        RandomAccessFile RAF = new RandomAccessFile("dataset/capitulos.db", "rw");

        RAF.seek(0);  
        int ultimoID = RAF.readInt();  

        RAF.seek(0);  
        RAF.writeInt(ultimoID + 1);  

        RAF.close();  
    }

    // Escreve os dados do capítulo no arquivo de forma binária
    public static void escreverCapitulo(byte[] dataBytes, long lugar) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("dataset/capitulos.db", "rw");
        raf.seek(lugar);  

        raf.writeByte(1);  

        raf.writeInt(dataBytes.length);  

        raf.write(dataBytes);  
        raf.close();  
    }

    // Pergunta ao usuário qual ID ele deseja 
    public static int qualID() {
        MyIO.println("Qual o ID?");
        int i = MyIO.readInt();  
        return i;  
    }

    // Coleta dados para criar um novo capítulo
    static Capitulo CriarNovoCapitulo() throws IOException {
        try (RandomAccessFile RAF = new RandomAccessFile("dataset/capitulos.db", "rw")) {
            int UltimoId = 0;

            RAF.seek(0);  
            UltimoId = RAF.readInt();  

            int id = UltimoId + 1;

            MyIO.print("(short) Capitulo: ");
            Short numCapitulo = (short) MyIO.readInt();

            MyIO.print("(short) Volume: ");
            Short volume = (short) MyIO.readInt();

            MyIO.print("(String) Nome: ");
            String nome = MyIO.readLine();

            MyIO.print("(String) Titulo Original: ");
            String tituloOriginal = MyIO.readLine();

            MyIO.print("(String) Titulo Ingles: ");
            String tituloIngles = MyIO.readLine();

            MyIO.print("(short) Paginas: ");
            Short paginas = (short) MyIO.readInt();

            MyIO.print("(xx/xx/xxxx) Data: ");
            String data = MyIO.readLine();

            MyIO.print("(String) Episodio: ");
            String episodio = MyIO.readLine();

            String[] titulos = { tituloOriginal, tituloIngles };

            return new Capitulo(id, numCapitulo, volume, nome, titulos, paginas, data, episodio);
        }
    }

    private static final String BD = "dataset/capitulos.db";

    public static void verificarDiretorio() {
        File dir = new File("dataset");
        if (!dir.exists()) {
            boolean criado = dir.mkdir();
            if (!criado) {
                System.out.println("Falha ao criar diretório 'dataset'");
                System.exit(1);
            }
        }
        
        // Verifica se o arquivo de dados existe
        File dbFile = new File(BD);
        if (!dbFile.exists()) {
            try {
                ImportadorCSV.importarCSVParaBinario();
            } catch (Exception e) {
                System.out.println("Erro ao criar arquivo de dados: " + e.getMessage());
                System.exit(1);
            }
        }
    }
}
