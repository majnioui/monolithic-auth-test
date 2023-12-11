import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { GitrepService } from '../service/gitrep.service';
import { IGitrep } from '../gitrep.model';
import { GitrepFormService } from './gitrep-form.service';

import { GitrepUpdateComponent } from './gitrep-update.component';

describe('Gitrep Management Update Component', () => {
  let comp: GitrepUpdateComponent;
  let fixture: ComponentFixture<GitrepUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let gitrepFormService: GitrepFormService;
  let gitrepService: GitrepService;
  let clientService: ClientService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), GitrepUpdateComponent],
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
      .overrideTemplate(GitrepUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(GitrepUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    gitrepFormService = TestBed.inject(GitrepFormService);
    gitrepService = TestBed.inject(GitrepService);
    clientService = TestBed.inject(ClientService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Client query and add missing value', () => {
      const gitrep: IGitrep = { id: 456 };
      const client: IClient = { id: 26800 };
      gitrep.client = client;

      const clientCollection: IClient[] = [{ id: 20025 }];
      jest.spyOn(clientService, 'query').mockReturnValue(of(new HttpResponse({ body: clientCollection })));
      const additionalClients = [client];
      const expectedCollection: IClient[] = [...additionalClients, ...clientCollection];
      jest.spyOn(clientService, 'addClientToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ gitrep });
      comp.ngOnInit();

      expect(clientService.query).toHaveBeenCalled();
      expect(clientService.addClientToCollectionIfMissing).toHaveBeenCalledWith(
        clientCollection,
        ...additionalClients.map(expect.objectContaining),
      );
      expect(comp.clientsSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const gitrep: IGitrep = { id: 456 };
      const client: IClient = { id: 9176 };
      gitrep.client = client;

      activatedRoute.data = of({ gitrep });
      comp.ngOnInit();

      expect(comp.clientsSharedCollection).toContain(client);
      expect(comp.gitrep).toEqual(gitrep);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IGitrep>>();
      const gitrep = { id: 123 };
      jest.spyOn(gitrepFormService, 'getGitrep').mockReturnValue(gitrep);
      jest.spyOn(gitrepService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ gitrep });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: gitrep }));
      saveSubject.complete();

      // THEN
      expect(gitrepFormService.getGitrep).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(gitrepService.update).toHaveBeenCalledWith(expect.objectContaining(gitrep));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IGitrep>>();
      const gitrep = { id: 123 };
      jest.spyOn(gitrepFormService, 'getGitrep').mockReturnValue({ id: null });
      jest.spyOn(gitrepService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ gitrep: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: gitrep }));
      saveSubject.complete();

      // THEN
      expect(gitrepFormService.getGitrep).toHaveBeenCalled();
      expect(gitrepService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IGitrep>>();
      const gitrep = { id: 123 };
      jest.spyOn(gitrepService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ gitrep });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(gitrepService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareClient', () => {
      it('Should forward to clientService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(clientService, 'compareClient');
        comp.compareClient(entity, entity2);
        expect(clientService.compareClient).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
