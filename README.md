# 🍽️ Foody - Marketplace de Restaurantes  

## Del restaurante a tu mesa

![Foody_logo](https://github.com/user-attachments/assets/b40c7b1f-f643-43cb-82ff-f38ac2c1ba6f)

Foody es una plataforma de pedidos en línea que conecta a los usuarios con sus restaurantes favoritos. La aplicación permite a los clientes elegir entre los diferentes restaurantes, explorar menús, realizar pedidos y recibir confirmaciones en tiempo real. Los dueños de restaurantes pueden gestionar sus productos y pedidos desde un panel de administración.  

## 🚀 Características principales  

✅ **Explorar restaurantes** y ver su menú actualizado.  
✅ **Realizar pedidos** fácilmente desde la aplicación.  
✅ **Confirmación automática** de pedidos por parte del restaurante.  
✅ **Gestión de productos y pedidos** desde un panel de administración.  
✅ **Arquitectura modular** con tecnologías modernas.  

---

## 📐 Flujo de la Aplicación  

A continuación, se muestra un esquema del flujo de interacción en la aplicación:  

![Flujo de la aplicación](https://github.com/user-attachments/assets/7db6619b-7cad-4116-9558-4abb287a4825)


---

## 🗄️ Modelo de Base de Datos  

Foody utiliza PostgreSQL como base de datos relacional. El siguiente diagrama representa su estructura:  

 <img width="755" alt="RestaurantsDataBase" src="https://github.com/user-attachments/assets/28e084fc-327c-43d3-a9db-c040eb63cbc2" />


---

## 🛠️ Tecnologías Utilizadas  

El proyecto está construido con el siguiente **stack tecnológico**:  

### 🔹 Backend  
- **Java 21**  
- **Spring Boot** (Framework principal)  
- **Spring Security** (Autenticación y autorización)  
- **PostgreSQL** (Base de datos)  
- **Hibernate** (ORM para interacción con la base de datos)  

### 🔹 Frontend  
- **TypeScript**  
- **React** (Librería para interfaz de usuario)  
- **Next.js** (Renderizado del lado del servidor y optimización)  
- **HTML, Tailwind** (Diseño y estilos)  

### 🔹 Otras herramientas  
- **Docker** (Para despliegue y contenedores)  
- **JWT** (JSON Web Tokens para autenticación)  
- **Telegram API** (Para notificaciones futuras)  

---

## 📦 Instalación y Configuración  

### 🔹 Requisitos previos  

Antes de instalar y ejecutar la aplicación, asegúrate de tener instalado:  
- **Java 21 o superior**  
- **Node.js 18+ y npm**  
- **PostgreSQL**  
- **Docker** (Opcional para despliegue con contenedores)  

### 🔹 Configuración de Backend  

1. Clona el repositorio:  
   ```sh
   git clone https://github.com/No-Country-simulation/c24-39-t-webapp.git
   cd backend
   
2. Configura la base de datos en application.properties:
  spring.datasource.url=jdbc:postgresql://localhost:5432/foody_db
  spring.datasource.username=tu_usuario
  spring.datasource.password=tu_contraseña

3. Ejecuta el backend con Maven o Gradle:
  ./mvnw spring-boot:run
   
🔹 Configuración de Frontend

1. Ve al directorio del frontend:
  cd ../frontend
  
2. Instala las dependencias:
  npm install

4. Ejecuta la aplicación en modo desarrollo:
  npm run dev

4. Abre el navegador en:
  http://localhost:3000

🏗️ Uso de la Aplicación

🔹 Para Usuarios
1. Registrarse e iniciar sesión.
2. Seleccionar un restaurante y ver su menú.
3. Añadir productos al carrito y realizar el pedido.
4. Recibir confirmación del pedido.
   
🔹 Para Administradores (Restaurantes)
1. Iniciar sesión en el panel de administración.
2. Gestionar el menú de productos.
3. Ver y aceptar pedidos en tiempo real.
   
🛠️ Despliegue con Docker

Puedes ejecutar la aplicación con Docker de la siguiente manera:

1. Construir la imagen y ejecutar el contenedor:
  docker-compose up --build

2. Acceder a la aplicación en:
  http://localhost:3000

🎯 Futuras Implementaciones
🔹 Integración con pasarelas de pago.
🔹 Notificaciones en tiempo real con WebSockets.
🔹 Optimización del rendimiento y SEO en Next.js.

🤝 Contribución
Si quieres contribuir, sigue estos pasos:

1. Haz un fork del repositorio.
2. Crea una nueva rama (git checkout -b feature-nueva).
3. Realiza tus cambios y haz commit (git commit -m "Añadir nueva funcionalidad").
4. Sube los cambios (git push origin feature-nueva).
5. Abre un Pull Request.
   
📜 Licencia
Este proyecto está bajo la licencia MIT. Puedes usarlo, modificarlo y distribuirlo libremente.

¡Gracias por interesarte en Foody! 🍕🚀
