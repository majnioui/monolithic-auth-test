import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IDocker } from '../docker.model';
import { DockerService } from '../service/docker.service';
import { DockerFormService, DockerFormGroup } from './docker-form.service';

@Component({
  standalone: true,
  selector: 'jhi-docker-update',
  templateUrl: './docker-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class DockerUpdateComponent implements OnInit {
  isSaving = false;
  docker: IDocker | null = null;

  editForm: DockerFormGroup = this.dockerFormService.createDockerFormGroup();

  constructor(
    protected dockerService: DockerService,
    protected dockerFormService: DockerFormService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ docker }) => {
      this.docker = docker;
      if (docker) {
        this.updateForm(docker);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const docker = this.dockerFormService.getDocker(this.editForm);
    if (docker.id !== null) {
      this.subscribeToSaveResponse(this.dockerService.update(docker));
    } else {
      this.subscribeToSaveResponse(this.dockerService.create(docker));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IDocker>>): void {
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

  protected updateForm(docker: IDocker): void {
    this.docker = docker;
    this.dockerFormService.resetForm(this.editForm, docker);
  }
}
