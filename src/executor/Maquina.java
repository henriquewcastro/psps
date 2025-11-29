package executor;

import java.util.List;

public class Maquina {

    private final CPU cpu;
    private final Memoria memoria;

    public Maquina(int numPalavrasMemoria) {
        this.cpu = new CPU();
        this.memoria = new Memoria(numPalavrasMemoria);
    }

    public CPU getCpu() {
        return cpu;
    }

    public Memoria getMemoria() {
        return memoria;
    }

    /**
     * Carrega um programa em HEX (cada linha = 3 bytes = 6 hex) a partir de um endereço em bytes.
     * Exemplo de linha: "00100A".
     */
    public void carregarProgramaHex(List<String> linhas, int enderecoInicialByte) {
        int addr = enderecoInicialByte;

        for (String linha : linhas) {
            String hex = linha.trim();
            if (hex.isEmpty()) continue;

            int valor = Integer.parseInt(hex, 16); // 24 bits
            memoria.escreverPalavraPorByte(addr, valor);
            addr += 3;
        }

        cpu.PC().setValor(enderecoInicialByte); // PC em bytes
    }

    /**
     * Executa UM passo da CPU (uma instrução).
     * Use isso no botão "Step" da interface.
     */
    public void passo() {
        int pc = cpu.PC().getValorUnsigned();

        // 1) busca e decodifica
        Instrucao inst = Instrucao.decodificar(memoria, pc);
        int proximoPC = (pc + inst.tamanhoBytes) & 0xFFFFFF;

        // 2) executa
        executarInstrucao(inst, proximoPC);
    }

    private void executarInstrucao(Instrucao inst, int proximoPC) {
        int op = inst.opcode;

        switch (op) {
            // ======== formato 3/4 (memória) =========

            case Opcode.LDA: {
                int operando = lerOperandoMemoria(inst, proximoPC);
                cpu.A().setValor(operando);
                cpu.PC().setValor(proximoPC);
                break;
            }

            case Opcode.STA: {
                int ea = calcularEnderecoEfetivo(inst, proximoPC);
                memoria.escreverPalavraPorByte(ea, cpu.A().getValorUnsigned());
                cpu.PC().setValor(proximoPC);
                break;
            }

            case Opcode.ADD: {
                int operando = lerOperandoMemoria(inst, proximoPC);
                int resultado = cpu.A().getValor() + operando;
                cpu.A().setValor(resultado);
                cpu.PC().setValor(proximoPC);
                break;
            }

            case Opcode.SUB: {
                int operando = lerOperandoMemoria(inst, proximoPC);
                int resultado = cpu.A().getValor() - operando;
                cpu.A().setValor(resultado);
                cpu.PC().setValor(proximoPC);
                break;
            }

            case Opcode.COMP: {
                int operando = lerOperandoMemoria(inst, proximoPC);
                int resultado = cpu.A().getValor() - operando;
                cpu.setCCFromCompare(resultado);
                cpu.PC().setValor(proximoPC);
                break;
            }

            case Opcode.J: {
                int ea = calcularEnderecoEfetivo(inst, proximoPC);
                cpu.PC().setValor(ea);
                break;
            }

            case Opcode.JEQ: {
                int ea = calcularEnderecoEfetivo(inst, proximoPC);
                if (cpu.getCC() == 0) {
                    cpu.PC().setValor(ea);
                } else {
                    cpu.PC().setValor(proximoPC);
                }
                break;
            }

            case Opcode.JSUB: {
                int ea = calcularEnderecoEfetivo(inst, proximoPC);
                cpu.L().setValor(proximoPC);
                cpu.PC().setValor(ea);
                break;
            }

            case Opcode.RSUB: {
                int newPC = cpu.L().getValorUnsigned();
                cpu.PC().setValor(newPC);
                break;
            }

            case Opcode.TIX: {
                int ea = calcularEnderecoEfetivo(inst, proximoPC);
                int m = memoria.lerPalavraPorByte(ea);

                int novoX = cpu.X().getValor() + 1;
                cpu.X().setValor(novoX);

                int resultado = cpu.X().getValor() - m;
                cpu.setCCFromCompare(resultado);

                cpu.PC().setValor(proximoPC);
                break;
            }

            // ======== formato 2 =========

            case Opcode.CLEAR: {
                Registrador r1 = cpu.getRegistradorPorCodigo(inst.r1);
                if (r1 != null) {
                    r1.limpar();
                }
                cpu.PC().setValor(proximoPC);
                break;
            }

            case Opcode.TIXR: {
                Registrador r1 = cpu.getRegistradorPorCodigo(inst.r1);
                int novoX = cpu.X().getValor() + 1;
                cpu.X().setValor(novoX);

                if (r1 != null) {
                    int resultado = cpu.X().getValor() - r1.getValor();
                    cpu.setCCFromCompare(resultado);
                }
                cpu.PC().setValor(proximoPC);
                break;
            }

            default:
                // por enquanto, se cair aqui, não implementado
                throw new UnsupportedOperationException(
                    String.format("Opcode não implementado: 0x%02X", op)
                );
        }
    }

    /**
     * Calcula endereço efetivo (EA) para formato 3/4.
     */
    private int calcularEnderecoEfetivo(Instrucao inst, int proximoPC) {
        if (inst.formato != 3 && inst.formato != 4) {
            throw new IllegalArgumentException("Endereço efetivo só para formatos 3/4.");
        }

        int ea;

        if (inst.formato == 3) {
            int disp = inst.disp; // já com sinal (32 bits)
            if (inst.p == 1) {
                ea = proximoPC + disp; // PC-relative
            } else if (inst.b == 1) {
                ea = cpu.B().getValorUnsigned() + disp;
            } else {
                ea = disp & 0xFFFFF; // direto
            }
        } else {
            // formato 4 – disp = endereço de 20 bits
            int addr = inst.disp & 0xFFFFF;
            if (inst.b == 1) {
                ea = cpu.B().getValorUnsigned() + addr;
            } else if (inst.p == 1) {
                ea = proximoPC + addr; // nem sempre usado, mas deixo aqui
            } else {
                ea = addr;
            }
        }

        if (inst.x == 1) {
            ea += cpu.X().getValorUnsigned();
        }

        return ea & 0xFFFFFF;
    }

    /**
     * Lê o OPERANDO para instruções de memória (LDA, ADD, COMP, etc.)
     * Considera simples, imediato (#), indireto (@).
     */
    private int lerOperandoMemoria(Instrucao inst, int proximoPC) {
        int ea = calcularEnderecoEfetivo(inst, proximoPC);

        boolean n = inst.n == 1;
        boolean i = inst.i == 1;

        if (n && i) { // simples (direto)
            return memoria.lerPalavraPorByte(ea);
        } else if (!n && i) { // imediato
            return ea; // o valor é o próprio campo
        } else if (n && !i) { // indireto
            int ptr = memoria.lerPalavraPorByte(ea);
            return memoria.lerPalavraPorByte(ptr);
        } else {
            // caso estranho (0,0), trata como simples
            return memoria.lerPalavraPorByte(ea);
        }
    }
}
