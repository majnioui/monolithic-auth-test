import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { IGitrep } from '../gitrep.model';
import { GitrepService } from '../service/gitrep.service';
import { GitrepFormService, GitrepFormGroup } from './gitrep-form.service';

@Component({
  standalone: true,
  selector: 'jhi-gitrep-update',
  templateUrl: './gitrep-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class GitrepUpdateComponent implements OnInit {
  isSaving = false;
  gitrep: IGitrep | null = null;

  clientsSharedCollection: IClient[] = [];

  editForm: GitrepFormGroup = this.gitrepFormService.createGitrepFormGroup();

  constructor(
    protected gitrepService: GitrepService,
    protected gitrepFormService: GitrepFormService,
    protected clientService: ClientService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  compareClient = (o1: IClient | null, o2: IClient | null): boolean => this.clientService.compareClient(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ gitrep }) => {
      this.gitrep = gitrep;
      if (gitrep) {
        this.updateForm(gitrep);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const gitrep = this.gitrepFormService.getGitrep(this.editForm);
    if (gitrep.id !== null) {
      this.subscribeToSaveResponse(this.gitrepService.update(gitrep));
    } else {
      this.subscribeToSaveResponse(this.gitrepService.create(gitrep));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IGitrep>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(gitrep: IGitrep): void {
    this.gitrep = gitrep;
    this.gitrepFormService.resetForm(this.editForm, gitrep);

    this.clientsSharedCollection = this.clientService.addClientToCollectionIfMissing<IClient>(this.clientsSharedCollection, gitrep.client);
  }

  protected loadRelationshipsOptions(): void {
    this.clientService
      .query()
      .pipe(map((res: HttpResponse<IClient[]>) => res.body ?? []))
      .pipe(map((clients: IClient[]) => this.clientService.addClientToCollectionIfMissing<IClient>(clients, this.gitrep?.client)))
      .subscribe((clients: IClient[]) => (this.clientsSharedCollection = clients));
  }
}
