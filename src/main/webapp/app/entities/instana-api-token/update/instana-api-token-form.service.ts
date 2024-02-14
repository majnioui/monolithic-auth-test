import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IInstanaApiToken, NewInstanaApiToken } from '../instana-api-token.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IInstanaApiToken for edit and NewInstanaApiTokenFormGroupInput for create.
 */
type InstanaApiTokenFormGroupInput = IInstanaApiToken | PartialWithRequiredKeyOf<NewInstanaApiToken>;

type InstanaApiTokenFormDefaults = Pick<NewInstanaApiToken, 'id'>;

type InstanaApiTokenFormGroupContent = {
  id: FormControl<IInstanaApiToken['id'] | NewInstanaApiToken['id']>;
  token: FormControl<IInstanaApiToken['token']>;
};

export type InstanaApiTokenFormGroup = FormGroup<InstanaApiTokenFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class InstanaApiTokenFormService {
  createInstanaApiTokenFormGroup(instanaApiToken: InstanaApiTokenFormGroupInput = { id: null }): InstanaApiTokenFormGroup {
    const instanaApiTokenRawValue = {
      ...this.getFormDefaults(),
      ...instanaApiToken,
    };
    return new FormGroup<InstanaApiTokenFormGroupContent>({
      id: new FormControl(
        { value: instanaApiTokenRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      token: new FormControl(instanaApiTokenRawValue.token),
    });
  }

  getInstanaApiToken(form: InstanaApiTokenFormGroup): IInstanaApiToken | NewInstanaApiToken {
    return form.getRawValue() as IInstanaApiToken | NewInstanaApiToken;
  }

  resetForm(form: InstanaApiTokenFormGroup, instanaApiToken: InstanaApiTokenFormGroupInput): void {
    const instanaApiTokenRawValue = { ...this.getFormDefaults(), ...instanaApiToken };
    form.reset(
      {
        ...instanaApiTokenRawValue,
        id: { value: instanaApiTokenRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): InstanaApiTokenFormDefaults {
    return {
      id: null,
    };
  }
}
