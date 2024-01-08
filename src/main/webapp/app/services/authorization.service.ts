import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthorizationService {
  constructor(private http: HttpClient) {}

  getGithubRepositories(): Observable<any[]> {
    return this.http.get<any[]>('/github/repositories');
  }

  getGitLabRepositories(): Observable<any[]> {
    return this.http.get<any[]>('/gitlab/repositories');
  }

  getBitbucketRepositories(): Observable<any[]> {
    return this.http.get<any[]>('/bitbucket/repositories');
  }

  saveClientUrl(clientUrl: string, platformType: string): Observable<any> {
    return this.http.post('/api/save-client-url', { clientUrl, platformType });
  }

  getSuggestedBuildpack(repoName: string, userLogin: string): Observable<any> {
    return this.http.get<any>(`/suggest-buildpack?repoName=${repoName}&userLogin=${userLogin}`);
  }

  cloneRepository(repoName: string, userLogin: string): Observable<any> {
    const params = new HttpParams().set('repoName', repoName).set('userLogin', userLogin);
    return this.http.post('/clone-repo', null, { params });
  }
}
