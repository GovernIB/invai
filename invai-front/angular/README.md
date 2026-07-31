# InvaiFront

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.1.5.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

The Playwright tests authenticate against the real CAIB IdP and use the development backend
configured by `proxy.dev.conf.json`. Use a dedicated test account without MFA and do not commit its
credentials.

Install the Chromium browser once:

```bash
npx playwright install chromium
```

Copy `.env.example` to `.env` and fill in `E2E_USERNAME` and `E2E_PASSWORD`, then run:

```bash
npm run test:e2e
```

Playwright starts the Angular development server automatically. If a server is already listening at
`http://localhost:4200`, it is reused. To test against a local backend, start `npm run local` in
another terminal before running the e2e command. The suite uses a single Playwright worker, so all
tests run sequentially.

The applications lifecycle test creates a uniquely named record in the real development backend,
checks its create, update, withdrawal and reactivation flows, and leaves it inactive when the test
finishes. Inactive records prefixed with `E2E` are therefore expected after running the suite.

The maintenance lifecycle tests cover categories, information systems, environments, fields and
IT commissions. Each test lists, creates, views, updates and deletes its own uniquely named record.
Cleanup removes active test records when a flow fails. Environment and IT commission deletion are
deactivations, so inactive records whose code, name or expedient starts with `E2E` are also expected
after running the suite.

To run only the maintenance coverage:

```bash
npm run test:e2e -- e2e/maintenances.spec.ts
```

For interactive debugging, use `npm run test:e2e:headed`.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
