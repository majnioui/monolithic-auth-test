import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IDocker, NewDocker } from '../docker.model';

export type PartialUpdateDocker = Partial<IDocker> & Pick<IDocker, 'id'>;

export type EntityResponseType = HttpResponse<IDocker>;
export type EntityArrayResponseType = HttpResponse<IDocker[]>;

@Injectable({ providedIn: 'root' })
export class DockerService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/dockers');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(docker: NewDocker): Observable<EntityResponseType> {
    return this.http.post<IDocker>(this.resourceUrl, docker, { observe: 'response' });
  }

  update(docker: IDocker): Observable<EntityResponseType> {
    return this.http.put<IDocker>(`${this.resourceUrl}/${this.getDockerIdentifier(docker)}`, docker, { observe: 'response' });
  }

  partialUpdate(docker: PartialUpdateDocker): Observable<EntityResponseType> {
    return this.http.patch<IDocker>(`${this.resourceUrl}/${this.getDockerIdentifier(docker)}`, docker, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IDocker>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IDocker[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getDockerIdentifier(docker: Pick<IDocker, 'id'>): number {
    return docker.id;
  }

  compareDocker(o1: Pick<IDocker, 'id'> | null, o2: Pick<IDocker, 'id'> | null): boolean {
    return o1 && o2 ? this.getDockerIdentifier(o1) === this.getDockerIdentifier(o2) : o1 === o2;
  }

  addDockerToCollectionIfMissing<Type extends Pick<IDocker, 'id'>>(
    dockerCollection: Type[],
    ...dockersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const dockers: Type[] = dockersToCheck.filter(isPresent);
    if (dockers.length > 0) {
      const dockerCollectionIdentifiers = dockerCollection.map(dockerItem => this.getDockerIdentifier(dockerItem)!);
      const dockersToAdd = dockers.filter(dockerItem => {
        const dockerIdentifier = this.getDockerIdentifier(dockerItem);
        if (dockerCollectionIdentifiers.includes(dockerIdentifier)) {
          return false;
        }
        dockerCollectionIdentifiers.push(dockerIdentifier);
        return true;
      });
      return [...dockersToAdd, ...dockerCollection];
    }
    return dockerCollection;
  }
}
