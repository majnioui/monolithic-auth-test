import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { InstanaApiTokenDetailComponent } from './instana-api-token-detail.component';

describe('InstanaApiToken Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstanaApiTokenDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: InstanaApiTokenDetailComponent,
              resolve: { instanaApiToken: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(InstanaApiTokenDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load instanaApiToken on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', InstanaApiTokenDetailComponent);

      // THEN
      expect(instance.instanaApiToken).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
