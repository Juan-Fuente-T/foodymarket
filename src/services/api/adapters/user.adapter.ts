import { User } from "@/types/models";

// Exporta la función como "named export"
export const adaptUser = (data: any): User => ({
    id: data.id || '',
    name: data.usr_nombre || '',
    email: data.usr_email || '',
    role: data.usr_tipo as User['role'] || 'cliente',
    phone: data.usr_telefono || '',
    address: data.usr_direccion || '',
    createdAt: data.usr_fecha_registro?.toString() || new Date().toISOString()
//   updatedAt: '' // No existe en backend
});

// Opcional: Exporta tipos útiles relacionados
// export type UserAdapter = ReturnType<typeof adaptUser>;