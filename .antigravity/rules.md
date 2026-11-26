# Reglas Obligatorias de Arquitectura — MindFind (Sprint 1)

## 1. Referencia Arquitectónica Obligatoria
- Todo el código del `/backend` (Java 21 + Spring Boot 3 + Maven) debe seguir estrictamente la misma estructura, convenciones de nombres y separación de capas que el proyecto de referencia ubicado en `_referencia/ulima-market-platform-main`.
- Antes de crear cualquier clase nueva en el backend, revisa cómo está implementada su equivalente en `_referencia/ulima-market-platform-main`.

## 2. Bounded Contexts y Espejo Backend / Frontend
- El proyecto es un monorrepositorio dividido en `/backend` y `/frontend`.
- Los nombres de los Bounded Contexts deben ser exactamente iguales en el paquete base del backend (`pe.edu.ulima.mindfind.<bc>`) y en las carpetas del frontend (`frontend/src/<bc>`):
  - `shared`: infraestructura transversal (`AbstractDomainAggregateRoot`, `DomainEventPublisher`, `AuditableAbstractPersistenceEntity`, `GlobalExceptionHandler`, `OpenApiConfiguration`).
  - `identidad_acreditacion`: HU-01 (registro y acreditación de psicólogos clínicos).
  - `triage_matching`: HU-04 (cuestionario de triage), HU-06 (protocolo y alerta de crisis) y HU-05 (motor de compatibilidad y ranking Top 5).
- Mantén consistencia total de marca: usa únicamente **MindFind** en títulos, UI, Swagger y documentación (no mezclar otros nombres).

## 3. Reglas Estrictas de DDD en `/backend`
Cada Bounded Context debe dividirse exactamente en 4 capas:
1. `domain/`:
   - Contiene `model/aggregates/`, `model/valueobjects/`, `model/commands/`, `model/queries/`, `model/events/`, `repositories/` (solo interfaces) y `services/` (solo interfaces).
   - **PROHIBIDO** usar anotaciones de JPA/Hibernate (`@Entity`, `@Table`, `@Id`, `@Column`) o dependencias de Spring Web dentro de `domain/`. El dominio debe ser Java puro.
   - Los invariantes de negocio se validan dentro de los Aggregates y Value Objects (ej. `NumeroColegiatura` numérico de 4 a 6 dígitos, unicidad de correo/colegiatura, estado inicial `PENDIENTE_VERIFICACION`, detección de palabras clave de crisis que interrumpe el triage y bloquea el matching).
2. `application/internal/`:
   - Contiene `commandservices/`, `queryservices/`, `eventhandlers/` y `outboundservices/acl/`.
   - Orquesta transacciones, repositorios y publicación de eventos mediante `DomainEventPublisher`.
3. `infrastructure/persistence/jpa/`:
   - Contiene `entities/` (`*JpaEntity`), `repositories/` (`*JpaRepository`) y `adapters/` (`*RepositoryAdapter` que traduce entre el modelo de dominio y JPA).
4. `interfaces/`:
   - Contiene `rest/` (Controllers, `resources/` DTOs, `transform/` Assemblers), `events/` (Integration Events) y `acl/` (`*ContextFacade`).

## 4. Comunicación entre Bounded Contexts (ACL y Eventos)
- **PROHIBIDO** que un Bounded Context importe clases de `domain/`, `infrastructure/` o `application/` de otro Bounded Context.
- Toda comunicación entre `triage_matching` e `identidad_acreditacion` debe hacerse exclusivamente a través de:
  - **Anti-Corruption Layer (ACL):** `identidad_acreditacion/interfaces/acl/IdentityContextFacade` consumido desde `triage_matching/application/internal/outboundservices/acl/ExternalIdentityService`.
  - **Integration Events** cuando corresponda propagar hechos entre contextos.
- Toda regla de capas y aislamiento entre contextos debe validarse con pruebas de arquitectura en `ArchitectureTest.java` usando **ArchUnit**.

## 5. Reglas del `/frontend`
- Stack: React + Vite + TypeScript + Tailwind CSS.
- Estructura espejo en `src/shared/`, `src/identidad_acreditacion/` y `src/triage_matching/`.
- Usar la paleta de MindFind: verde azulado, blanco, lavanda suave y gris claro.
- **PROHIBIDO** usar datos hardcodeados/mockeados en las pantallas finales: todos los flujos (HU-01, HU-04, HU-06, HU-05) deben consumir los endpoints reales del backend mediante la variable de entorno `VITE_API_BASE_URL`.

## 6. Reglas de Calidad y Entregable
- Todo cambio debe compilar y pasar `./mvnw test` (backend) y `npm run build` (frontend) antes de darse por terminado.
- Incluir un `DataSeeder` en el backend con psicólogos verificados de ejemplo para que el algoritmo de matching (HU-05) funcione inmediatamente en la demo en vivo.