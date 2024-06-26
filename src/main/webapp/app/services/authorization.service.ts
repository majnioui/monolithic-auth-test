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

  getSuggestedBuildpack(repoName: string, userLogin: string, platformType: string): Observable<any> {
    return this.http.get<any>(`/suggest-buildpack?repoName=${repoName}&userLogin=${userLogin}&platformType=${platformType}`);
  }

  cloneRepository(repoName: string, userLogin: string, platformType: string): Observable<any> {
    const params = new HttpParams().set('repoName', repoName).set('userLogin', userLogin).set('platformType', platformType);
    return this.http.post('/clone-repo', null, { params });
  }

  executeBuildCommand(repoName: string, userLogin: string, platformType: string, command: string): Observable<any> {
    const params = new HttpParams()
      .set('repoName', repoName)
      .set('userLogin', userLogin)
      .set('platformType', platformType)
      .set('command', command);

    return this.http.post<any>('/execute-build-command', null, { params });
  }

  pushToRegistry(imageName: string, username: string, password: string, repoName: string, selectedRegistry: string) {
    const url = '/push-to-registry';
    const params = new HttpParams()
      .set('imageName', imageName)
      .set('username', username)
      .set('password', password)
      .set('repositoryName', repoName)
      .set('registryType', selectedRegistry);
    return this.http.post(url, params);
  }
}
