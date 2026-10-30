package br.com.orcamento.service;

import br.com.orcamento.tools.OrcamentoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * CHATCLIENT: e o objeto do Spring AI que conversa com o modelo de IA.
 * Aqui ele recebe a mensagem da pessoa, tem acesso as tools e devolve a resposta em texto.
 */
@Service
public class AssistenteService {

    private static final String INSTRUCOES = """
            Voce e um assistente de orcamento pessoal.
            Responda sempre em portugues do Brasil, em frases curtas e sem formatacao,
            porque a resposta pode ser lida em voz alta.
            Para registrar ou consultar despesas, use SEMPRE as ferramentas disponiveis.
            Nunca invente valores: use apenas o que as ferramentas devolverem.
            """;

    private final ChatClient chatClient;
    private final OrcamentoTools ferramentas;

    public AssistenteService(ChatClient.Builder builder, OrcamentoTools ferramentas) {
        this.chatClient = builder.defaultSystem(INSTRUCOES).build();
        this.ferramentas = ferramentas;
    }

    public String responder(String mensagem) {
        return chatClient.prompt()
                .user(mensagem)
                .tools(ferramentas)
                .call()
                .content();
    }
}
