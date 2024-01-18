import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { DockerComponent } from './list/docker.component';
import { DockerDetailComponent } from './detail/docker-detail.component';
import { DockerUpdateComponent } from './update/docker-update.component';
import DockerResolve from './route/docker-routing-resolve.service';

const dockerRoute: Routes = [
  {
    path: '',
    component: DockerComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: DockerDetailComponent,
    resolve: {
      docker: DockerResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: DockerUpdateComponent,
    resolve: {
      docker: DockerResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: DockerUpdateComponent,
    resolve: {
      docker: DockerResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default dockerRoute;
