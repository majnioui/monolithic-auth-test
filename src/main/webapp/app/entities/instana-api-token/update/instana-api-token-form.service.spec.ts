import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../instana-api-token.test-samples';

import { InstanaApiTokenFormService } from './instana-api-token-form.service';

describe('InstanaApiToken Form Service', () => {
  let service: InstanaApiTokenFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InstanaApiTokenFormService);
  });

  describe('Service methods', () => {
    describe('createInstanaApiTokenFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createInstanaApiTokenFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            token: expect.any(Object),
          }),
        );
      });

      it('passing IInstanaApiToken should create a new form with FormGroup', () => {
        const formGroup = service.createInstanaApiTokenFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            token: expect.any(Object),
          }),
        );
      });
    });

    describe('getInstanaApiToken', () => {
      it('should return NewInstanaApiToken for default InstanaApiToken initial value', () => {
        const formGroup = service.createInstanaApiTokenFormGroup(sampleWithNewData);

        const instanaApiToken = service.getInstanaApiToken(formGroup) as any;

        expect(instanaApiToken).toMatchObject(sampleWithNewData);
      });

      it('should return NewInstanaApiToken for empty InstanaApiToken initial value', () => {
        const formGroup = service.createInstanaApiTokenFormGroup();

        const instanaApiToken = service.getInstanaApiToken(formGroup) as any;

        expect(instanaApiToken).toMatchObject({});
      });

      it('should return IInstanaApiToken', () => {
        const formGroup = service.createInstanaApiTokenFormGroup(sampleWithRequiredData);

        const instanaApiToken = service.getInstanaApiToken(formGroup) as any;

        expect(instanaApiToken).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IInstanaApiToken should not enable id FormControl', () => {
        const formGroup = service.createInstanaApiTokenFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewInstanaApiToken should disable id FormControl', () => {
        const formGroup = service.createInstanaApiTokenFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
