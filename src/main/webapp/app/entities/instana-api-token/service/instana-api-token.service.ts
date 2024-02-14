import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IInstanaApiToken, NewInstanaApiToken } from '../instana-api-token.model';

export type PartialUpdateInstanaApiToken = Partial<IInstanaApiToken> & Pick<IInstanaApiToken, 'id'>;

export type EntityResponseType = HttpResponse<IInstanaApiToken>;
export type EntityArrayResponseType = HttpResponse<IInstanaApiToken[]>;

@Injectable({ providedIn: 'root' })
export class InstanaApiTokenService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/instana-api-tokens');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(instanaApiToken: NewInstanaApiToken): Observable<EntityResponseType> {
    return this.http.post<IInstanaApiToken>(this.resourceUrl, instanaApiToken, { observe: 'response' });
  }

  update(instanaApiToken: IInstanaApiToken): Observable<EntityResponseType> {
    return this.http.put<IInstanaApiToken>(`${this.resourceUrl}/${this.getInstanaApiTokenIdentifier(instanaApiToken)}`, instanaApiToken, {
      observe: 'response',
    });
  }

  partialUpdate(instanaApiToken: PartialUpdateInstanaApiToken): Observable<EntityResponseType> {
    return this.http.patch<IInstanaApiToken>(`${this.resourceUrl}/${this.getInstanaApiTokenIdentifier(instanaApiToken)}`, instanaApiToken, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IInstanaApiToken>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IInstanaApiToken[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getInstanaApiTokenIdentifier(instanaApiToken: Pick<IInstanaApiToken, 'id'>): number {
    return instanaApiToken.id;
  }

  compareInstanaApiToken(o1: Pick<IInstanaApiToken, 'id'> | null, o2: Pick<IInstanaApiToken, 'id'> | null): boolean {
    return o1 && o2 ? this.getInstanaApiTokenIdentifier(o1) === this.getInstanaApiTokenIdentifier(o2) : o1 === o2;
  }

  addInstanaApiTokenToCollectionIfMissing<Type extends Pick<IInstanaApiToken, 'id'>>(
    instanaApiTokenCollection: Type[],
    ...instanaApiTokensToCheck: (Type | null | undefined)[]
  ): Type[] {
    const instanaApiTokens: Type[] = instanaApiTokensToCheck.filter(isPresent);
    if (instanaApiTokens.length > 0) {
      const instanaApiTokenCollectionIdentifiers = instanaApiTokenCollection.map(
        instanaApiTokenItem => this.getInstanaApiTokenIdentifier(instanaApiTokenItem)!,
      );
      const instanaApiTokensToAdd = instanaApiTokens.filter(instanaApiTokenItem => {
        const instanaApiTokenIdentifier = this.getInstanaApiTokenIdentifier(instanaApiTokenItem);
        if (instanaApiTokenCollectionIdentifiers.includes(instanaApiTokenIdentifier)) {
          return false;
        }
        instanaApiTokenCollectionIdentifiers.push(instanaApiTokenIdentifier);
        return true;
      });
      return [...instanaApiTokensToAdd, ...instanaApiTokenCollection];
    }
    return instanaApiTokenCollection;
  }
}
