import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { DockerDetailComponent } from './docker-detail.component';

describe('Docker Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DockerDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: DockerDetailComponent,
              resolve: { docker: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(DockerDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load docker on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', DockerDetailComponent);

      // THEN
      expect(instance.docker).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
