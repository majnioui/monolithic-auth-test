import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'client',
        data: { pageTitle: 'monolithicauthtestApp.client.home.title' },
        loadChildren: () => import('./client/client.routes'),
      },
      {
        path: 'gitrep',
        data: { pageTitle: 'monolithicauthtestApp.gitrep.home.title' },
        loadChildren: () => import('./gitrep/gitrep.routes'),
      },
      {
        path: 'docker',
        data: { pageTitle: 'monolithicauthtestApp.docker.home.title' },
        loadChildren: () => import('./docker/docker.routes'),
      },
      {
        path: 'instana-api-token',
        data: { pageTitle: 'monolithicauthtestApp.instanaApiToken.home.title' },
        loadChildren: () => import('./instana-api-token/instana-api-token.routes'),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
