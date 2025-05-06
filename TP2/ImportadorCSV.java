import java.io.*;
import java.text.ParseException;

public class ImportadorCSV {

    public static void importarCSVParaBinario() throws IOException, ParseException {
        String csv = "dataset/capitulos.csv";
        String binario = "dataset/capitulos.db";

        BufferedReader br = new BufferedReader(new FileReader(csv));
        RandomAccessFile raf = new RandomAccessFile(binario, "rw");

        // Cabeçalho: reserva 4 bytes para último ID inserido
        raf.setLength(0); // Limpa arquivo
        int ultimoId = 0;
        raf.writeInt(ultimoId);

        String linha;
        while ((linha = br.readLine()) != null) {
            String[] campos = AuxFuncoes.separarPorVirgula(linha);

            Short numeroCapitulo = Short.parseShort(campos[0]);
            int id = numeroCapitulo;
            Short volume = Short.parseShort(campos[1]);
            String nome = campos[2];
            String[] titulos = { campos[3], campos[4] };
            Short paginas = Short.parseShort(campos[5]);
            String data = AuxFuncoes.formatarData(campos[6]);
            String episodio = campos[7];

            Capitulo capitulo = new Capitulo(id, numeroCapitulo, volume, nome, titulos, paginas, data, episodio);
            byte[] dataBytes = capitulo.toByteArray();

            // Escreve capítulo válido (valido = 1, tamanho, dados)
            raf.seek(raf.length());
            raf.writeByte(1); // válido
            raf.writeInt(dataBytes.length);
            raf.write(dataBytes);

            ultimoId = id;
        }

        // Atualiza o cabeçalho com o último ID inserido
        raf.seek(0);
        raf.writeInt(ultimoId);

        br.close();
        raf.close();
    }
}
