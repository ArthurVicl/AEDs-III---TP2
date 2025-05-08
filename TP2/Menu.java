
import java.io.*;

public class Menu {

    public static String BD = "dataset/capitulos.db";

    private static Indice indice = new Indice(50); // ordem pode ser configurada
    private static Hashing Hashing = new Hashing();

    private static String modoIndice = "B+"; // padrão


    public static void setModoIndice(String modo) {
        modoIndice = modo;
    }

    public static void menu() throws IOException {

    // Verifica se precisa reconstruir o índice
    File idxFile = new File(modoIndice.equals("B+") ? "dataset/capitulos.idx" : "dataset/capitulos_hash.idx");
    File dbFile = new File(BD);
    
    // Forçar reconstrução completa na primeira execução
    if (!idxFile.exists() || dbFile.lastModified() > idxFile.lastModified()) {
        System.out.println("Reconstruindo índice completo...");
        reconstruirIndice();
        salvarIndice();
    } else {
        try {
            if (modoIndice.equals("B+")) {
                indice.carregarIndice("dataset/capitulos.idx");
            } else {
                Hashing.carregarIndice("dataset/capitulos_hash.idx");
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar índice. Reconstruindo...");
            reconstruirIndice();
            salvarIndice();
        }
    }

    

        while (true) {
            MyIO.println("\n--- Menu CRUD Capitulo ---");
            MyIO.println("1. Criar Capitulo");
            MyIO.println("2. Ler Um Capitulo");
            MyIO.println("3. Ler Multiplos Capitulos");
            MyIO.println("4. Atualizar Capitulo");
            MyIO.println("5. Deletar Capitulo");
            MyIO.println("6. Sair");

            MyIO.print("Escolha uma opcao: ");
            int opcao = MyIO.readInt();

            switch (opcao) {
                case 1 -> {
                    if (criarCapitulo(AuxFuncoes.CriarNovoCapitulo())) {
                        MyIO.println("Criado com sucesso");
                    } else {
                        MyIO.println("Falhou na criacao");
                    }
                }
                case 2 -> {
                    if (!lerCapitulo(AuxFuncoes.qualID())) {
                        MyIO.println("Nao encontrado");
                    }
                }
                case 3 ->
                    lerCapitulos(AuxFuncoes.PerguntaQTD_ID());

                case 4 -> {
                    if (atualizarCapitulo(AuxFuncoes.qualID())) {
                        MyIO.println("Atualizado com sucesso");
                    } else {
                        MyIO.println("Falhou na atualizacao");
                    }
                }
                case 5 -> {
                    if (deletarCapitulo(AuxFuncoes.qualID())) {
                        MyIO.println("Excluido com sucesso");
                    } else {
                        MyIO.println("Falhou na exclusao");
                    }
                }
                case 6 -> {
                    try {
                        if (modoIndice.equals("B+")) {
                            indice.salvarIndice("dataset/capitulos.idx");
                        } else {
                            Hashing.salvarIndice("dataset/capitulos_hash.idx");
                        }
                        System.out.println("Índice salvo com sucesso!");
                    } catch (IOException e) {
                        System.out.println("Erro ao salvar índice: " + e.getMessage());
                    }
                    System.out.println("Saindo...");
                    System.exit(0);
                }
                
                default ->
                    MyIO.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void salvarIndice() {
        try {
            if (modoIndice.equals("B+")) {
                indice.salvarIndice("dataset/capitulos.idx");
                System.out.println("Índice B+ salvo com sucesso!");
            } else {
                Hashing.salvarIndice("dataset/capitulos_hash.idx");
                System.out.println("Índice Hashing salvo com sucesso!");
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar índice: " + e.getMessage());
        }
    }

    private static boolean criarCapitulo(Capitulo capitulo) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(BD, "rw")) {
            long posicao = raf.length();
            byte[] bytes = capitulo.toByteArray();
            
            // Escreve no arquivo
            raf.seek(posicao);
            raf.writeByte(1);
            raf.writeInt(bytes.length);
            raf.write(bytes);
            
            // Atualiza o índice
            if (modoIndice.equals("B+")) {
                indice.inserir(capitulo.getId(), posicao);
            } else {
                Hashing.inserir(capitulo.getId(), posicao);
            }
            
            // Atualiza último ID
            AuxFuncoes.IncrementaUltimoIdInserido();
            
            return true;
        } catch (Exception e) {
            System.out.println("Erro ao criar capítulo: " + e.getMessage());
            return false;
        }
    }

    private static boolean lerCapitulo(int ID) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(BD, "rw");
    
        Long posicao;
        if (modoIndice.equals("B+")) {
        posicao = indice.buscar(ID);
        } else {
            posicao = Hashing.buscar(ID);
        }


        if (posicao == null) {
            raf.close();
            return false; // ID não encontrado
        }
    
        raf.seek(posicao);
    
        byte valido = raf.readByte();
        int tamanhoVetor = raf.readInt();
    
        if (valido == 1) {
            byte[] byteArray = new byte[tamanhoVetor];
            raf.readFully(byteArray);
    
            Capitulo capitulo = new Capitulo();
            capitulo.fromByteArray(byteArray);
    
            MyIO.println(capitulo.toString());
            raf.close();
            return true;
        }
    
        raf.close();
        return false;
    }
    

    private static void lerCapitulos(int[] ids) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("dataset/capitulos.db", "rw");

        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            byte valido = raf.readByte();
            int tamanhoVetor = raf.readInt();

            if (valido == 1) {
                byte[] byteArray = new byte[tamanhoVetor];
                raf.readFully(byteArray);

                Capitulo capitulo = new Capitulo();
                capitulo.fromByteArray(byteArray);

                for (int id : ids) {
                    if (capitulo.getId() == id) {
                        MyIO.println(capitulo.toString());
                    }
                }
            } else {
                raf.skipBytes(tamanhoVetor);
            }
        }

        raf.close();

    }

