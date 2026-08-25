# Sistema de Gestión de Tutorías

## Descripción

El **Sistema de Gestión de Tutorías** es una aplicación web desarrollada para la Universidad Tecnológica Emiliano Zapata (UTEZ), orientada a digitalizar y centralizar el proceso de acompañamiento académico entre alumnos, tutores y el área de coordinación de tutorías.

La plataforma administra todo el ciclo de la tutoría: desde la asignación de tutores a grupos por academia, el registro de periodos escolares y grupos, hasta el seguimiento individual y grupal de cada alumno mediante solicitudes de tutoría, sesiones (individuales y grupales), acuerdos de seguimiento, canalizaciones a áreas de apoyo externas (Psicología, Trabajo Social, Servicios Médicos, entre otras) y generación de reportes académicos en Excel y PDF.

El sistema opera bajo tres roles diferenciados —**Alumno**, **Tutor** y **Coordinador**— cada uno con su propio panel y conjunto de funcionalidades, protegidos mediante autenticación con contraseñas cifradas y control de sesión activa por usuario.

## Estructura del Código

El proyecto sigue una arquitectura **MVC** clásica sobre **Jakarta EE**, organizada en las siguientes capas:

- **Servlets (`controllers/`)** — Actúan como controladores: reciben las peticiones HTTP, aplican las reglas de negocio y validaciones del lado del servidor, y despachan la respuesta hacia la vista correspondiente. Existen servlets dedicados por módulo (gestión de alumnos, tutores, asignaciones, periodos escolares, solicitudes, sesiones, canalizaciones, reportes, autenticación, etc.), además de un filtro (`filters/`) que protege las rutas según el rol de sesión activo.

- **DAOs (`models/dao/`)** — Encapsulan el acceso a la base de datos (Oracle, vía HikariCP para el pool de conexiones) usando JDBC con sentencias preparadas. Cada entidad principal del sistema tiene su propio DAO responsable de las operaciones CRUD y consultas específicas de negocio.

- **Modelos y DTOs (`models/`)** — Representan las entidades del dominio (Alumno, Tutor, Coordinador, Grupo, Solicitud, Canalización, Área, Periodo Escolar, etc.) y objetos de transferencia de datos (DTOs) usados para consultas agregadas o vistas de reportes que no mapean directamente a una sola tabla.

- **Utilidades (`utils/`)** — Componentes transversales: conexión a base de datos, cifrado de contraseñas, envío de correos, generación de matrículas, y construcción de reportes en Excel (Apache POI) y PDF (OpenPDF).

- **Vistas (JSP, `webapp/`)** — Organizadas por rol (`alumno/`, `tutor/`, `coordinador/`) más una carpeta `includes/` para fragmentos reutilizables (navegación, alertas, overlay de carga). Usan JSTL para la lógica de presentación, evitando código Java embebido salvo casos puntuales de formateo.

- **Assets (`webapp/assets/`)** — Hojas de estilo y scripts JavaScript organizados por rol/módulo, responsables de la validación en tiempo real de formularios, interacción con modales, llamadas AJAX (`fetch`) a los servlets, y una capa de UX común (sistema de alertas y overlay de carga automático).

## Equipo de Desarrollo

- **Irving Manuel Flores Torrescano** — Matrícula: 20253DS070 — Usuario: Irvingmanu
- **Yara Ayme Pacheco Mendoza** — Matrícula: 20253DS074 — Usuario: 20253ds074-Art
- **Edwin Sebastian Martínez Peralta** — Matrícula: 20253ds076 — Usuario: Sebastian-CR7
- **Jairo Jesus Velazquez Ojeda** — Matrícula: 20253DS081 — Usuario: J4IROXD
- **Arantza Saddai Hernández Martinez** — Matrícula: 20253DS071 — Usuario: ARY-BB
- **Luis Javier Ávila Baeza** — Matrícula: 20253DS092 — Usuario: 20253DS092-star
