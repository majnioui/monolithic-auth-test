import { Component, Input } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe } from 'app/shared/date';
import { IInstanaApiToken } from '../instana-api-token.model';

@Component({
  standalone: true,
  selector: 'jhi-instana-api-token-detail',
  templateUrl: './instana-api-token-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class InstanaApiTokenDetailComponent {
  @Input() instanaApiToken: IInstanaApiToken | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  previousState(): void {
    window.history.back();
  }
}
