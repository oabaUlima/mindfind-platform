package pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object que representa el porcentaje de compatibilidad entre 0.0 y 100.0, redondeado a 1 decimal.
 */
public record PorcentajeCompatibilidad(double valor) {

    public PorcentajeCompatibilidad {
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < 0.0 || valor > 100.0) {
            throw new IllegalArgumentException("El porcentaje de compatibilidad debe estar entre 0.0 y 100.0");
        }
        valor = BigDecimal.valueOf(valor).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
