# Copilot instructions for LowCodePlatform

Project shape and architecture
- Stack: Java 17 + Spring Boot 3.1.5 (microservices), Vue 3 + Element Plus frontend, Docker Compose for prod-like runs.
- Services and ports: device-service (8081) manages devices; scene-service (8082) manages scenes and orchestrates actions; frontend (8080) serves UI. See `docker-compose.yml`.
- Data flow: Frontend → REST on device/scene services. Scene service calls device service via REST in `backend/scene-service/src/main/java/com/xiyuan/iot/scene/engine/SceneExecutor.java` (property `device.service.url`, default http://localhost:8081, overridden in Compose by `DEVICE_SERVICE_URL`).
- IDs: Entities have numeric DB `id` plus external string `deviceId`/`sceneId` (UUID). Choose endpoints accordingly:
  - Device: `/api/devices/{id}` vs `/api/devices/by-device-id/{deviceId}`
  - Scene: `/api/scenes/{id}` vs `/api/scenes/by-scene-id/{sceneId}`

Back end conventions
- Layering: Controller → Service → Repository. Examples: `DeviceController` → `DeviceService` → `DeviceRepository`; `SceneController` → `SceneService` → `SceneRepository`.
- Lombok + constructor injection: use `@RequiredArgsConstructor`; add fields as `final`.
- Transactions at service layer with `@Transactional`.
- Persistence: H2 in-memory by default (`application.yml`), `ddl-auto: update`; data resets on restart unless switched to file DB.
- Logging: use SLF4J `log` (already present) and keep package logger at DEBUG (`com.xiyuan.iot`).
- Scene execution: `SceneExecutor` sorts actions by `order`, sleeps `delaySeconds`, and posts `{ action, parameters }` to device-service `/api/devices/{deviceId}/control`. String parameters are parsed to number/bool when possible.

Front end conventions
- Axios clients live in `frontend/src/services/api.js`. Base URLs default to localhost 8081/8082 or can be overridden via `VUE_APP_DEVICE_SERVICE_URL` and `VUE_APP_SCENE_SERVICE_URL` (`frontend/.env`).
- Dev server proxy in `vue.config.js` maps relative `/api/devices` and `/api/scenes` to 8081/8082. If you use absolute base URLs, proxy is bypassed (CORS is allowed by backends via `@CrossOrigin("*")`).
- Views: `Dashboard.vue`, `DeviceManagement.vue`, `SceneManagement.vue`, `SceneDesigner.vue` use the services in `api.js`.

Run/build workflows (PowerShell on Windows)
- Device service: `cd backend/device-service; mvn spring-boot:run`
- Scene service: `cd backend/scene-service; mvn spring-boot:run`
- Frontend dev: `cd frontend; npm install; npm run serve` (served at http://localhost:8080)
- Docker all-in-one: from repo root run `docker-compose up -d`; Nginx in `frontend/nginx.conf` proxies `/api/*` to the backend containers.

Extending the platform
- New device type: update `DeviceType` enum and initialize in `SimulatedDeviceManager.initializeDevice(...)`; expose fields via `Device` properties; update frontend type options.
- New trigger type: add to `TriggerType`, handle in scene logic (and UI) and ensure `SceneExecutor` can translate it into device operations.
- Real devices: replace/extend the simulator via an adapter (see README "Home Assistant" notes); switch by configuration.

Gotchas and examples
- Prefer `sceneId`/`deviceId` for client interactions. Example control call: POST `http://localhost:8081/api/devices/{deviceId}/control` with `{ "action": "turn_on" }`.
- Execute a scene: POST `http://localhost:8082/api/scenes/{sceneId}/execute`.
- In Compose, scene-service discovers device-service via `DEVICE_SERVICE_URL=http://device-service:8081`.
