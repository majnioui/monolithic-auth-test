import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IDocker } from '../docker.model';
import { DockerService } from '../service/docker.service';

@Component({
  standalone: true,
  templateUrl: './docker-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class DockerDeleteDialogComponent {
  docker?: IDocker;

  constructor(
    protected dockerService: DockerService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.dockerService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
