import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IInstanaApiToken } from '../instana-api-token.model';
import { InstanaApiTokenService } from '../service/instana-api-token.service';

@Component({
  standalone: true,
  templateUrl: './instana-api-token-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class InstanaApiTokenDeleteDialogComponent {
  instanaApiToken?: IInstanaApiToken;

  constructor(
    protected instanaApiTokenService: InstanaApiTokenService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.instanaApiTokenService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
