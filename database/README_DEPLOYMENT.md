# Despliegue - Ventas Compras Antecsi

## Resumen del Proyecto

- **Stack:** Spring Boot 3.5, Java 17, PostgreSQL, JPA/Hibernate, JWT
- **Base de datos:** PostgreSQL

---

## 1. Crear Base de Datos y Usuario (PostgreSQL)

Conectarse como superusuario (`postgres`) y ejecutar:

```sql
CREATE DATABASE ventas_db
  WITH OWNER = postgres
  ENCODING = 'UTF8'
  TEMPLATE = template0;

\c ventas_db

CREATE USER ventas_user WITH PASSWORD 'PasswordSeguro123';
GRANT ALL PRIVILEGES ON DATABASE ventas_db TO ventas_user;
GRANT ALL ON SCHEMA public TO ventas_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ventas_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ventas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ventas_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ventas_user;
```

---

## 2. Ejecutar Scripts SQL

**Opción A – Con psql:**

```bash
psql -U ventas_user -d ventas_db -f database/02_schema.sql
psql -U ventas_user -d ventas_db -f database/03_initial_data.sql
```

**Opción B – Sin scripts (recomendado si usas la app):**

La aplicación usa `spring.jpa.hibernate.ddl-auto: update`, por lo que Hibernate puede crear/actualizar las tablas al iniciar. Los datos iniciales (roles, métodos de pago, usuario admin) se insertan con `DataInitializer.java`.

---

## 3. Variables de Entorno para Despliegue

| Variable       | Descripción                          | Ejemplo                         |
|----------------|--------------------------------------|---------------------------------|
| `DB_URL`       | URL de conexión PostgreSQL           | `jdbc:postgresql://host:5432/ventas_db` |
| `DB_USERNAME`  | Usuario de la base de datos          | `ventas_user`                   |
| `DB_PASSWORD`  | Contraseña de la base de datos       | `tu_password_seguro`            |
| `JWT_SECRET`   | Clave secreta para JWT (usar segura) | Mínimo 256 bits recomendado     |
| `JWT_EXPIRATION` | Duración del token en ms           | `3600000` (1 hora)              |

---

## 4. Build y Ejecución

```bash
# Build
./mvnw clean package -DskipTests

# Ejecutar JAR
java -jar target/ventas-0.0.1-SNAPSHOT.jar
```

Con variables de entorno:

```bash
java -jar -DDB_URL=jdbc:postgresql://host:5432/ventas_db \
  -DDB_USERNAME=ventas_user \
  -DDB_PASSWORD=tu_password \
  -DJWT_SECRET=tu_clave_secreta_muy_larga target/ventas-0.0.1-SNAPSHOT.jar
```

---

## 5. Usuario Inicial

El `DataInitializer` crea el **Superusuario** (dueño del software):

- **Username:** `superadmin`
- **Password:** `superadmin123`

Este usuario es el único que puede crear **admins** y **cajeros** desde el API. Cambiar la contraseña inmediatamente en producción.

---

## 6. API y Documentación

- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

---

## 7. Tablas Creadas

| Tabla           | Descripción              |
|-----------------|--------------------------|
| roles           | Roles de usuario         |
| usuarios        | Usuarios del sistema     |
| categorias      | Categorías de productos  |
| productos       | Productos                |
| clientes        | Clientes                 |
| proveedores     | Proveedores              |
| metodos_pago    | Métodos de pago          |
| ventas          | Ventas                   |
| venta_detalle   | Detalle de ventas        |
| compras         | Compras                  |
| compra_detalle  | Detalle de compras       |
