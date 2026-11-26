package pe.edu.ulima.mindfind.triage_matching.domain.services;

import pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates.EvaluacionTriage;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PorcentajeCompatibilidad;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PsicologoRef;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoCompatibilidad;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Domain Service puro (Java sin Spring/JPA) encargado de calcular el ranking Top 5
 * de psicólogos verificados según criterios clínicos ponderados (HU-05).
 */
public class MotorMatching {

    private static final double PESO_ESPECIALIDAD = 40.0;
    private static final double PESO_MODALIDAD = 20.0;
    private static final double PESO_PRESUPUESTO = 20.0;
    private static final double PESO_ENFOQUE = 10.0;
    private static final double PESO_DISPONIBILIDAD = 10.0;
    private static final int TOP_LIMIT = 5;

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern HOUR_PATTERN = Pattern.compile("(\\d{1,2}):\\d{2}");

    private static final Set<String> STOP_WORDS = Set.of(
            "para", "como", "todo", "toda", "esta", "este", "tengo", "mucho", "mucha",
            "poco", "desde", "hace", "sobre", "entre", "ante", "algo", "unos", "unas"
    );

    private static final List<String> DIAS_SEMANA = List.of(
            "lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"
    );

    public List<ResultadoCompatibilidad> calcularTop5(EvaluacionTriage evaluacion, List<PsicologoRef> psicologos) {
        Objects.requireNonNull(evaluacion, "La evaluación de triage es obligatoria");
        if (psicologos == null || psicologos.isEmpty()) {
            return List.of();
        }

        var evaluados = psicologos.stream()
                .filter(Objects::nonNull)
                .filter(PsicologoRef::verificado)
                .map(psicologo -> evaluarPsicologo(evaluacion, psicologo))
                .sorted(Comparator
                        .comparingDouble((ResultadoCompatibilidad r) -> r.porcentajeCompatibilidad().valor()).reversed()
                        .thenComparing(ResultadoCompatibilidad::tarifaPorSesion, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ResultadoCompatibilidad::idPsicologo))
                .limit(TOP_LIMIT)
                .toList();

        var ranked = new ArrayList<ResultadoCompatibilidad>(evaluados.size());
        for (int i = 0; i < evaluados.size(); i++) {
            var item = evaluados.get(i);
            ranked.add(new ResultadoCompatibilidad(
                    item.idPsicologo(),
                    item.nombrePsicologo(),
                    item.enfoqueTeorico(),
                    item.especialidades(),
                    item.modalidadesAtencion(),
                    item.tarifaPorSesion(),
                    item.disponibilidadHoraria(),
                    item.porcentajeCompatibilidad(),
                    i + 1,
                    item.esMatchParcial(),
                    item.razonesCompatibilidad()));
        }
        return List.copyOf(ranked);
    }

    private ResultadoCompatibilidad evaluarPsicologo(EvaluacionTriage evaluacion, PsicologoRef psicologo) {
        var razones = new ArrayList<String>();

        // 1. Especialidades vs síntomas/motivo (40%)
        var resultadoEspecialidad = evaluarEspecialidades(evaluacion, psicologo, razones);

        // 2. Modalidad de atención (20%)
        boolean modalidadCompleta = psicologo.modalidadesAtencion().contains(evaluacion.getModalidadPreferida());
        double puntajeModalidad = modalidadCompleta ? PESO_MODALIDAD : 0.0;
        if (modalidadCompleta) {
            razones.add("Atiende en tu modalidad preferida (" + evaluacion.getModalidadPreferida().name() + ")");
        } else {
            razones.add("No atiende en modalidad " + evaluacion.getModalidadPreferida().name());
        }

        // 3. Presupuesto máximo vs tarifa por sesión (20%)
        var resultadoPresupuesto = evaluarPresupuesto(evaluacion.getPresupuestoMaximo().monto(), psicologo.tarifaPorSesion(), razones);

        // 4. Enfoque teórico preferido (10% — puntaje neutro favorable si no especifica)
        var resultadoEnfoque = evaluarEnfoque(evaluacion.getEnfoquePreferido(), psicologo.enfoqueTeorico(), razones);

        // 5. Disponibilidad horaria (10%)
        var resultadoDisponibilidad = evaluarDisponibilidad(evaluacion.getDisponibilidadPreferida(), psicologo.disponibilidadHoraria(), razones);

        double total = resultadoEspecialidad.puntaje()
                + puntajeModalidad
                + resultadoPresupuesto.puntaje()
                + resultadoEnfoque.puntaje()
                + resultadoDisponibilidad.puntaje();

        boolean esMatchParcial = !(resultadoEspecialidad.completo()
                && modalidadCompleta
                && resultadoPresupuesto.completo()
                && resultadoEnfoque.completo()
                && resultadoDisponibilidad.completo());

        return new ResultadoCompatibilidad(
                psicologo.idPsicologo(),
                psicologo.nombreCompleto(),
                psicologo.enfoqueTeorico(),
                psicologo.especialidades(),
                psicologo.modalidadesAtencion(),
                psicologo.tarifaPorSesion(),
                psicologo.disponibilidadHoraria(),
                new PorcentajeCompatibilidad(total),
                1,
                esMatchParcial,
                razones);
    }

