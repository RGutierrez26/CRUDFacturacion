# Sistema de Facturación y Gestión Comercial con Conexión ARCA / AFIP

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg?logo=typescript)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-Build%20Tool-purple.svg?logo=vite)](https://vitejs.dev/)
[![Gradle](https://img.shields.io/badge/Gradle-Wrapper-02303A.svg?logo=gradle)](https://gradle.org/)

Aplicación web integral para la administración, emisión y auditoría de comprobantes comerciales, integrada con los servicios web de **ARCA / AFIP** (Administración Federal de Ingresos Públicos de Argentina) bajo el régimen de facturación electrónica en entorno de homologación (testing).

Proyecto desarrollado para la cátedra **Desarrollo de Software** (3er Año - Universidad Tecnológica Nacional, Facultad Regional Mendoza).

---

## 🚀 Características Principales

* **Facturación Electrónica Oficial (ARCA/AFIP):**
  * Autenticación criptográfica con **WSAA** (Web Service de Autenticación y Autorización) mediante firma CMS / PKCS#7 (BouncyCastle).
  * Emisión de comprobantes vía **WSFE v1** (`FECAESolicitar`), obteniendo **CAE** (Código de Autorización Electrónico) y fecha de vencimiento fiscal.
  * Determinación automática y correlativa del próximo número fiscal mediante consulta en vivo a AFIP (`FECompUltimoAutorizado`).
  * Desglose impositivo de IVA (alícuota general 21%) conforme a las reglamentaciones vigentes (RG 5616).
  * Mecanismo de **caché de Ticket de Acceso (12 horas)** para evitar saturación de pedidos de autenticación y bloqueos de IP.

* **Gestión de Comprobantes:**
  * Estados de comprobante: `PENDIENTE`, `EMITIDA` y `ANULADA`.
  * Validación de reglas fiscales: facturas emitidas con CAE son **estrictamente inmutables** (no editables ni eliminables).
  * Soporte diferenciado entre **Baja Lógica** (`fechaBaja`, soft delete) y **Anulación Comercial** (`fechaAnulacion` para control contable).
  * Modificación diferencial (PATCH) y validación matemática de subtotales, bonificaciones y saldos a dos decimales.

* **Reportes y Exportación:**
  * Grilla paginada con ordenamiento descendente y filtros combinables por rango de fechas, estado comercial y monto mínimo.
  * Exportación a **PDF** (OpenPDF) y **Excel** (TSV con formato de celdas), incorporando columnas fiscales de CAE y vencimiento.
  * Diálogo nativo del sistema operativo ("Guardar como...") mediante la **File System Access API** del navegador para elegir carpeta y nombre de archivo.

* **Arquitectura Limpia y Desacoplada:**
  * Backend API REST con Spring Boot 3, Java 21 y arquitectura por capas (Controller, Service, Repository, DTO Records y Entidades JPA).
  * Frontend SPA moderno con Vite y TypeScript, sin librerías externas pesadas, con carga bajo demanda de catálogos y modales de selección.

---

## 📁 Estructura del Proyecto

```text
├── build.gradle                       # Configuración y dependencias Gradle
├── gradlew / gradlew.bat              # Wrapper de Gradle
├── privada_test.key                   # Clave privada RSA (ignorado por .gitignore)
├── solicitud_test.csr                 # Solicitud CSR de certificado (ignorado por .gitignore)
├── certificado_test.crt               # Certificado digital X.509 de AFIP (ignorado por .gitignore)
├── ticket-homo-wsfe.properties        # Caché de Token y Sign WSAA (ignorado por .gitignore)
│
├── src/main/java/com/TpGrupalSpring/TpGrupal/
│   ├── Controller/                    # Endpoints REST (/api/facturas, /api/facturareportes, etc.)
│   │   ├── FacturaVentaController.java
│   │   ├── FacturaRestController.java
│   │   └── GlobalExceptionHandler.java# Manejo centralizado de errores y respuestas RFC 7807
│   ├── DTO/                           # DTOs inmutables implementados con Java Records
│   │   ├── Consultas/                 # DTOs de grillas y reportes (FacturaReporteDTO, etc.)
│   │   └── FacturaVenta/              # DTOs de alta, edición y detalle completo
│   ├── Entities/                      # Entidades del modelo relacional JPA (schema 'facturacion')
│   │   ├── Abstractas/                # AuditoriaApp (campos de auditoría y baja lógica)
│   │   └── Dominio/                   # FacturaVenta, Detalle, Cliente, PuntoVenta, etc.
│   ├── Repository/                    # Repositorios JPA con soporte de soft-delete
│   ├── Service/                       # Lógica de negocio y servicios maestros
│   │   ├── Afip/                      # AfipWsaaService (LoginCms) y AfipWsfeService (WSFEv1)
│   │   ├── FacturaVenta/              # CRUD, reglas de validación y cálculo de importes
│   │   └── ReporteFacturaService.java # Paginación eficiente y generación de PDF/Excel
│   └── config/                        # Configuración de CORS y autoarranque
│
├── src/main/resources/
│   ├── application.yaml               # Configuración base con variables de entorno
│   └── application-local.yaml.example # Plantilla de configuración local
│
└── frontend/                          # Cliente Web (Vite + TypeScript)
    ├── index.html                     # Tablero principal de facturación
    ├── nueva-factura.html             # Formulario de emisión y selección de artículos
    ├── package.json                   # Dependencias de frontend
    └── src/
        ├── main.ts                    # Control de grilla, paginación, filtros y descargas
        ├── nuevaFactura.ts            # Cálculo reactivo y guardado de comprobantes
        ├── style.css                  # Estilos visuales minimalistas y corporativos
        ├── models/                    # Interfaces de datos y tipos TypeScript
        └── services/                  # Clientes HTTP y llamadas a la API REST
```

---

## ⚙️ Requisitos Previos

* **Java:** JDK 21 o superior.
* **Node.js:** Versión 18 o superior con gestor de paquetes `pnpm` o `npm`.
* **Base de Datos:** Instancia de PostgreSQL (local o en la nube vía Supabase).
* **OpenSSL:** Necesario únicamente si se desean generar nuevos certificados para AFIP.

---

## 🛠️ Configuración del Entorno

### 1. Base de Datos (PostgreSQL / Supabase)

El sistema opera bajo el esquema relacional **`facturacion`**. Las tablas e índices son generados y actualizados automáticamente por Hibernate (`ddl-auto: update`).

Para configurar la conexión en entorno de desarrollo local:
1. Copia la plantilla en `src/main/resources/`:
   ```bash
   cp src/main/resources/application-local.yaml.example src/main/resources/application-local.yaml
   ```
2. Completa tus credenciales en `application-local.yaml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/facturacion
       username: tu_usuario_postgres
       password: tu_password_postgres
   ```
   *(Si utilizas Supabase, copia la Connection String en modo Session/Transaction pooler con `sslmode=require`).*

Alternativamente, puedes exportar variables de entorno en tu sistema:
* `DB_URL`: URL JDBC completa.
* `DB_USERNAME`: Usuario de la base de datos.
* `DB_PASSWORD`: Contraseña de la base de datos.

---

### 2. Credenciales y Certificados de AFIP / ARCA (Homologación)

Para emitir comprobantes electrónicos de prueba:

#### A. Generar Clave Privada y Solicitud CSR:
Desde la raíz del proyecto, ejecuta mediante OpenSSL:
```bash
# 1. Generar clave privada RSA de 2048 bits
openssl genrsa -out privada_test.key 2048

# 2. Generar solicitud de certificado (CSR) reemplazando con tu CUIT y Alias
openssl req -new -key privada_test.key -subj "/C=AR/O=EmpresaTest/CN=MiComputador/serialNumber=CUIT 20000000001" -out solicitud_test.csr
```

#### B. Obtener el Certificado en AFIP (WSASS):
1. Ingresa al portal de AFIP con Clave Fiscal en entorno de Homologación.
2. Accede al servicio **WSASS - Autogestión Certificados Homologación**.
3. Da de alta un nuevo computador fiscal subiendo el archivo `solicitud_test.csr`.
4. Descarga el certificado otorgado y guárdalo en la raíz del proyecto como **`certificado_test.crt`**.
5. En el **Administrador de Relaciones de Clave Fiscal**, asocia el computador fiscal con el servicio **wsfe (Facturación Electrónica)**.

#### C. Configurar el CUIT en la Aplicación:
En tu archivo `application-local.yaml` (o variable de entorno `AFIP_CUIT`), indica el CUIT emisor de prueba:
```yaml
afip:
  cuit: 20000000001
  cert-path: certificado_test.crt
  key-path: privada_test.key
```

> **Nota sobre el Ticket de Acceso (WSAA):**  
> Al autenticarse, AFIP entrega un Ticket de Acceso con 12 horas de duración. El sistema lo guarda automáticamente en `ticket-homo-wsfe.properties` para reutilizarlo en cada factura sin volver a solicitar credenciales, renovándolo de forma transparente al expirar.

---

## 🚦 Ejecución de la Aplicación

### 1. Iniciar el Backend (Spring Boot)
Desde la raíz del proyecto:
```bash
# En Windows:
./gradlew.bat bootRun

# En Linux / macOS:
./gradlew bootRun
```
El servidor backend se iniciará en `http://localhost:8080`.

#### Comprobación de Conectividad con AFIP:
Puedes verificar la autenticación en vivo con los servidores de AFIP consultando:
```bash
curl http://localhost:8080/api/facturas/afip/test-wsaa
```
Si la configuración es correcta, recibirás una confirmación con el Token y Sign generados.

---

### 2. Iniciar el Frontend (Vite + TypeScript)
En una nueva terminal, ingresa a la carpeta del frontend:
```bash
cd frontend

# Instalar dependencias
pnpm install   # o npm install

# Iniciar servidor de desarrollo
pnpm dev       # o npm run dev
```
Accede a la interfaz web en tu navegador: **`http://localhost:5173`**.

---

## 📋 Endpoints Principales de la API REST

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/facturareportes` | Consulta paginada y filtrada para la grilla principal |
| `GET` | `/api/facturareportes/exportar/pdf` | Descarga de reporte en formato PDF con CAE |
| `GET` | `/api/facturareportes/exportar/excel` | Descarga de reporte en formato Excel (TSV) con CAE |
| `GET` | `/api/facturas/{id}` | Obtiene el detalle completo de un comprobante por ID |
| `POST` | `/api/facturas` | Registra una nueva factura en estado `PENDIENTE` (sin número) |
| `PATCH` | `/api/facturas/{id}` | Modificación diferencial de cabecera o ítems no emitidos |
| `DELETE` | `/api/facturas/{id}` | Baja lógica del comprobante (soft-delete definitivo) |
| `POST` | `/api/facturas/{id}/anular` | Anulación comercial del comprobante |
| `POST` | `/api/facturas/{id}/facturar` | **Emisión electrónica ante AFIP/ARCA (obtención de CAE)** |
| `GET` | `/api/facturas/afip/test-wsaa` | Diagnóstico de autenticación con el servicio WSAA |

---

## 🔒 Reglas de Negocio y Seguridad Fiscal

1. **Sin número al crear:** Las facturas se crean siempre con `numero = null` y estado `PENDIENTE`. El número oficial correlativo es asignado de forma exclusiva por AFIP al momento de autorizar el CAE.
2. **Inmutabilidad:** Todo comprobante en estado `EMITIDA` con CAE válido no permite modificaciones, eliminaciones ni anulaciones directas.
3. **Auditoría:** La entidad abstracta `AuditoriaApp` persiste automáticamente las fechas de alta, modificación y baja lógica, así como los identificadores de usuario intervinientes.
4. **Seguridad de Secretos:** Las claves privadas (`*.key`), certificados (`*.crt`), solicitudes (`*.csr`), archivos de ticket (`ticket-*.properties`) y configuraciones locales (`application-local.yaml`) se encuentran excluidos del control de versiones mediante `.gitignore`.

---

## 👥 Equipo y Créditos

* **Institución:** Universidad Tecnológica Nacional - Facultad Regional Mendoza (UTN-FRM).
* **Materia:** Desarrollo de Software (3er Año).
