import http from "node:http";
import httpProxy from "http-proxy";

const PROXY_PORT = 3000;
const NEXT_PORT = 3001;
const BACKEND_PORT = 8080;
const PREFIX = "/codeeditor/default";

const nextProxy = httpProxy.createProxyServer({ target: `http://127.0.0.1:${NEXT_PORT}` });
const backendProxy = httpProxy.createProxyServer({ target: `http://127.0.0.1:${BACKEND_PORT}` });

nextProxy.on("error", (err, _req, res) => {
  console.error("[proxy:next] error:", err.message);
  if (!res.headersSent) {
    res.writeHead(502, { "Content-Type": "text/plain" });
    res.end("Bad Gateway");
  }
});

backendProxy.on("error", (err, _req, res) => {
  console.error("[proxy:backend] error:", err.message);
  if (!res.headersSent) {
    res.writeHead(502, { "Content-Type": "text/plain" });
    res.end("Bad Gateway");
  }
});

backendProxy.on("proxyRes", (proxyRes, req, res) => {
  const origin = req.headers["x-original-origin"] || "*";
  proxyRes.headers["access-control-allow-origin"] = origin;
  proxyRes.headers["access-control-allow-credentials"] = "true";
});

const server = http.createServer((req, res) => {
  console.log(`[proxy] ${req.method} ${req.url}`);
  // /absports/3000/api/... → backend の /api/... に転送
  if (req.url.match(/^\/absports\/3000\/api\//)) {
    // CORS preflight をプロキシ側で処理
    if (req.method === "OPTIONS") {
      res.writeHead(204, {
        "Access-Control-Allow-Origin": req.headers.origin || "*",
        "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
        "Access-Control-Allow-Headers": "Authorization, Content-Type, X-Employee-Id",
        "Access-Control-Allow-Credentials": "true",
        "Access-Control-Max-Age": "3600",
      });
      res.end();
      return;
    }
    req.url = req.url.replace(/^\/absports\/3000/, "");
    // Origin ヘッダーを除去してバックエンドの CORS チェックを回避
    delete req.headers.origin;
    console.log(`[proxy] → backend: ${req.url}`);
    backendProxy.web(req, res);
    return;
  }
  // それ以外 → /codeeditor/default を前置して next へ
  req.url = `${PREFIX}${req.url}`;
  nextProxy.web(req, res);
});

server.listen(PROXY_PORT, "0.0.0.0", () => {
  console.log(`[sagemaker-proxy] listening on :${PROXY_PORT} → next(:${NEXT_PORT}) / backend(:${BACKEND_PORT})`);
});
