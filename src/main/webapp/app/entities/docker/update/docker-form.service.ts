import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IDocker, NewDocker } from '../docker.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IDocker for edit and NewDockerFormGroupInput for create.
 */
type DockerFormGroupInput = IDocker | PartialWithRequiredKeyOf<NewDocker>;

type DockerFormDefaults = Pick<NewDocker, 'id'>;

type DockerFormGroupContent = {
  id: FormControl<IDocker['id'] | NewDocker['id']>;
  username: FormControl<IDocker['username']>;
  repoName: FormControl<IDocker['repoName']>;
  url: FormControl<IDocker['url']>;
};

export type DockerFormGroup = FormGroup<DockerFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class DockerFormService {
  createDockerFormGroup(docker: DockerFormGroupInput = { id: null }): DockerFormGroup {
    const dockerRawValue = {
      ...this.getFormDefaults(),
      ...docker,
    };
    return new FormGroup<DockerFormGroupContent>({
      id: new FormControl(
        { value: dockerRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      username: new FormControl(dockerRawValue.username),
      repoName: new FormControl(dockerRawValue.repoName),
      url: new FormControl(dockerRawValue.url),
    });
  }

  getDocker(form: DockerFormGroup): IDocker | NewDocker {
    return form.getRawValue() as IDocker | NewDocker;
  }

  resetForm(form: DockerFormGroup, docker: DockerFormGroupInput): void {
    const dockerRawValue = { ...this.getFormDefaults(), ...docker };
    form.reset(
      {
        ...dockerRawValue,
        id: { value: dockerRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): DockerFormDefaults {
    return {
      id: null,
    };
  }
}