    private CriterioPuntaje evaluarEspecialidades(
            EvaluacionTriage evaluacion,
            PsicologoRef psicologo,
            List<String> razones) {

        var especialidadesNorm = psicologo.especialidades().stream()
                .map(MotorMatching::normalizar)
                .filter(s -> !s.isBlank())
                .toList();

        var sintomas = evaluacion.getSintomasDeclarados();
        var sintomasCoincidentes = new ArrayList<String>();
        for (var sintoma : sintomas) {
            var sintNorm = normalizar(sintoma);
            boolean match = especialidadesNorm.stream().anyMatch(esp -> coincideTextoClinico(esp, sintNorm));
            if (match) {
                sintomasCoincidentes.add(sintoma);
            }
        }

        var motivoNorm = normalizar(evaluacion.getMotivoConsulta());
        boolean coincideMotivo = especialidadesNorm.stream().anyMatch(esp -> coincideTextoClinico(esp, motivoNorm));

        int totalSintomas = sintomas.size();
        int matchedCount = sintomasCoincidentes.size();

        if (matchedCount == totalSintomas) {
            razones.add("Especialidad alineada con todos tus síntomas: " + String.join(", ", sintomasCoincidentes));
            return new CriterioPuntaje(PESO_ESPECIALIDAD, true);
        }
        if (matchedCount > 0) {
            double base = PESO_ESPECIALIDAD * ((double) matchedCount / totalSintomas);
            double puntaje = coincideMotivo ? Math.min(PESO_ESPECIALIDAD - 1.0, base + 4.0) : base;
            razones.add("Coincidencia parcial de especialidades (" + matchedCount + " de " + totalSintomas + " síntomas: "
                    + String.join(", ", sintomasCoincidentes) + ")");
            return new CriterioPuntaje(puntaje, false);
        }
        if (coincideMotivo) {
            razones.add("Especialidad relacionada con tu motivo de consulta");
            return new CriterioPuntaje(PESO_ESPECIALIDAD * 0.5, false);
        }
        razones.add("Sin coincidencia directa con tus síntomas declarados");
        return new CriterioPuntaje(0.0, false);
    }

    private CriterioPuntaje evaluarPresupuesto(BigDecimal presupuestoMaximo, BigDecimal tarifa, List<String> razones) {
        if (tarifa == null) {
            razones.add("Tarifa por sesión no especificada");
            return new CriterioPuntaje(0.0, false);
        }
        if (tarifa.compareTo(presupuestoMaximo) <= 0) {
            razones.add("Tarifa S/ " + tarifa + " dentro de tu presupuesto máximo (S/ " + presupuestoMaximo + ")");
            return new CriterioPuntaje(PESO_PRESUPUESTO, true);
        }
        BigDecimal limiteTolerancia = presupuestoMaximo.multiply(new BigDecimal("1.25"));
        if (tarifa.compareTo(limiteTolerancia) <= 0) {
            double excesoRatio = tarifa.subtract(presupuestoMaximo).doubleValue() / presupuestoMaximo.doubleValue();
            double puntaje = Math.max(5.0, PESO_PRESUPUESTO * (1.0 - (excesoRatio * 2.0)));
            razones.add("Tarifa S/ " + tarifa + " supera levemente tu presupuesto de S/ " + presupuestoMaximo);
            return new CriterioPuntaje(puntaje, false);
        }
        razones.add("Tarifa S/ " + tarifa + " excede tu presupuesto de S/ " + presupuestoMaximo);
        return new CriterioPuntaje(0.0, false);
    }

    private CriterioPuntaje evaluarEnfoque(String enfoquePreferido, String enfoquePsicologo, List<String> razones) {
        var prefNorm = normalizar(enfoquePreferido);
        if (prefNorm.isBlank()
                || prefNorm.equals("sin preferencia")
                || prefNorm.equals("ninguno")
                || prefNorm.equals("no especifica")
                || prefNorm.equals("cualquiera")) {
            razones.add("Enfoque " + enfoquePsicologo + " (sin restricción de enfoque)");
            return new CriterioPuntaje(PESO_ENFOQUE, true);
        }
        var psiNorm = normalizar(enfoquePsicologo);
        if (prefNorm.equals(psiNorm) || psiNorm.contains(prefNorm) || prefNorm.contains(psiNorm)) {
            razones.add("Coincide con tu enfoque teórico preferido (" + enfoquePsicologo + ")");
            return new CriterioPuntaje(PESO_ENFOQUE, true);
        }
        razones.add("Trabaja con enfoque " + enfoquePsicologo + " (diferente a " + enfoquePreferido + ")");
        return new CriterioPuntaje(0.0, false);
    }

