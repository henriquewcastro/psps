package executor;

public class Instrucao {

    public int opcode;       // 8 bits (com 2 LSB = 0)
    public int formato;      // 1, 2, 3 ou 4
    public int n, i, x, b, p, e;
    public int disp;         // deslocamento já com sinal (formato 3) ou endereço (formato 4)
    public int r1, r2;       // formato 2
    public int tamanhoBytes; // 1..4

    public static Instrucao decodificar(Memoria memoria, int pcByte) {
        Instrucao inst = new Instrucao();

        int b1 = memoria.lerByte(pcByte);
        int op = b1 & 0xFC; // zera 2 bits menos significativos

        inst.opcode = op;

        int formatoBase = getFormatoBase(op);

        if (formatoBase == 1) {
            inst.formato = 1;
            inst.tamanhoBytes = 1;
            return inst;
        }

        if (formatoBase == 2) {
            inst.formato = 2;
            int b2 = memoria.lerByte(pcByte + 1);
            inst.r1 = (b2 >> 4) & 0x0F;
            inst.r2 = b2 & 0x0F;
            inst.tamanhoBytes = 2;
            return inst;
        }

        // formato 3/4: precisamos dos próximos bytes
        int b2 = memoria.lerByte(pcByte + 1);
        int b3 = memoria.lerByte(pcByte + 2);

        inst.n = (b1 >> 1) & 0x1;
        inst.i = b1 & 0x1;

        inst.x = (b2 >> 7) & 0x1;
        inst.b = (b2 >> 6) & 0x1;
        inst.p = (b2 >> 5) & 0x1;
        inst.e = (b2 >> 4) & 0x1;

        if (inst.e == 0) {
            // formato 3
            inst.formato = 3;
            int disp12 = ((b2 & 0x0F) << 8) | b3;
            // sinal: bit 11
            if ((disp12 & 0x800) != 0) {
                disp12 |= 0xFFFFF000; // estende para 32 bits
            }
            inst.disp = disp12;
            inst.tamanhoBytes = 3;
        } else {
            // formato 4 – precisamos do 4º byte
            inst.formato = 4;
            int b4 = memoria.lerByte(pcByte + 3);
            int addr20 = ((b2 & 0x0F) << 16) | (b3 << 8) | b4;
            inst.disp = addr20; // usamos disp como endereço direto
            inst.tamanhoBytes = 4;
        }

        return inst;
    }

    /**
     * Retorna o formato base da instrução com esse opcode:
     * 1, 2 ou 3 (3 significa 3/4).
     * Aqui colocamos só as que estamos usando.
     */
    private static int getFormatoBase(int opcode) {
        switch (opcode) {
            // formato 3/4
            case Opcode.LDA:
            case Opcode.STA:
            case Opcode.ADD:
            case Opcode.SUB:
            case Opcode.COMP:
            case Opcode.J:
            case Opcode.JEQ:
            case Opcode.JSUB:
            case Opcode.RSUB:
            case Opcode.TIX:
                return 3;

            // formato 2
            case Opcode.CLEAR:
            case Opcode.TIXR:
                return 2;

            // se tivesse formato 1, colocaria aqui
            default:
                // por padrão, assume 3/4
                return 3;
        }
    }
}
