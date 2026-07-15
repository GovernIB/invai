import { Component } from '@angular/core';

@Component({
  standalone: true,
  selector: 'app-application-responsible-form',
  templateUrl: './application-responsible-form.html',
})
export class ApplicationResponsibleForm {
  protected readonly placeholder = $localize`El formulari de responsable encara no està implementat.`;
}
