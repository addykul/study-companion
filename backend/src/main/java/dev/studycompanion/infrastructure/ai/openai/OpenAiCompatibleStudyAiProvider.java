package dev.studycompanion.infrastructure.ai.openai;

import dev.studycompanion.application.port.StudyAiProvider;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Spring AI adapter for OpenAI-compatible model APIs, including OpenRouter. */
@Component
@Profile("openrouter")
public class OpenAiCompatibleStudyAiProvider implements StudyAiProvider {
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    public OpenAiCompatibleStudyAiProvider(ChatClient.Builder chatClientBuilder,
                                          EmbeddingModel embeddingModel) {
        this.chatClient = chatClientBuilder.build();
        this.embeddingModel = embeddingModel;
    }

    @Override
    public String generateText(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    @Override
    public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(responseType);
    }

    @Override
    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        return embeddingModel.embed(texts);
    }
}
