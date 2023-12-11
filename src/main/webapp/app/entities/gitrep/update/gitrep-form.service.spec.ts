import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../gitrep.test-samples';

import { GitrepFormService } from './gitrep-form.service';

describe('Gitrep Form Service', () => {
  let service: GitrepFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GitrepFormService);
  });

  describe('Service methods', () => {
    describe('createGitrepFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createGitrepFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            clientid: expect.any(Object),
            accesstoken: expect.any(Object),
            client: expect.any(Object),
          }),
        );
      });

      it('passing IGitrep should create a new form with FormGroup', () => {
        const formGroup = service.createGitrepFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            clientid: expect.any(Object),
            accesstoken: expect.any(Object),
            client: expect.any(Object),
          }),
        );
      });
    });

    describe('getGitrep', () => {
      it('should return NewGitrep for default Gitrep initial value', () => {
        const formGroup = service.createGitrepFormGroup(sampleWithNewData);

        const gitrep = service.getGitrep(formGroup) as any;

        expect(gitrep).toMatchObject(sampleWithNewData);
      });

      it('should return NewGitrep for empty Gitrep initial value', () => {
        const formGroup = service.createGitrepFormGroup();

        const gitrep = service.getGitrep(formGroup) as any;

        expect(gitrep).toMatchObject({});
      });

      it('should return IGitrep', () => {
        const formGroup = service.createGitrepFormGroup(sampleWithRequiredData);

        const gitrep = service.getGitrep(formGroup) as any;

        expect(gitrep).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IGitrep should not enable id FormControl', () => {
        const formGroup = service.createGitrepFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewGitrep should disable id FormControl', () => {
        const formGroup = service.createGitrepFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
