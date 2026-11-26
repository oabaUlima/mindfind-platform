package pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object que representa la tarifa por sesión en soles peruanos (PEN).
 */
public record TarifaSesion(BigDecimal monto, String moneda) {

    public static final String MONEDA_PEN = "PEN";

    public TarifaSesion {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("La tarifa por sesión debe ser mayor a 0 PEN");
        }
        moneda = (moneda == null || moneda.isBlank()) ? MONEDA_PEN : moneda.trim().toUpperCase();
        if (!MONEDA_PEN.equals(moneda)) {
            throw new IllegalArgumentException("La moneda de la tarifa debe ser PEN");
        }
        monto = monto.setScale(2, RoundingMode.HALF_UP);
    }

    public static TarifaSesion enSoles(BigDecimal monto) {
        return new TarifaSesion(monto, MONEDA_PEN);
    }
}
