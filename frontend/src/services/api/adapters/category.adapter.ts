import { Category } from '@/types/models';

export const adaptCategory = (data: any): Category => ({
  id: data.ctg_id?.toString() || '',
  name: data.name || ''
});
