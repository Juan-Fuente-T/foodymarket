
import React, { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import { Upload } from 'lucide-react';

export interface FileUploadProps {
  onFileSelected: (file: File) => void;
  onFileOptimized?: (url: string) => void;
  id?: string;
  label?: string;
  accept?: string;
  maxSize?: number; // In MB
  currentImage?: string;
  className?: string;
}

export function FileUpload({
  onFileSelected,
  onFileOptimized,
  id = 'file-upload',
  label = 'Upload File',
  accept = 'image/*',
  maxSize = 5, // Default 5MB
  currentImage,
  className = '',
}: FileUploadProps) {
  const [preview, setPreview] = useState<string | null>(currentImage || null);
  const [isUploading, setIsUploading] = useState(false);

  const optimizeImage = async (file: File): Promise<File> => {
    return new Promise((resolve, reject) => {
      // If not an image, just return the original file
      if (!file.type.startsWith('image/')) {
        resolve(file);
        return;
      }

      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = (event) => {
        const img = new Image();
        img.src = event.target?.result as string;
        img.onload = () => {
          // Create canvas for resizing
          const canvas = document.createElement('canvas');
          let width = img.width;
          let height = img.height;
          
          // Max dimensions
          const MAX_WIDTH = 1200;
          const MAX_HEIGHT = 1200;
          
          // Resize if needed
          if (width > MAX_WIDTH) {
            height = (MAX_WIDTH / width) * height;
            width = MAX_WIDTH;
          }
          
          if (height > MAX_HEIGHT) {
            width = (MAX_HEIGHT / height) * width;
            height = MAX_HEIGHT;
          }
          
          canvas.width = width;
          canvas.height = height;
          
          const ctx = canvas.getContext('2d');
          ctx?.drawImage(img, 0, 0, width, height);
          
          // Convert to blob with reduced quality for JPG/JPEG
          const quality = file.type === 'image/jpeg' || file.type === 'image/jpg' ? 0.8 : 0.9;
          canvas.toBlob(
            (blob) => {
              if (!blob) {
                reject(new Error('Failed to create blob'));
                return;
              }
              
              // Create a new File from the blob
              const optimizedFile = new File([blob], file.name, {
                type: file.type,
                lastModified: Date.now(),
              });
              
              // Set preview
              setPreview(URL.createObjectURL(blob));
              if (onFileOptimized) {
                onFileOptimized(URL.createObjectURL(blob));
              }
              
              resolve(optimizedFile);
            },
            file.type,
            quality
          );
        };
      };
      reader.onerror = () => {
        reject(new Error('Failed to read file'));
      };
    });
  };

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      if (acceptedFiles.length === 0) return;
      
      try {
        setIsUploading(true);
        const file = acceptedFiles[0];
        
        // Check file size (convert MB to bytes)
        if (file.size > maxSize * 1024 * 1024) {
          toast.error(`File size exceeds ${maxSize}MB limit`);
          return;
        }
        
        // Optimize image
        const optimizedFile = await optimizeImage(file);
        
        // Update preview if not already set by optimization
        if (!preview) {
          setPreview(URL.createObjectURL(optimizedFile));
        }
        
        // Pass the optimized file to parent component
        onFileSelected(optimizedFile);
        toast.success('File ready for upload');
      } catch (error) {
        console.error('Error handling file:', error);
        toast.error('Failed to process file');
      } finally {
        setIsUploading(false);
      }
    },
    [maxSize, onFileSelected, preview]
  );
  
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      [accept]: []
    },
    maxFiles: 1,
  });

  return (
    <div className={`space-y-2 ${className}`}>
      {label && <Label htmlFor={id}>{label}</Label>}
      
      <div
        {...getRootProps()}
        className={`border-2 border-dashed rounded-md p-6 cursor-pointer text-center transition-colors
          ${isDragActive ? 'border-primary bg-primary/5' : 'border-gray-300 hover:bg-gray-50'}
          ${isUploading ? 'opacity-50 cursor-wait' : ''}
        `}
      >
        <Input {...getInputProps()} id={id} />
        
        {preview ? (
          <div className="space-y-4">
            <div className="relative mx-auto w-full max-w-xs overflow-hidden rounded-md">
              <img
                src={preview}
                alt="Preview"
                className="h-auto w-full object-cover"
              />
            </div>
            <p className="text-sm text-gray-500">Click or drag to replace</p>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center space-y-2">
            <Upload className="h-8 w-8 text-gray-400" />
            <div className="text-sm text-gray-500">
              {isDragActive ? (
                <p>Drop the file here</p>
              ) : (
                <p>
                  Drag & drop a file here, or <span className="text-primary">browse</span>
                </p>
              )}
              <p className="mt-1">Max file size: {maxSize}MB</p>
            </div>
          </div>
        )}
      </div>
      
      {preview && (
        <div className="text-center">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => {
              setPreview(null);
              onFileSelected(new File([], ''));
            }}
          >
            Remove file
          </Button>
        </div>
      )}
    </div>
  );
}
