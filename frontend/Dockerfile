# --- FASE 1: CONSTRUCCIÓN ---
FROM node:20-alpine AS frontend-build
WORKDIR /app

# --- Variable entorno ---
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# --- FASE 2: SERVICIO ---
FROM nginx:stable-alpine
COPY --from=frontend-build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]