import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { DockerService } from '../service/docker.service';
import { IDocker } from '../docker.model';
import { DockerFormService } from './docker-form.service';

import { DockerUpdateComponent } from './docker-update.component';

describe('Docker Management Update Component', () => {
  let comp: DockerUpdateComponent;
  let fixture: ComponentFixture<DockerUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let dockerFormService: DockerFormService;
  let dockerService: DockerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), DockerUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(DockerUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(DockerUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    dockerFormService = TestBed.inject(DockerFormService);
    dockerService = TestBed.inject(DockerService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const docker: IDocker = { id: 456 };

      activatedRoute.data = of({ docker });
      comp.ngOnInit();

      expect(comp.docker).toEqual(docker);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IDocker>>();
      const docker = { id: 123 };
      jest.spyOn(dockerFormService, 'getDocker').mockReturnValue(docker);
      jest.spyOn(dockerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ docker });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: docker }));
      saveSubject.complete();

      // THEN
      expect(dockerFormService.getDocker).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(dockerService.update).toHaveBeenCalledWith(expect.objectContaining(docker));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IDocker>>();
      const docker = { id: 123 };
      jest.spyOn(dockerFormService, 'getDocker').mockReturnValue({ id: null });
      jest.spyOn(dockerService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ docker: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: docker }));
      saveSubject.complete();

      // THEN
      expect(dockerFormService.getDocker).toHaveBeenCalled();
      expect(dockerService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IDocker>>();
      const docker = { id: 123 };
      jest.spyOn(dockerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ docker });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(dockerService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
