import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IGitrep, NewGitrep } from '../gitrep.model';

export type PartialUpdateGitrep = Partial<IGitrep> & Pick<IGitrep, 'id'>;

export type EntityResponseType = HttpResponse<IGitrep>;
export type EntityArrayResponseType = HttpResponse<IGitrep[]>;

@Injectable({ providedIn: 'root' })
export class GitrepService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/gitreps');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(gitrep: NewGitrep): Observable<EntityResponseType> {
    return this.http.post<IGitrep>(this.resourceUrl, gitrep, { observe: 'response' });
  }

  update(gitrep: IGitrep): Observable<EntityResponseType> {
    return this.http.put<IGitrep>(`${this.resourceUrl}/${this.getGitrepIdentifier(gitrep)}`, gitrep, { observe: 'response' });
  }

  partialUpdate(gitrep: PartialUpdateGitrep): Observable<EntityResponseType> {
    return this.http.patch<IGitrep>(`${this.resourceUrl}/${this.getGitrepIdentifier(gitrep)}`, gitrep, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IGitrep>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IGitrep[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getGitrepIdentifier(gitrep: Pick<IGitrep, 'id'>): number {
    return gitrep.id;
  }

  compareGitrep(o1: Pick<IGitrep, 'id'> | null, o2: Pick<IGitrep, 'id'> | null): boolean {
    return o1 && o2 ? this.getGitrepIdentifier(o1) === this.getGitrepIdentifier(o2) : o1 === o2;
  }

  addGitrepToCollectionIfMissing<Type extends Pick<IGitrep, 'id'>>(
    gitrepCollection: Type[],
    ...gitrepsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const gitreps: Type[] = gitrepsToCheck.filter(isPresent);
    if (gitreps.length > 0) {
      const gitrepCollectionIdentifiers = gitrepCollection.map(gitrepItem => this.getGitrepIdentifier(gitrepItem)!);
      const gitrepsToAdd = gitreps.filter(gitrepItem => {
        const gitrepIdentifier = this.getGitrepIdentifier(gitrepItem);
        if (gitrepCollectionIdentifiers.includes(gitrepIdentifier)) {
          return false;
        }
        gitrepCollectionIdentifiers.push(gitrepIdentifier);
        return true;
      });
      return [...gitrepsToAdd, ...gitrepCollection];
    }
    return gitrepCollection;
  }
}
