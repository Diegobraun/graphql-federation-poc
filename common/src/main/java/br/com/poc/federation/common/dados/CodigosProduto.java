package br.com.poc.federation.common.dados;

import java.util.List;

public final class CodigosProduto {

    public static final String CONTA_ESSENCIAL = "CC-ESSENCIAL";
    public static final String CONTA_PREMIUM = "CC-PREMIUM";
    public static final String POUPANCA = "POUPANCA";

    public static final String EMPRESTIMO_PESSOAL = "EMP-PESSOAL";
    public static final String EMPRESTIMO_CONSIGNADO = "EMP-CONSIGNADO";
    public static final String EMPRESTIMO_VEICULO = "EMP-VEICULO";

    public static final String CDB_LIQUIDEZ = "CDB-LIQUIDEZ";
    public static final String CDB_2_ANOS = "CDB-2A";
    public static final String LCI_90 = "LCI-90";
    public static final String TESOURO_SELIC = "TESOURO-SELIC";
    public static final String FUNDO_MULTIMERCADO = "FUNDO-MM";

    public static final List<String> EMPRESTIMOS = List.of(EMPRESTIMO_PESSOAL, EMPRESTIMO_CONSIGNADO, EMPRESTIMO_VEICULO);
    public static final List<String> INVESTIMENTOS = List.of(CDB_LIQUIDEZ, CDB_2_ANOS, LCI_90, TESOURO_SELIC, FUNDO_MULTIMERCADO);

    private CodigosProduto() {
    }
}
