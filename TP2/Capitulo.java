import java.io.*;

// Classe que representa um Capítulo com diversos atributos e métodos relacionados à leitura e escrita de dados.
class Capitulo {
    protected int id; // Identificador do capítulo
    protected Short numCapitulo; // Número do capítulo
    protected Short volume; // Volume ao qual o capítulo pertence
    protected String nome; // Nome do capítulo
    protected String[] titulos; // Array de títulos (ex: Título original, Título em inglês)
    protected byte qtdString; // Quantidade de títulos no array titulos
    protected Short paginas; // Número de páginas do capítulo
    protected String data; // Data de lançamento do capítulo
    protected String episodio; // Episódio relacionado ao capítulo

    // Construtor
    public Capitulo(int id, Short numCapitulo, Short volume, String nome, String[] titulos, Short paginas, String data,
            String episodio) throws IOException {
        this.id = id;
        this.numCapitulo = numCapitulo;
        this.volume = volume;
        this.nome = nome;
        this.qtdString = (byte) titulos.length; 
        this.titulos = titulos;
        this.paginas = paginas;
        this.data = data;
        this.episodio = episodio;
    }

    // Construtor padrão
    public Capitulo() {
        this.id = -1;
        this.numCapitulo = -1;
        this.volume = -1;
        this.nome = "";
        this.titulos = new String[] { "", "" }; 
        this.qtdString = (byte) titulos.length;
        this.paginas = -1;
        this.data = "";
        this.episodio = "";
    }

    //Ver objeto
    @Override
    public String toString() {
        return "ID: " + id 
                + "\n Numero do Capitulo: " + numCapitulo
                + "\n Volume: " + volume
                + "\n Nome: " + nome
                + "\n TituloOriginal: " + titulos[0]
                + "\n TituloIngles: " + titulos[1]
                + "\n Quantidade de Titulos: " + qtdString
                + "\n Paginas: " + paginas
                + "\n Data: " + data
                + "\n Episodio: " + episodio;
    }

    // Método que converte o objeto em um array de bytes (serialização)
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);


        dos.writeInt(id);
        dos.writeShort(numCapitulo);
        dos.writeShort(volume);
        dos.writeUTF(nome);
        dos.writeByte(qtdString);
        for (String titulo : titulos) {
            dos.writeUTF(titulo); 
        }
        dos.writeShort(paginas);
        dos.writeUTF(data);
        dos.writeUTF(episodio);

        return baos.toByteArray(); 
    }

    // Método que converte um array de bytes em um objeto Capitulo (desserialização)
    public void fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

    
        this.id = dis.readInt();
        this.numCapitulo = dis.readShort();
        this.volume = dis.readShort();
        this.nome = dis.readUTF();
        this.qtdString = dis.readByte();
        this.titulos = new String[qtdString];
        for (byte i = 0; i < qtdString; i++) {
            this.titulos[i] = dis.readUTF(); 
        }
        this.paginas = dis.readShort();
        this.data = dis.readUTF();
        this.episodio = dis.readUTF();

        bais.close();
        dis.close();
    }

    // Métodos getters e setters
    public int getId() {
        return id;
    }

    public Short getNumCapitulo() {
        return numCapitulo;
    }

    public Short getVolume() {
        return volume;
    }

    public String getNome() {
        return nome;
    }

    public String[] getTitulos() {
        return titulos;
    }

    public Byte getQtdString() {
        return qtdString;
    }

    public Short getPaginas() {
        return paginas;
    }

    public String getData() {
        return data;
    }

    public String getEpisodio() {
        return episodio;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNumCapitulo(Short numCapitulo) {
        this.numCapitulo = numCapitulo;
    }

    public void setVolume(Short volume) {
        this.volume = volume;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setTitulos(String[] titulos) {
        this.titulos = titulos;
        this.qtdString = (byte) titulos.length; 
    }

    public void setPaginas(Short paginas) {
        this.paginas = paginas;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setEpisodio(String episodio) {
        this.episodio = episodio;
    }
}
