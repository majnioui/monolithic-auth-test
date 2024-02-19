import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IInstanaApiToken } from '../instana-api-token.model';
import { InstanaApiTokenService } from '../service/instana-api-token.service';
import { InstanaApiTokenFormService, InstanaApiTokenFormGroup } from './instana-api-token-form.service';

@Component({
  standalone: true,
  selector: 'jhi-instana-api-token-update',
  templateUrl: './instana-api-token-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class InstanaApiTokenUpdateComponent implements OnInit {
  isSaving = false;
  instanaApiToken: IInstanaApiToken | null = null;

  editForm: InstanaApiTokenFormGroup = this.instanaApiTokenFormService.createInstanaApiTokenFormGroup();

  constructor(
    protected instanaApiTokenService: InstanaApiTokenService,
    protected instanaApiTokenFormService: InstanaApiTokenFormService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ instanaApiToken }) => {
      this.instanaApiToken = instanaApiToken;
      if (instanaApiToken) {
        this.updateForm(instanaApiToken);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const instanaApiToken = this.instanaApiTokenFormService.getInstanaApiToken(this.editForm);
    if (instanaApiToken.id !== null) {
      this.subscribeToSaveResponse(this.instanaApiTokenService.update(instanaApiToken));
    } else {
      this.subscribeToSaveResponse(this.instanaApiTokenService.create(instanaApiToken));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IInstanaApiToken>>): void {
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

  protected updateForm(instanaApiToken: IInstanaApiToken): void {
    this.instanaApiToken = instanaApiToken;
    this.instanaApiTokenFormService.resetForm(this.editForm, instanaApiToken);
  }
}
