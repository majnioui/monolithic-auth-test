import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { GitrepDetailComponent } from './gitrep-detail.component';

describe('Gitrep Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GitrepDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: GitrepDetailComponent,
              resolve: { gitrep: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(GitrepDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load gitrep on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', GitrepDetailComponent);

      // THEN
      expect(instance.gitrep).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
