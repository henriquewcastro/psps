package executor;

public interface Opcode {

    // formato 3/4 (memória)
    int LDA   = 0x00;
    int LDX   = 0x04;
    int LDL   = 0x08;
    int STA   = 0x0C;
    int STX   = 0x10;
    int STL   = 0x14;

    int ADD   = 0x18;
    int SUB   = 0x1C;
    int MUL   = 0x20;
    int DIV   = 0x24;
    int COMP  = 0x28;
    int TIX   = 0x2C;

    int JEQ   = 0x30;
    int JGT   = 0x34;
    int JLT   = 0x38;
    int J     = 0x3C;

    int AND   = 0x40;
    int OR    = 0x44;
    int LDCH  = 0x50;
    int STCH  = 0x54;

    int JSUB  = 0x48;
    int RSUB  = 0x4C;

    int LDB   = 0x68;
    int LDS   = 0x6C;
    int LDT   = 0x74;
    int STB   = 0x78;
    int STS   = 0x7C;
    int STT   = 0x84;

    // formato 2 (registrador–registrador / shifts)
    int ADDR  = 0x90;
    int SUBR  = 0x94;
    int MULR  = 0x98;
    int DIVR  = 0x9C;
    int COMPR = 0xA0;
    int SHIFTL = 0xA4;
    int SHIFTR = 0xA8;
    // RMO = 0xAC se quiser
    int CLEAR = 0xB4;
    int TIXR  = 0xB8;
}
