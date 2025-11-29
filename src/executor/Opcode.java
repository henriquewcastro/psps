package executor;

/**
 * OpCodes principais do SIC/XE (só alguns, o resto você pode ir adicionando).
 * Valor aqui é o opcode "normalizado" (8 bits, com 2 bits menos significativos = 0).
 */
public interface Opcode {

    int LDA   = 0x00;
    int STA   = 0x0C;
    int ADD   = 0x18;
    int SUB   = 0x1C;
    int COMP  = 0x28;
    int J     = 0x3C;
    int JEQ   = 0x30;
    int JSUB  = 0x48;
    int RSUB  = 0x4C;
    int TIX   = 0x2C;

    int CLEAR = 0xB4; // formato 2
    int TIXR  = 0xB8; // formato 2

    // ...adicione outros aqui se precisar
}
