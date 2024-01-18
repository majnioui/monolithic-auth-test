import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../docker.test-samples';

import { DockerFormService } from './docker-form.service';

describe('Docker Form Service', () => {
  let service: DockerFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DockerFormService);
  });

  describe('Service methods', () => {
    describe('createDockerFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createDockerFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            username: expect.any(Object),
            repoName: expect.any(Object),
            url: expect.any(Object),
          }),
        );
      });

      it('passing IDocker should create a new form with FormGroup', () => {
        const formGroup = service.createDockerFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            username: expect.any(Object),
            repoName: expect.any(Object),
            url: expect.any(Object),
          }),
        );
      });
    });

    describe('getDocker', () => {
      it('should return NewDocker for default Docker initial value', () => {
        const formGroup = service.createDockerFormGroup(sampleWithNewData);

        const docker = service.getDocker(formGroup) as any;

        expect(docker).toMatchObject(sampleWithNewData);
      });

      it('should return NewDocker for empty Docker initial value', () => {
        const formGroup = service.createDockerFormGroup();

        const docker = service.getDocker(formGroup) as any;

        expect(docker).toMatchObject({});
      });

      it('should return IDocker', () => {
        const formGroup = service.createDockerFormGroup(sampleWithRequiredData);

        const docker = service.getDocker(formGroup) as any;

        expect(docker).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IDocker should not enable id FormControl', () => {
        const formGroup = service.createDockerFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewDocker should disable id FormControl', () => {
        const formGroup = service.createDockerFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
