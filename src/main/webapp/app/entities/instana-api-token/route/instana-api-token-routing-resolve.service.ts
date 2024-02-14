import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IInstanaApiToken } from '../instana-api-token.model';
import { InstanaApiTokenService } from '../service/instana-api-token.service';

export const instanaApiTokenResolve = (route: ActivatedRouteSnapshot): Observable<null | IInstanaApiToken> => {
  const id = route.params['id'];
  if (id) {
    return inject(InstanaApiTokenService)
      .find(id)
      .pipe(
        mergeMap((instanaApiToken: HttpResponse<IInstanaApiToken>) => {
          if (instanaApiToken.body) {
            return of(instanaApiToken.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default instanaApiTokenResolve;
