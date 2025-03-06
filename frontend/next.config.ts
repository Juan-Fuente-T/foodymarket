import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  images: {
	remotePatterns:[
	  {hostname: "ixbnnrlngacirjuxztxa.supabase.co",
	  pathname: "/**",},
	],
  },
	output:"standalone",
};

export default nextConfig;
