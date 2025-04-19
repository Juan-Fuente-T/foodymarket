
import { User, UserRole } from '@/types/models';

export const adaptUser = (data: any): User => {
  let role: UserRole;
  if (data.role === 'CLIENTE' || data.role === 'cliente') {
    role = 'CLIENTE';
  } else {
    role = 'RESTAURANTE';
  }

  return {
    id: data.id?.toString() || '',
    name: data.name || '',
    email: data.email || '',
    role,
    phone: data.phone || '',
    address: data.address || '',
    createdAt: data.createdAt || new Date().toISOString(),
    avatar: data.avatar || ''
  };
};
