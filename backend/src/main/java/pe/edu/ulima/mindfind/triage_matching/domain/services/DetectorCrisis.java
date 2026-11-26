package pe.edu.ulima.mindfind.triage_matching.domain.services;

import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.NivelRiesgo;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoDeteccionCrisis;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Domain Service puro (Java sin Spring/JPA) que detecta indicadores de riesgo crítico
 * en el cuestionario de triage (HU-06), normalizando mayúsculas y tildes.
 */
public class DetectorCrisis {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

    private static final List<String> PALABRAS_CRITICAS = List.of(
            "autolesion",
            "suicid",
            "quitarme la vida",
            "matarme",
            "no quiero vivir",
            "hacerme dano",
            "violencia intrafamiliar",
            "maltrato",
            "abuso"
    );

    private static final List<String> INDICADORES_RIESGO_ALTO_NO_CRITICO = List.of(
            "panico",
            "ataque de panico",
            "desesperanza",
            "insomnio severo"
    );

    public ResultadoDeteccionCrisis evaluar(
            String motivoConsulta,
            List<String> sintomasDeclarados,
            String preferenciasProfesional,
            boolean indicadorRiesgoInminente) {

        var textos = new ArrayList<String>();
        if (motivoConsulta != null && !motivoConsulta.isBlank()) {
            textos.add(motivoConsulta);
        }
        if (sintomasDeclarados != null) {
            sintomasDeclarados.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .forEach(textos::add);
        }
        if (preferenciasProfesional != null && !preferenciasProfesional.isBlank()) {
            textos.add(preferenciasProfesional);
        }

        for (var texto : textos) {
            var normalizado = normalizarTexto(texto);
            for (var palabraCritica : PALABRAS_CRITICAS) {
                if (normalizado.contains(palabraCritica)) {
                    return new ResultadoDeteccionCrisis(true, NivelRiesgo.CRITICO, palabraCritica);
                }
            }
        }

        if (indicadorRiesgoInminente) {
            return new ResultadoDeteccionCrisis(true, NivelRiesgo.CRITICO, "INDICADOR_RIESGO_INMINENTE");
        }

        var nivelNoCritico = estimarNivelNoCritico(textos, sintomasDeclarados);
        return new ResultadoDeteccionCrisis(false, nivelNoCritico, null);
    }

    private NivelRiesgo estimarNivelNoCritico(List<String> textos, List<String> sintomasDeclarados) {
        for (var texto : textos) {
            var normalizado = normalizarTexto(texto);
            for (var indicador : INDICADORES_RIESGO_ALTO_NO_CRITICO) {
                if (normalizado.contains(indicador)) {
                    return NivelRiesgo.ALTO;
                }
            }
        }
        long cantidadSintomas = sintomasDeclarados == null
                ? 0
                : sintomasDeclarados.stream().filter(s -> s != null && !s.isBlank()).count();
        if (cantidadSintomas >= 4) {
            return NivelRiesgo.ALTO;
        }
        if (cantidadSintomas >= 2) {
            return NivelRiesgo.MODERADO;
        }
        return NivelRiesgo.BAJO;
    }

    static String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        var decomposed = Normalizer.normalize(texto, Normalizer.Form.NFD);
        var withoutMarks = COMBINING_MARKS.matcher(decomposed).replaceAll("")
                .replace('ñ', 'n')
                .replace('Ñ', 'n')
                .toLowerCase(Locale.ROOT);
        var clean = NON_ALPHANUMERIC.matcher(withoutMarks).replaceAll(" ");
        return MULTIPLE_SPACES.matcher(clean).replaceAll(" ").trim();
    }
}
