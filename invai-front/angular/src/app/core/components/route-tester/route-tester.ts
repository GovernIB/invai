import { Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-route-tester',
  template: `
    <div class="h-full grid place-items-center p-6">
      <div class="text-center space-y-3">
        <h1 class="text-xl font-semibold" i18n>Ruta actual</h1>
        <p class="font-mono text-sm break-all opacity-80">{{ currentUrl() }}</p>
      </div>
    </div>
  `,
})
export default class RouteTester {
  private readonly _router = inject(Router);
  protected readonly currentUrl = computed(() => this._router.url);
}
