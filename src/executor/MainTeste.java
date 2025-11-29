package executor;

import java.util.Arrays;
import java.util.List;

public class MainTeste {

    public static void main(String[] args) {
        // 2048 palavras -> 2048 * 3 bytes = 6144 bytes de memória
        Maquina maquina = new Maquina(2048);

        // Programa de teste:
        // 1) LDA #5
        // 2) ADD #3
        // 3) STA 0x100
        // 4) RSUB
        List<String> programa = Arrays.asList(
            "010005",  // LDA #5
            "190003",  // ADD #3
            "0F0100",  // STA 0x0100
            "4F0000"   // RSUB
        );

        // Carrega o programa a partir do endereço 0x0000 (em BYTES)
        maquina.carregarProgramaHex(programa, 0x0000);

        System.out.println("== ESTADO INICIAL ==");
        imprimirEstado(maquina);

        // Executa 4 passos (4 instruções)
        for (int passo = 1; passo <= 4; passo++) {
            System.out.println("\n-- Passo " + passo + " --");
            maquina.passo();
            imprimirEstado(maquina);
        }

        // Depois das 4 instruções:
        // - A deve ser 8
        // - memória em 0x100 deve ser 8
        int resultadoMem = maquina.getMemoria().lerPalavraPorByte(0x0100);

        System.out.printf(
            "%nValor em memoria[0x0100] = %06X (esperado 000008)%n",
            resultadoMem
        );
        System.out.printf(
            "Valor de A = %06X (esperado 000008)%n",
            maquina.getCpu().A().getValorUnsigned()
        );
    }

    private static void imprimirEstado(Maquina m) {
        CPU c = m.getCpu();
        System.out.printf(
            "PC=%06X  A=%06X  X=%06X  L=%06X  B=%06X  S=%06X  T=%06X  CC=%d%n",
            c.PC().getValorUnsigned(),
            c.A().getValorUnsigned(),
            c.X().getValorUnsigned(),
            c.L().getValorUnsigned(),
            c.B().getValorUnsigned(),
            c.S().getValorUnsigned(),
            c.T().getValorUnsigned(),
            c.getCC()
        );
    }
}