    private static boolean atualizarCapitulo(int ID) throws IOException {
        RandomAccessFile RAF = new RandomAccessFile(BD, "rw");
        RAF.seek(4);
    
        while (RAF.getFilePointer() < RAF.length()) {
            long posicao = RAF.getFilePointer();
            byte valido = RAF.readByte();
            int tamanhoVetor = RAF.readInt();
    
            if (valido == 1) {
                byte[] byteArray = new byte[tamanhoVetor];
                RAF.readFully(byteArray);
                Capitulo capitulo = new Capitulo();
                capitulo.fromByteArray(byteArray);
    
                if (capitulo.getId() == ID) {
                    Capitulo novoCapitulo = AuxFuncoes.CriarNovoCapitulo();
                    novoCapitulo.setId(ID);
    
                    byte[] novoByteArray = novoCapitulo.toByteArray();
    
                    if (novoByteArray.length <= tamanhoVetor) {
                        MyIO.println("Atualizacao coube no espaco reservado");
                        RAF.seek(posicao + 5);
                        RAF.write(novoByteArray);
                        RAF.write(new byte[tamanhoVetor - novoByteArray.length]);
                        RAF.close();
                        return true;
                    } else {
                        MyIO.println("Atualizacao nao coube no espaco reservado, inserido no fim do arquivo");
    
                        // Marca o antigo como removido
                        RAF.seek(posicao);
                        RAF.writeByte(0);
    
                        // Insere no final
                        RAF.seek(RAF.length());
                        long novaPosicao = RAF.getFilePointer();
                        AuxFuncoes.escreverCapitulo(novoByteArray, novaPosicao);
    
                        // Atualiza o índice com a nova posição
                        if (modoIndice.equals("B+")) {
                            indice.inserir(ID, novaPosicao);
                        } else {
                            Hashing.remover(ID);
                            Hashing.inserir(ID, novaPosicao);
                        }
                        
                        RAF.close();
                        return true;
                    }
                }
            } else {
                RAF.skipBytes(tamanhoVetor);
            }
        }
    
        RAF.close();
        return false;
    }

    private static boolean deletarCapitulo(int ID) throws IOException {
        RandomAccessFile RAF = new RandomAccessFile(BD, "rw");
    
        RAF.seek(0);
        int UltimoId = RAF.readInt();
    
        while (RAF.getFilePointer() < RAF.length()) {
            long ponteiro = RAF.getFilePointer();
            byte valido = RAF.readByte();
            int tamanhoVetor = RAF.readInt();
    
            if (valido == 1) {
                byte[] byteArray = new byte[tamanhoVetor];
                RAF.readFully(byteArray);
                Capitulo capitulo = new Capitulo();
                capitulo.fromByteArray(byteArray);
    
                if (capitulo.getId() == ID) {
                    RAF.seek(ponteiro);
                    RAF.writeByte(0);
    
                    // Remove o ID do índice
                    if (modoIndice.equals("B+")) {
                        indice.remover(ID);
                    } else {
                        Hashing.remover(ID);
                    }
                    
    
                    // Atualiza último ID se necessário
                    if (ID == UltimoId) {
                        RAF.seek(0);
                        RAF.writeInt(UltimoId - 1);
                    }
    
                    RAF.close();
                    return true;
                }
            } else {
                RAF.skipBytes(tamanhoVetor);
            }
        }
        RAF.close();
        return false;
    }

    private static void reconstruirIndice() throws IOException {
        System.out.println("Iniciando reconstrução COMPLETA do índice...");
        
        // Limpa o índice existente
        if (modoIndice.equals("B+")) {
            indice = new Indice(50);
        } else {
            Hashing = new Hashing();
        }
    
        RandomAccessFile raf = new RandomAccessFile(BD, "rw");
        raf.seek(4); // Pula o cabeçalho
    
        int registrosProcessados = 0;
        
        while (raf.getFilePointer() < raf.length()) {
            long posicao = raf.getFilePointer();
            byte valido = raf.readByte();
            int tamanho = raf.readInt();
    
            if (valido == 1) {
                byte[] dados = new byte[tamanho];
                raf.readFully(dados);
    
                Capitulo capitulo = new Capitulo();
                capitulo.fromByteArray(dados);
    
                if (modoIndice.equals("B+")) {
                    indice.inserir(capitulo.getId(), posicao);
                } else {
                    Hashing.inserir(capitulo.getId(), posicao);
                }
                
                registrosProcessados++;
            } else {
                raf.skipBytes(tamanho);
            }
        }
    
        raf.close();
        System.out.println("Reconstrução completa! " + registrosProcessados + " capítulos indexados.");
    }
    
}
