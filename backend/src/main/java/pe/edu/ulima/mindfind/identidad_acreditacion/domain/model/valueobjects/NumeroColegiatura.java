package pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects;

import java.util.regex.Pattern;

/**
 * Value Object que representa el número de colegiatura profesional (4 a 6 dígitos numéricos).
 */
public record NumeroColegiatura(String value) {

    private static final Pattern VALID_PATTERN = Pattern.compile("^\\d{4,6}$");
    private static final String INVALID_MESSAGE = "Debe ingresar un número de colegiatura válido";

    public NumeroColegiatura {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE);
        }
        var trimmed = value.trim();
        if (!VALID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(INVALID_MESSAGE);
        }
        value = trimmed;
    }
}
