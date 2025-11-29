package executor;

/**
 * Representa um registrador de 24 bits da máquina SIC/XE.
 * Não trata ponto flutuante (F não será usado neste projeto).
 */
public class Registrador {

    private final String nome;
    private final int codigo; // opcional: 0=A, 1=X, ...; use -1, -2 para PC, SW se quiser
    private int valor24bits;  // apenas os 24 bits menos significativos são usados

    public Registrador(String nome, int codigo) {
        this.nome = nome;
        this.codigo = codigo;
        this.valor24bits = 0;
    }

    public String getNome() {
        return nome;
    }

    public int getCodigo() {
        return codigo;
    }

    /**
     * Retorna o valor como inteiro com sinal (24 bits, sinal estendido pra 32).
     */
    public int getValor() {
        int v = valor24bits & 0xFFFFFF;
        // se o bit de sinal (bit 23) estiver 1, estende o sinal para 32 bits
        if ((v & 0x800000) != 0) {
            v |= 0xFF000000;
        }
        return v;
    }

    /**
     * Retorna o valor como inteiro sem sinal (0 a 0xFFFFFF).
     */
    public int getValorUnsigned() {
        return valor24bits & 0xFFFFFF;
    }

    /**
     * Define o valor do registrador (apenas 24 bits são mantidos).
     */
    public void setValor(int novoValor) {
        this.valor24bits = novoValor & 0xFFFFFF;
    }

    /**
     * Zera o registrador (CLEAR).
     */
    public void limpar() {
        this.valor24bits = 0;
    }

    @Override
    public String toString() {
        return nome + " = 0x" + String.format("%06X", getValorUnsigned());
    }
}
