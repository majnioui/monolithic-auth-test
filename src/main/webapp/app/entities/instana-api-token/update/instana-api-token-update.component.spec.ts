import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { InstanaApiTokenService } from '../service/instana-api-token.service';
import { IInstanaApiToken } from '../instana-api-token.model';
import { InstanaApiTokenFormService } from './instana-api-token-form.service';

import { InstanaApiTokenUpdateComponent } from './instana-api-token-update.component';

describe('InstanaApiToken Management Update Component', () => {
  let comp: InstanaApiTokenUpdateComponent;
  let fixture: ComponentFixture<InstanaApiTokenUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let instanaApiTokenFormService: InstanaApiTokenFormService;
  let instanaApiTokenService: InstanaApiTokenService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), InstanaApiTokenUpdateComponent],
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
      .overrideTemplate(InstanaApiTokenUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(InstanaApiTokenUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    instanaApiTokenFormService = TestBed.inject(InstanaApiTokenFormService);
    instanaApiTokenService = TestBed.inject(InstanaApiTokenService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const instanaApiToken: IInstanaApiToken = { id: 456 };

      activatedRoute.data = of({ instanaApiToken });
      comp.ngOnInit();

      expect(comp.instanaApiToken).toEqual(instanaApiToken);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IInstanaApiToken>>();
      const instanaApiToken = { id: 123 };
      jest.spyOn(instanaApiTokenFormService, 'getInstanaApiToken').mockReturnValue(instanaApiToken);
      jest.spyOn(instanaApiTokenService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ instanaApiToken });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: instanaApiToken }));
      saveSubject.complete();

      // THEN
      expect(instanaApiTokenFormService.getInstanaApiToken).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(instanaApiTokenService.update).toHaveBeenCalledWith(expect.objectContaining(instanaApiToken));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IInstanaApiToken>>();
      const instanaApiToken = { id: 123 };
      jest.spyOn(instanaApiTokenFormService, 'getInstanaApiToken').mockReturnValue({ id: null });
      jest.spyOn(instanaApiTokenService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ instanaApiToken: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: instanaApiToken }));
      saveSubject.complete();

      // THEN
      expect(instanaApiTokenFormService.getInstanaApiToken).toHaveBeenCalled();
      expect(instanaApiTokenService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IInstanaApiToken>>();
      const instanaApiToken = { id: 123 };
      jest.spyOn(instanaApiTokenService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ instanaApiToken });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(instanaApiTokenService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
