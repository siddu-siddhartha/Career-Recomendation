# Backend deployment

Deploy the backend with the `prod` Spring profile enabled. Configure these environment
variables in the hosting provider; do not commit production credentials:

* `SPRING_PROFILES_ACTIVE=prod`
* `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`
  for the hosted MySQL database.
* `APP_JWT_SECRET` with a randomly generated value of at least 32 characters.
* `APP_CORS_ALLOWED_ORIGINS` with the frontend's exact origin, without a trailing
  slash. For this deployment, use `https://career-recomendation.vercel.app`.
  Separate multiple origins with commas.
* `GEMINI_API_KEY` if the Gemini chat feature is enabled.

The `prod` profile requires the database and JWT environment variables instead of
falling back to local development values. Its default CORS origin is
`https://career-recomendation.vercel.app`; set `APP_CORS_ALLOWED_ORIGINS` to override
it or add other origins. The default CORS origins in the regular profile remain
`localhost:5173` and `localhost:5174` for local development.

For the current Railway/Vercel deployment, set:

* `SPRING_PROFILES_ACTIVE=prod`
* `SPRING_DATASOURCE_URL=jdbc:mysql://mysql.railway.internal:3306/railway?serverTimezone=UTC`
* `SPRING_DATASOURCE_USERNAME=root`
* `SPRING_DATASOURCE_PASSWORD` to the password from the Railway MySQL service
* `APP_JWT_SECRET` to a newly generated random secret of at least 32 characters
* `APP_CORS_ALLOWED_ORIGINS=https://career-recomendation.vercel.app`

In Vercel, set `VITE_API_BASE_URL=https://career-recommendation-production-ac3f.up.railway.app`.
