import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IGitrep } from '../gitrep.model';
import { GitrepService } from '../service/gitrep.service';

@Component({
  standalone: true,
  templateUrl: './gitrep-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class GitrepDeleteDialogComponent {
  gitrep?: IGitrep;

  constructor(
    protected gitrepService: GitrepService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.gitrepService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
