/** @type {import('next').NextConfig} */
const isSagemaker = process.env.SAGEMAKER === "1";
const basePath = isSagemaker ? "/codeeditor/default/absports/3000" : "";

const nextConfig = {
  basePath,
  assetPrefix: basePath,
  skipTrailingSlashRedirect: isSagemaker,
  output: isSagemaker ? undefined : "standalone",
};

export default nextConfig;
