// Used for the Docker/production build (see angular.json's fileReplacements) — a relative path
// works through nginx's /api/ reverse proxy (see nginx.conf) regardless of what domain or IP
// the app is actually accessed from, unlike a hardcoded "http://localhost:8080/api" which only
// ever worked when the browser and backend happened to be on the same machine.
export const environment = {
  production: true,
  apiUrl: '/api',
};
