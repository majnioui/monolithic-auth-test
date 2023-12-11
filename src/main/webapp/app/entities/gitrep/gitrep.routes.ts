import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { GitrepComponent } from './list/gitrep.component';
import { GitrepDetailComponent } from './detail/gitrep-detail.component';
import { GitrepUpdateComponent } from './update/gitrep-update.component';
import GitrepResolve from './route/gitrep-routing-resolve.service';

const gitrepRoute: Routes = [
  {
    path: '',
    component: GitrepComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: GitrepDetailComponent,
    resolve: {
      gitrep: GitrepResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: GitrepUpdateComponent,
    resolve: {
      gitrep: GitrepResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: GitrepUpdateComponent,
    resolve: {
      gitrep: GitrepResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default gitrepRoute;
