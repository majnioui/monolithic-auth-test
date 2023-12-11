import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IGitrep, NewGitrep } from '../gitrep.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IGitrep for edit and NewGitrepFormGroupInput for create.
 */
type GitrepFormGroupInput = IGitrep | PartialWithRequiredKeyOf<NewGitrep>;

type GitrepFormDefaults = Pick<NewGitrep, 'id'>;

type GitrepFormGroupContent = {
  id: FormControl<IGitrep['id'] | NewGitrep['id']>;
  clientid: FormControl<IGitrep['clientid']>;
  accesstoken: FormControl<IGitrep['accesstoken']>;
  client: FormControl<IGitrep['client']>;
};

export type GitrepFormGroup = FormGroup<GitrepFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class GitrepFormService {
  createGitrepFormGroup(gitrep: GitrepFormGroupInput = { id: null }): GitrepFormGroup {
    const gitrepRawValue = {
      ...this.getFormDefaults(),
      ...gitrep,
    };
    return new FormGroup<GitrepFormGroupContent>({
      id: new FormControl(
        { value: gitrepRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      clientid: new FormControl(gitrepRawValue.clientid),
      accesstoken: new FormControl(gitrepRawValue.accesstoken, {
        validators: [Validators.required],
      }),
      client: new FormControl(gitrepRawValue.client),
    });
  }

  getGitrep(form: GitrepFormGroup): IGitrep | NewGitrep {
    return form.getRawValue() as IGitrep | NewGitrep;
  }

  resetForm(form: GitrepFormGroup, gitrep: GitrepFormGroupInput): void {
    const gitrepRawValue = { ...this.getFormDefaults(), ...gitrep };
    form.reset(
      {
        ...gitrepRawValue,
        id: { value: gitrepRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): GitrepFormDefaults {
    return {
      id: null,
    };
  }
}
