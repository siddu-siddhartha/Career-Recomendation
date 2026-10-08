# Backend deployment

Deploy the backend with the `prod` Spring profile enabled. Configure these environment
variables in the hosting provider; do not commit production credentials:

* `SPRING_PROFILES_ACTIVE=prod`
* `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`
  for the hosted MySQL database.
* `APP_JWT_SECRET` with a randomly generated value of at least 32 characters.
* `APP_CORS_ALLOWED_ORIGINS` with the frontend's exact origin (for example,
  `https://your-app.vercel.app`), without a trailing slash. Separate multiple origins
  with commas.
* `GEMINI_API_KEY` if the Gemini chat feature is enabled.

The `prod` profile requires the database and JWT environment variables instead of
falling back to local development values. The default CORS origins in the regular
profile remain `localhost:5173` and `localhost:5174` for local development.
