import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IGitrep } from '../gitrep.model';
import { GitrepService } from '../service/gitrep.service';

export const gitrepResolve = (route: ActivatedRouteSnapshot): Observable<null | IGitrep> => {
  const id = route.params['id'];
  if (id) {
    return inject(GitrepService)
      .find(id)
      .pipe(
        mergeMap((gitrep: HttpResponse<IGitrep>) => {
          if (gitrep.body) {
            return of(gitrep.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default gitrepResolve;
