# Configuración de Desarrollo

## Primer Setup

### 1. Configurar el plugin EnvFile en IntelliJ

1. Abrir Edit Configurations (arriba a la derecha), junto a Play
2. En la ventana de configuración, busca la sección "EnvFile"
3. Marca el checkbox: ☑ Enable EnvFile
4. En "Path to .env file", selecciona: `backend/.env`
5. Click en "Apply" y "OK"

### 2. Contenido del archivo `.env`

**Para Desarrollo (DEV):**
```
SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=desarrollo-secret-key-minimo-32-caracteres-1234567890abcdef
```

**Para Producción (PROD):**
```
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=tu-clave-super-secreta-de-produccion-aqui
```

### 3. Qué hace cada archivo

- `application.properties`: Configuración general (SIEMPRE se carga)
- `application-dev.properties`: Sobrescribe values para DESARROLLO (BD H2, logs verbosos)
- `application-prod.properties`: Sobrescribe values para PRODUCCIÓN (BD PostgreSQL/Supabase)

### 4. Cambiar de entorno

Solo **edita el `.env`** y cambia:
```diff
- SPRING_PROFILES_ACTIVE=dev
+ SPRING_PROFILES_ACTIVE=prod
```

Luego reinicia la aplicación.

## Troubleshooting

**P: Las variables dicen null**
R: ¿Recargaste Maven después de cambiar el .env? (Click derecho en pom.xml → Maven → Reload)

**P: Veo que está en DEV pero quería PROD**
R: Verifica el archivo `.env`. El perfil ACTUAL viene de la variable `SPRING_PROFILES_ACTIVE` en ese archivo.

**P: ¿Dónde está el perfil activo en los logs?**
R: Busca en los logs: `The following profiles are active:` dirá qué perfil se está usando.

# LA OPCION ACTIVA AHORA ES ESTA
### OTRA OPCION: Configurar variables de entorno directamente en IntelliJ sin usar EnvFile.
1. Abrir Edit Configurations
2. En la ventana de configuración, busca la sección "Environment variables"
3. Agregar las variables:
   - `SPRING_PROFILES_ACTIVE=dev` (o `prod`)
   - `JWT_SECRET=tu-clave-aqui`
4. Click en "Apply" y "OK"
5. Reiniciar la aplicación.
6. Verificar en los logs el perfil activo.
7. Recargar Maven si las variables no se leen correctamente.



