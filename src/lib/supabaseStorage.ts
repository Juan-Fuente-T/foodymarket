
import { v4 as uuidv4 } from 'uuid';
import { supabase } from './supabaseClient';
import { toast } from 'sonner';

const BUCKET_NAME = "fotos-c24-39-t-webapp";
export const uploadFileToSupabase = async (file: File, folder: string, userId: string | number): Promise<{ url: string; path: string } | null> => {
  const fileExt = file.name.split('.').pop();
  // const uniqueFileName = `${uuidv4()}.${fileExt}`;
  // const filePath = `${folder}/${userId}/${uniqueFileName}`;
  const baseName = file.name.includes('.') ? file.name.substring(0, file.name.lastIndexOf('.')) : file.name;
  const sanitizedName = baseName.replace(/[^a-zA-Z0-9_.-]/g, '_').replace(/_+/g, '_');
  const uniqueFileName = `${Date.now()}_${sanitizedName}.${fileExt}`;  
  const filePath = `${folder}/${userId}/${uniqueFileName}`;
  console.log(`Subiendo ${file.name} a ${BUCKET_NAME}/${filePath}...`);

  try {
    const { data, error: uploadError } = await supabase.storage
      .from(BUCKET_NAME)
      .upload(filePath, file, {
        cacheControl: '3600',
        upsert: false, // Poner 'true' si se quisiera reemplazar con el mismo nombre exacto (menos probable con UUID)
      });

    if (uploadError) {
      console.error('Error al subir el archivo:', uploadError);
      toast.error(`Fallo al subir ${folder === 'logos' ? 'logo' : 'imagen de portada'}: ${uploadError.message}`);
      return null;
    }

    console.log('Subida exitosa:', data);

    // Obtener la URL pública
    const { data: urlData } = supabase.storage
      .from(BUCKET_NAME)
      .getPublicUrl(filePath);

    if (!urlData || !urlData.publicUrl) {
      console.error('Error al obtener la URL pública para:', filePath);
      toast.error(`Fallo al obtener la URL pública para ${folder === 'logos' ? 'logo' : 'imagen de portada'}.`);
      // Considerar borrar el archivo si la URL falla? (Opcional)
      // await supabase.storage.from(BUCKET_NAME).remove([filePath]);
      return null;
    }

    console.log('URL Pública:', urlData.publicUrl);
    return { url: urlData.publicUrl, path: filePath }; // Devolver la URL publica en lugar de urlData.publicUrl;

  } catch (error) {
    console.error('Error inesperado durante la subida:', error);
    toast.error('Error inesperado durante la subida del archivo.');
    return null;
  }
};