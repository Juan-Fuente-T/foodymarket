
// lib/supabaseClient.ts
import { createClient } from '@supabase/supabase-js'

// Default values for development (these should be replaced with proper values in production)
const defaultSupabaseUrl = 'https://your-project-id.supabase.co';
const defaultSupabaseKey = 'your-anon-key';

// Try to get environment variables, fallback to defaults for development
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || defaultSupabaseUrl;
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || defaultSupabaseKey;

// Check if we're using default values in production
if (import.meta.env.PROD && (supabaseUrl === defaultSupabaseUrl || supabaseAnonKey === defaultSupabaseKey)) {
  console.error("Production environment detected but using default Supabase credentials. Please set proper environment variables.");
}

console.log("Initializing Supabase with URL:", supabaseUrl);

export const supabase = createClient(supabaseUrl, supabaseAnonKey);
