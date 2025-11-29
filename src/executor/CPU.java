package executor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CPU da máquina SIC/XE (sem registrador de ponto flutuante).
 */
public class CPU {

    public static final String REG_A  = "A";
    public static final String REG_X  = "X";
    public static final String REG_L  = "L";
    public static final String REG_B  = "B";
    public static final String REG_S  = "S";
    public static final String REG_T  = "T";
    public static final String REG_PC = "PC";
    public static final String REG_SW = "SW"; // usado para guardar o CC (-1, 0, 1)

    private final Map<String, Registrador> registradores = new LinkedHashMap<>();

    public CPU() {
        // códigos 0..5 batendo com a tabela de registradores do SIC/XE
        registradores.put(REG_A,  new Registrador(REG_A, 0));
        registradores.put(REG_X,  new Registrador(REG_X, 1));
        registradores.put(REG_L,  new Registrador(REG_L, 2));
        registradores.put(REG_B,  new Registrador(REG_B, 3));
        registradores.put(REG_S,  new Registrador(REG_S, 4));
        registradores.put(REG_T,  new Registrador(REG_T, 5));
        // códigos especiais para PC e SW (se quiser usar)
        registradores.put(REG_PC, new Registrador(REG_PC, -1));
        registradores.put(REG_SW, new Registrador(REG_SW, -2));
    }

    public Map<String, Registrador> getMapaRegistradores() {
        return registradores;
    }

    public Registrador getRegistrador(String nome) {
        Registrador r = registradores.get(nome);
        if (r == null) {
            throw new IllegalArgumentException("Registrador inexistente: " + nome);
        }
        return r;
    }

    // atalhos úteis
    public Registrador A()  { return getRegistrador(REG_A); }
    public Registrador X()  { return getRegistrador(REG_X); }
    public Registrador L()  { return getRegistrador(REG_L); }
    public Registrador B()  { return getRegistrador(REG_B); }
    public Registrador S()  { return getRegistrador(REG_S); }
    public Registrador T()  { return getRegistrador(REG_T); }
    public Registrador PC() { return getRegistrador(REG_PC); }
    public Registrador SW() { return getRegistrador(REG_SW); }

    // =========================
    //  CÓDIGO DE CONDIÇÃO (CC)
    // =========================

    /**
     * Define o CC (-1, 0, 1) com base no resultado de uma comparação.
     * valor < 0 → CC = -1  (menor)
     * valor = 0 → CC = 0   (igual)
     * valor > 0 → CC = 1   (maior)
     */
    public void setCCFromCompare(int resultado) {
        int cc;
        if (resultado < 0)      cc = -1;
        else if (resultado > 0) cc = 1;
        else                    cc = 0;
        SW().setValor(cc);
    }

    /**
     * Obtém o CC atual (-1, 0, 1) a partir do registrador SW.
     */
    public int getCC() {
        return SW().getValor(); // vai devolver -1, 0 ou 1
    }

    /**
     * Zera todos os registradores (exceto talvez PC, se quiser tratar separado depois).
     */
    public void limparTodos() {
        for (Registrador r : registradores.values()) {
            r.limpar();
        }
    }

    public Registrador getRegistradorPorCodigo(int codigo) {
        switch (codigo) {
            case 0: return A();
            case 1: return X();
            case 2: return L();
            case 3: return B();
            case 4: return S();
            case 5: return T();
            // 6 seria F; 8, PC; 9, SW (se quiser tratar)
            default: return null;
        }
    }


}
