import java.io.*;
import java.util.*;

public class Hashing {
    private List<Bucket> diretorio;
    private int profundidadeGlobal;

    public Hashing() {
        this.diretorio = new ArrayList<>();
        this.profundidadeGlobal = 1;

        // Inicializa com dois buckets
        Bucket b0 = new Bucket(1);
        Bucket b1 = new Bucket(1);
        diretorio.add(b0);
        diretorio.add(b1);
    }

    private int hash(int id) {
        return id & ((1 << profundidadeGlobal) - 1);
    }

    public void inserir(int id, long posicao) {
        int h = hash(id);
        Bucket bucket = diretorio.get(h);

        if (!bucket.cheio()) {
            bucket.inserir(id, posicao);
        } else {
            dividirBucket(h);
            inserir(id, posicao); // tenta de novo após divisão
        }
    }

    private void dividirBucket(int indice) {
        Bucket antigo = diretorio.get(indice);
        int novaProfundidade = antigo.profundidadeLocal + 1;

        if (novaProfundidade > profundidadeGlobal) {
            duplicarDiretorio();
        }

        Bucket novo = new Bucket(novaProfundidade);

        List<Integer> ids = new ArrayList<>(antigo.ids);
        List<Long> posicoes = new ArrayList<>(antigo.posicoes);

        antigo.clear();
        antigo.profundidadeLocal = novaProfundidade;

        int mask = (1 << novaProfundidade) - 1;

        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            long pos = posicoes.get(i);
            if ((id & mask) == (indice | (1 << (novaProfundidade - 1)))) {
                novo.inserir(id, pos);
            } else {
                antigo.inserir(id, pos);
            }
        }

        // Atualiza ponteiros no diretório
        for (int i = 0; i < diretorio.size(); i++) {
            if (diretorio.get(i) == antigo) {
                int hash = i & ((1 << novaProfundidade) - 1);
                if ((hash & (1 << (novaProfundidade - 1))) != 0) {
                    diretorio.set(i, novo);
                }
            }
        }
    }

    private void duplicarDiretorio() {
        int tamanhoAtual = diretorio.size();
        for (int i = 0; i < tamanhoAtual; i++) {
            diretorio.add(diretorio.get(i));
        }
        profundidadeGlobal++;
    }

    public Long buscar(int id) {
        int h = hash(id);
        Bucket b = diretorio.get(h);
        return b.buscar(id);
    }

    public void remover(int id) {
        int h = hash(id);
        Bucket b = diretorio.get(h);
        b.remover(id);
    }

    public void salvarIndice(String caminho) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminho))) {
            writer.write("PROFUNDIDADE_GLOBAL:" + profundidadeGlobal + "\n");

            Map<Bucket, Integer> bucketIds = new HashMap<>();
            int nextBucketId = 0;

            // Mapeia cada bucket para um ID
            for (Bucket b : diretorio) {
                if (!bucketIds.containsKey(b)) {
                    bucketIds.put(b, nextBucketId++);
                }
            }

            // Salva buckets
            for (Map.Entry<Bucket, Integer> entry : bucketIds.entrySet()) {
                Bucket b = entry.getKey();
                int idBucket = entry.getValue();

                writer.write("BUCKET:" + idBucket + ":" + b.profundidadeLocal + "\n");
                for (int i = 0; i < b.ids.size(); i++) {
                    writer.write(b.ids.get(i) + "," + b.posicoes.get(i) + "\n");
                }
            }
        }
    }

    public void carregarIndice(String caminho) throws IOException {
        File arquivo = new File(caminho);
        if (!arquivo.exists()) return;

        BufferedReader reader = new BufferedReader(new FileReader(caminho));
        String linha;

        linha = reader.readLine();
        profundidadeGlobal = Integer.parseInt(linha.split(":")[1]);

        linha = reader.readLine(); // DIRETORIO:
        String[] dirMap = linha.split(":")[1].split(";");
        int dirSize = 1 << profundidadeGlobal;
        diretorio = new ArrayList<>(Collections.nCopies(dirSize, (Bucket) null));

        Map<Integer, Bucket> idParaBucket = new HashMap<>();
        Bucket atual = null;

        while ((linha = reader.readLine()) != null) {
            if (linha.startsWith("BUCKET:")) {
                String[] partes = linha.split(":");
                int bucketId = Integer.parseInt(partes[1]);
                int profundidadeLocal = Integer.parseInt(partes[2]);

                atual = new Bucket(profundidadeLocal);
                idParaBucket.put(bucketId, atual);
            } else {
                String[] partes = linha.split(",");
                atual.inserir(Integer.parseInt(partes[0]), Long.parseLong(partes[1]));
            }
        }

        // Reconstrói o diretório
        for (int i = 0; i < dirMap.length; i++) {
            int id = Integer.parseInt(dirMap[i]);
            diretorio.set(i, idParaBucket.get(id));
        }

        reader.close();
    }
}

class Bucket {
    public List<Integer> ids;
    public List<Long> posicoes;
    public int profundidadeLocal;
    private static final int MAX = 4;

    public Bucket(int profLocal) {
        this.profundidadeLocal = profLocal;
        this.ids = new ArrayList<>();
        this.posicoes = new ArrayList<>();
    }

    public boolean cheio() {
        return ids.size() >= MAX;
    }

    public void inserir(int id, long posicao) {
        ids.add(id);
        posicoes.add(posicao);
    }

    public Long buscar(int id) {
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == id) return posicoes.get(i);
        }
        return null;
    }

    public void remover(int id) {
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == id) {
                ids.remove(i);
                posicoes.remove(i);
                return;
            }
        }
    }

    public void clear() {
        ids.clear();
        posicoes.clear();
    }
}
