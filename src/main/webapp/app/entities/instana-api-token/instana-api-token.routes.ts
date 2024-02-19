import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { InstanaApiTokenComponent } from './list/instana-api-token.component';
import { InstanaApiTokenDetailComponent } from './detail/instana-api-token-detail.component';
import { InstanaApiTokenUpdateComponent } from './update/instana-api-token-update.component';
import InstanaApiTokenResolve from './route/instana-api-token-routing-resolve.service';

const instanaApiTokenRoute: Routes = [
  {
    path: '',
    component: InstanaApiTokenComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: InstanaApiTokenDetailComponent,
    resolve: {
      instanaApiToken: InstanaApiTokenResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: InstanaApiTokenUpdateComponent,
    resolve: {
      instanaApiToken: InstanaApiTokenResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: InstanaApiTokenUpdateComponent,
    resolve: {
      instanaApiToken: InstanaApiTokenResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default instanaApiTokenRoute;
