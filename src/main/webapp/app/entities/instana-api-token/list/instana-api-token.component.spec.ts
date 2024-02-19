import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { InstanaApiTokenService } from '../service/instana-api-token.service';

import { InstanaApiTokenComponent } from './instana-api-token.component';

describe('InstanaApiToken Management Component', () => {
  let comp: InstanaApiTokenComponent;
  let fixture: ComponentFixture<InstanaApiTokenComponent>;
  let service: InstanaApiTokenService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([{ path: 'instana-api-token', component: InstanaApiTokenComponent }]),
        HttpClientTestingModule,
        InstanaApiTokenComponent,
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({
              defaultSort: 'id,asc',
            }),
            queryParamMap: of(
              jest.requireActual('@angular/router').convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
              }),
            ),
            snapshot: { queryParams: {} },
          },
        },
      ],
    })
      .overrideTemplate(InstanaApiTokenComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(InstanaApiTokenComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(InstanaApiTokenService);

    const headers = new HttpHeaders();
    jest.spyOn(service, 'query').mockReturnValue(
      of(
        new HttpResponse({
          body: [{ id: 123 }],
          headers,
        }),
      ),
    );
  });

  it('Should call load all on init', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenCalled();
    expect(comp.instanaApiTokens?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to instanaApiTokenService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getInstanaApiTokenIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getInstanaApiTokenIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
