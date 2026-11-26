package pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object que representa el presupuesto máximo por sesión del paciente en PEN.
 */
public record PresupuestoMaximo(BigDecimal monto, String moneda) {

    public static final String MONEDA_PEN = "PEN";

    public PresupuestoMaximo {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El presupuesto máximo debe ser mayor a 0 PEN");
        }
        moneda = (moneda == null || moneda.isBlank()) ? MONEDA_PEN : moneda.trim().toUpperCase();
        if (!MONEDA_PEN.equals(moneda)) {
            throw new IllegalArgumentException("La moneda del presupuesto debe ser PEN");
        }
        monto = monto.setScale(2, RoundingMode.HALF_UP);
    }

    public static PresupuestoMaximo enSoles(BigDecimal monto) {
        return new PresupuestoMaximo(monto, MONEDA_PEN);
    }
}
