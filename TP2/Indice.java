import java.io.*;
import java.util.*;

class NoBPlus {
    public boolean folha;
    public List<Integer> chaves;
    public List<Object> ponteiros;
    public NoBPlus proximo;

    public NoBPlus(boolean folha) {
        this.folha = folha;
        this.chaves = new ArrayList<>();
        this.ponteiros = new ArrayList<>();
        this.proximo = null;
    }
}

public class Indice {
    private NoBPlus raiz;
    private int ordem;

    public Indice(int ordem) {
        this.ordem = ordem;
        this.raiz = new NoBPlus(true);
    }

    public void inserir(int id, long posicao) {
        InserirResultado resultado = inserirRecursivo(raiz, id, posicao);
        if (resultado != null && resultado.promovido) {
            NoBPlus novaRaiz = new NoBPlus(false);
            novaRaiz.chaves.add(resultado.chave);
            novaRaiz.ponteiros.add(raiz);
            novaRaiz.ponteiros.add(resultado.direita);
            raiz = novaRaiz;
        }
    }

    private InserirResultado inserirRecursivo(NoBPlus no, int id, long posicao) {
        if (no.folha) {
            int i = 0;
            while (i < no.chaves.size() && id > no.chaves.get(i)) i++;
            
            no.chaves.add(i, id);
            no.ponteiros.add(i, posicao);
            
            if (no.chaves.size() >= ordem) {
                return dividirNo(no);
            }
            return null;
        } else {
            int i = 0;
            while (i < no.chaves.size() && id >= no.chaves.get(i)) i++;
            
            InserirResultado resultado = inserirRecursivo((NoBPlus)no.ponteiros.get(i), id, posicao);
            
            if (resultado != null && resultado.promovido) {
                no.chaves.add(i, resultado.chave);
                no.ponteiros.add(i+1, resultado.direita);
                
                if (no.chaves.size() >= ordem) {
                    return dividirNo(no);
                }
            }
            return null;
        }
    }

    private InserirResultado dividirNo(NoBPlus no) {
        int meio = no.chaves.size() / 2;
        int chavePromovida = no.chaves.get(meio);
        
        NoBPlus novoNo = new NoBPlus(no.folha);
        novoNo.chaves.addAll(no.chaves.subList(meio + (no.folha ? 0 : 1), no.chaves.size()));
        novoNo.ponteiros.addAll(no.ponteiros.subList(meio + (no.folha ? 0 : 1), no.ponteiros.size()));
        
        no.chaves.subList(meio, no.chaves.size()).clear();
        no.ponteiros.subList(meio + (no.folha ? 0 : 1), no.ponteiros.size()).clear();
        
        if (no.folha) {
            novoNo.proximo = no.proximo;
            no.proximo = novoNo;
        }
        
        return new InserirResultado(chavePromovida, novoNo, true);
    }

    public void remover(int id) {
        NoBPlus no = raiz;

        while (!no.folha) {
            int i = 0;
            while (i < no.chaves.size() && id >= no.chaves.get(i)) i++;
            no = (NoBPlus) no.ponteiros.get(i);
        }

        int index = no.chaves.indexOf(id);
        if (index != -1) {
            no.chaves.remove(index);
            no.ponteiros.remove(index);
        }
    }

    public Long buscar(int id) {
        NoBPlus no = raiz;

        while (!no.folha) {
            int i = 0;
            while (i < no.chaves.size() && id >= no.chaves.get(i)) i++;
            no = (NoBPlus) no.ponteiros.get(i);
        }

        for (int i = 0; i < no.chaves.size(); i++) {
            if (no.chaves.get(i) == id) {
                return (Long) no.ponteiros.get(i);
            }
        }

        return null;
    }

    public void salvarIndice(String caminho) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminho))) {
            writer.write("ORDEM:" + ordem);
            writer.newLine();
            
            Queue<NoBPlus> fila = new LinkedList<>();
            fila.add(raiz);
            
            while (!fila.isEmpty()) {
                NoBPlus no = fila.poll();
                
                writer.write("NO:" + (no.folha ? "FOLHA" : "INTERNO"));
                writer.newLine();
                
                writer.write("CHAVES:");
                for (int chave : no.chaves) {
                    writer.write(chave + ";");
                }
                writer.newLine();
                
                if (no.folha) {
                    writer.write("POSICOES:");
                    for (Object pos : no.ponteiros) {
                        writer.write(pos + ";");
                    }
                    writer.newLine();
                } else {
                    for (Object ponteiro : no.ponteiros) {
                        fila.add((NoBPlus) ponteiro);
                    }
                }
            }
        }
    }

    public void carregarIndice(String caminho) throws IOException {
        File arquivo = new File(caminho);
        if (!arquivo.exists()) return;
    
        BufferedReader reader = new BufferedReader(new FileReader(caminho));
        String linha;
        
        // Lê a ordem
        linha = reader.readLine();
        this.ordem = Integer.parseInt(linha.split(":")[1]);
        
        // Reconstrói a árvore
        this.raiz = lerNoTexto(reader);
        
        reader.close();
    }
    
    private NoBPlus lerNoTexto(BufferedReader reader) throws IOException {
        String linha = reader.readLine();
        boolean folha = linha.split(":")[1].equals("FOLHA");
        NoBPlus no = new NoBPlus(folha);
        
        // Lê chaves
        linha = reader.readLine();
        String[] chavesStr = linha.split(":")[1].split(";");
        for (String chave : chavesStr) {
            if (!chave.isEmpty()) {
                no.chaves.add(Integer.parseInt(chave));
            }
        }
        
        if (folha) {
            // Lê posições
            linha = reader.readLine();
            String[] posicoesStr = linha.split(":")[1].split(";");
            for (String pos : posicoesStr) {
                if (!pos.isEmpty()) {
                    no.ponteiros.add(Long.parseLong(pos));
                }
            }
        } else {
            // Lê nós filhos
            for (int i = 0; i <= no.chaves.size(); i++) {
                NoBPlus filho = lerNoTexto(reader);
                no.ponteiros.add(filho);
            }
        }
        
        return no;
    }
}

class InserirResultado {
    int chave;
    NoBPlus direita;
    boolean promovido;
    
    public InserirResultado(int chave, NoBPlus direita, boolean promovido) {
        this.chave = chave;
        this.direita = direita;
        this.promovido = promovido;
    }
}



