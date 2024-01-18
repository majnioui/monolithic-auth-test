import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IDocker } from '../docker.model';
import { DockerService } from '../service/docker.service';

export const dockerResolve = (route: ActivatedRouteSnapshot): Observable<null | IDocker> => {
  const id = route.params['id'];
  if (id) {
    return inject(DockerService)
      .find(id)
      .pipe(
        mergeMap((docker: HttpResponse<IDocker>) => {
          if (docker.body) {
            return of(docker.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default dockerResolve;
