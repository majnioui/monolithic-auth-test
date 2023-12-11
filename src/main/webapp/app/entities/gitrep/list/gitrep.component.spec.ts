import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { GitrepService } from '../service/gitrep.service';

import { GitrepComponent } from './gitrep.component';

describe('Gitrep Management Component', () => {
  let comp: GitrepComponent;
  let fixture: ComponentFixture<GitrepComponent>;
  let service: GitrepService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([{ path: 'gitrep', component: GitrepComponent }]), HttpClientTestingModule, GitrepComponent],
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
      .overrideTemplate(GitrepComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(GitrepComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(GitrepService);

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
    expect(comp.gitreps?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to gitrepService', () => {
      const entity = { id: 123 };
      jest.spyOn(service, 'getGitrepIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getGitrepIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });
});