    private CriterioPuntaje evaluarDisponibilidad(
            String disponibilidadPreferida,
            String disponibilidadPsicologo,
            List<String> razones) {

        var prefNorm = normalizar(disponibilidadPreferida);
        var psiNorm = normalizar(disponibilidadPsicologo);

        if (prefNorm.contains("indiferente")
                || prefNorm.contains("flexible")
                || prefNorm.contains("cualquier")) {
            razones.add("Horario compatible (" + disponibilidadPsicologo + ")");
            return new CriterioPuntaje(PESO_DISPONIBILIDAD, true);
        }

        if (!prefNorm.isBlank() && (psiNorm.contains(prefNorm) || prefNorm.contains(psiNorm))) {
            razones.add("Disponibilidad horaria compatible: " + disponibilidadPsicologo);
            return new CriterioPuntaje(PESO_DISPONIBILIDAD, true);
        }

        var periodosPref = extraerPeriodos(disponibilidadPreferida);
        var periodosPsi = extraerPeriodos(disponibilidadPsicologo);
        var diasPref = extraerDias(prefNorm);
        var diasPsi = extraerDias(psiNorm);

        boolean periodoCoincide = !periodosPref.isEmpty() && periodosPref.stream().anyMatch(periodosPsi::contains);
        boolean todosPeriodosCoinciden = !periodosPref.isEmpty() && periodosPsi.containsAll(periodosPref);
        boolean diaCoincide = diasPref.isEmpty() || diasPref.stream().anyMatch(diasPsi::contains);
        boolean todosDiasCoinciden = diasPref.isEmpty() || diasPsi.containsAll(diasPref);

        if (!periodosPref.isEmpty() && todosPeriodosCoinciden && todosDiasCoinciden) {
            razones.add("Disponibilidad horaria compatible: " + disponibilidadPsicologo);
            return new CriterioPuntaje(PESO_DISPONIBILIDAD, true);
        }
        if (periodosPref.isEmpty() && !diasPref.isEmpty() && todosDiasCoinciden) {
            razones.add("Días de atención compatibles: " + disponibilidadPsicologo);
            return new CriterioPuntaje(PESO_DISPONIBILIDAD, true);
        }
        if (periodoCoincide || (!diasPref.isEmpty() && diaCoincide)) {
            razones.add("Compatibilidad horaria parcial (" + disponibilidadPsicologo + ")");
            return new CriterioPuntaje(PESO_DISPONIBILIDAD * 0.5, false);
        }

        razones.add("Horario disponible (" + disponibilidadPsicologo + ") distinto al preferido");
        return new CriterioPuntaje(0.0, false);
    }

    private static boolean coincideTextoClinico(String especialidadNorm, String textoNorm) {
        if (especialidadNorm.isBlank() || textoNorm.isBlank()) {
            return false;
        }
        if (especialidadNorm.equals(textoNorm)
                || especialidadNorm.contains(textoNorm)
                || textoNorm.contains(especialidadNorm)) {
            return true;
        }
        var tokensEsp = extraerTokensSignificativos(especialidadNorm);
        var tokensTexto = extraerTokensSignificativos(textoNorm);
        return tokensEsp.stream().anyMatch(tokensTexto::contains);
    }

    private static Set<String> extraerTokensSignificativos(String textoNorm) {
        var tokens = new LinkedHashSet<String>();
        for (var token : textoNorm.split(" ")) {
            if (token.length() >= 4 && !STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private static Set<String> extraerPeriodos(String horario) {
        var norm = normalizar(horario);
        var periodos = new LinkedHashSet<String>();
        if (norm.contains("manana")) {
            periodos.add("manana");
        }
        if (norm.contains("tarde")) {
            periodos.add("tarde");
        }
        if (norm.contains("noche")) {
            periodos.add("noche");
        }
        var matcher = HOUR_PATTERN.matcher(horario == null ? "" : horario);
        while (matcher.find()) {
            int hora = Integer.parseInt(matcher.group(1));
            if (hora >= 6 && hora <= 12) {
                periodos.add("manana");
            } else if (hora >= 13 && hora <= 17) {
                periodos.add("tarde");
            } else if (hora >= 18 && hora <= 23) {
                periodos.add("noche");
            }
        }
        return periodos;
    }

    private static Set<String> extraerDias(String norm) {
        var dias = new LinkedHashSet<String>();
        if (norm.contains("lunes a viernes")) {
            dias.addAll(List.of("lunes", "martes", "miercoles", "jueves", "viernes"));
        }
        if (norm.contains("lunes a sabado")) {
            dias.addAll(List.of("lunes", "martes", "miercoles", "jueves", "viernes", "sabado"));
        }
        if (norm.contains("martes a sabado")) {
            dias.addAll(List.of("martes", "miercoles", "jueves", "viernes", "sabado"));
        }
        for (var dia : DIAS_SEMANA) {
            if (norm.contains(dia)) {
                dias.add(dia);
            }
        }
        return dias;
    }

    private static String normalizar(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        var decomposed = Normalizer.normalize(raw, Normalizer.Form.NFD);
        var withoutMarks = COMBINING_MARKS.matcher(decomposed).replaceAll("")
                .replace('ñ', 'n')
                .replace('Ñ', 'n')
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ');
        var clean = NON_ALPHANUMERIC.matcher(withoutMarks).replaceAll(" ");
        return MULTIPLE_SPACES.matcher(clean).replaceAll(" ").trim();
    }

    private record CriterioPuntaje(double puntaje, boolean completo) {
    }
}
