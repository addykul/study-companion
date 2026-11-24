package dev.studycompanion.application.port;

import java.util.List;

/** Outbound application port for the model capabilities used by study workflows. */
public interface StudyAiProvider {
    String generateText(String systemPrompt, String userPrompt);

    <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType);

    float[] embed(String text);

    List<float[]> embedAll(List<String> texts);
}
