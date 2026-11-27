# MindFind Platform — Sprint 1

Plataforma web para conectar personas en búsqueda de apoyo psicológico con psicólogos clínicos acreditados mediante evaluación inicial de triage, detección preventiva de crisis y emparejamiento por compatibilidad.

## Enlaces de Despliegue (Nube)

- **Aplicación Web (Frontend en Vercel):** https://mindfind-platform.vercel.app
- **API REST / Swagger UI (Backend en Render):** https://mindfind-api.onrender.com/swagger-ui.html
- **Base de Datos:** PostgreSQL en Neon

> *Nota: El backend corre en el plan gratuito de Render; tras 15 minutos de inactividad puede tardar ~45 segundos en responder a la primera petición.*

## Arquitectura DDD y Bounded Contexts

El proyecto utiliza un monorrepositorio (`/backend` y `/frontend`) con exactamente los mismos nombres de **Bounded Contexts** en ambas capas, alineados con el informe y el Event Storming:

| Bounded Context | Rol en DDD | Alcance Sprint 1 |
| :--- | :--- | :--- |
| `shared` | Infraestructura transversal | `AbstractDomainAggregateRoot`, `DomainEventPublisher`, `GlobalExceptionHandler` (RFC 9457), CORS y OpenAPI |
| `identidad_acreditacion` | Supporting Subdomain | **HU-01:** Registro de psicólogos colegiados (CPsP), validación de credenciales, verificación de perfiles y carga semilla (`IdentidadDataSeeder`) |
| `triage_matching` | **Core Domain** | **HU-04:** Cuestionario de ingreso · **HU-06:** Detección y protocolo de crisis · **HU-05:** Motor de compatibilidad y ranking Top 5 |

- **Backend (Java 21 + Spring Boot 3):** 4 capas estrictas (`domain`, `application/internal`, `infrastructure/persistence/jpa`, `interfaces`), dominio 100% Java puro sin anotaciones JPA, comunicación entre contextos exclusivamente vía **Anti-Corruption Layer** (`IdentityContextFacade` → `ExternalIdentityService`) e **Integration Events** (`PsychologistVerifiedIntegrationEvent`), validado automáticamente con 6 reglas de **ArchUnit** (`ArchitectureTest.java`).
- **Frontend (React + Vite + TypeScript + Tailwind CSS):** estructura espejo en `src/shared`, `src/identidad_acreditacion` y `src/triage_matching`, conectado al backend real mediante `VITE_API_BASE_URL`.

## Ejecución Local

### Backend (puerto 8080, H2 en memoria)
```bash
cd backend
./mvnw test            # En Windows: .\mvnw.cmd clean test (23 pruebas)
./mvnw spring-boot:run # En Windows: .\mvnw.cmd spring-boot:run
```

### Frontend (puerto 5173)
```bash
cd frontend
npm install
npm run dev
```

## Equipo de Desarrollo
- Bueno Aranda, Oswaldo Alonso
- Diaz Mogollon, Bruno Jose
- Garcia Castro, Auro Fabian
- Montalvan Choquehuanca, Sergio Enrique
- Trujillo Cáceres, Bruno
- Tupia Canales, Bruno Stefano
- Zurita Solano, Ysrael Alberto
