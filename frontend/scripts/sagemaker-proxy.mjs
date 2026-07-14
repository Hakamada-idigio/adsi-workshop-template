import http from "node:http";
import httpProxy from "http-proxy";

const PROXY_PORT = 3000;
const NEXT_PORT = 3001;
const PREFIX = "/codeeditor/default";

const proxy = httpProxy.createProxyServer({ target: `http://127.0.0.1:${NEXT_PORT}` });

proxy.on("error", (err, _req, res) => {
  console.error("[proxy] error:", err.message);
  if (!res.headersSent) {
    res.writeHead(502, { "Content-Type": "text/plain" });
    res.end("Bad Gateway");
  }
});

const server = http.createServer((req, res) => {
  // /absports/3000/... が届く → /codeeditor/default/absports/3000/... に前置して next へ
  req.url = `${PREFIX}${req.url}`;
  proxy.web(req, res);
});

server.listen(PROXY_PORT, "0.0.0.0", () => {
  console.log(`[sagemaker-proxy] listening on :${PROXY_PORT} → next(:${NEXT_PORT})`);
});
