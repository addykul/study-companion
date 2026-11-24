package dev.studycompanion.infrastructure.persistence.jdbc;

public final class PgVectorFormatter {
    private PgVectorFormatter() {}

    public static String toLiteral(float[] vector) {
        StringBuilder literal = new StringBuilder("[");
        for (int index = 0; index < vector.length; index++) {
            if (index > 0) literal.append(',');
            literal.append(vector[index]);
        }
        return literal.append(']').toString();
    }
}
